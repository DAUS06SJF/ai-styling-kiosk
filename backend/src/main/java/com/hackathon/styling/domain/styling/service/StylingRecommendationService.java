package com.hackathon.styling.domain.styling.service;

import com.hackathon.styling.domain.admin.domain.Product;
import com.hackathon.styling.domain.admin.dto.ProductResponse;
import com.hackathon.styling.domain.admin.repository.ProductRepository;
import com.hackathon.styling.domain.styling.client.StylingAiClient;
import com.hackathon.styling.domain.styling.client.StylingAiInput;
import com.hackathon.styling.domain.styling.client.StylingAiOutput;
import com.hackathon.styling.domain.styling.client.StylingImageClient;
import com.hackathon.styling.domain.styling.client.StylingImageInput;
import com.hackathon.styling.domain.styling.config.OpenAiProperties;
import com.hackathon.styling.domain.styling.domain.StylingRecommendation;
import com.hackathon.styling.domain.styling.dto.StylingRecommendationRequest;
import com.hackathon.styling.domain.styling.dto.StylingRecommendationResponse;
import com.hackathon.styling.domain.styling.repository.StylingRecommendationRepository;
import com.hackathon.styling.domain.styling.storage.StylingImageStorage;
import com.hackathon.styling.global.error.BusinessException;
import com.hackathon.styling.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StylingRecommendationService {

    private static final int DESCRIPTION_LIMIT = 240;
    private static final int LOOK_VARIANT_COUNT = 4;

    private final ProductRepository productRepository;
    private final StylingRecommendationRepository stylingRecommendationRepository;
    private final StylingAiClient stylingAiClient;
    private final StylingImageClient stylingImageClient;
    private final StylingImageStorage stylingImageStorage;
    private final OpenAiProperties properties;

    @Transactional
    public StylingRecommendationResponse recommend(StylingRecommendationRequest request) {
        Product selectedProduct = productRepository.findByHangerCodeIgnoreCase(request.hangerCode().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.HANGER_NOT_FOUND));

        List<Product> candidates = productRepository.findAvailableForStyling(
                selectedProduct.getId(),
                PageRequest.of(0, properties.getCandidateLimit())
        );
        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.STYLING_CANDIDATE_NOT_FOUND);
        }

        List<GeneratedLook> generatedLooks = generateLooks(request, selectedProduct, candidates);
        StylingRecommendationResponse latestResponse = null;
        for (GeneratedLook generatedLook : generatedLooks) {
            latestResponse = saveLook(request, selectedProduct, generatedLook);
        }
        return latestResponse;
    }

    private List<GeneratedLook> generateLooks(
            StylingRecommendationRequest request,
            Product selectedProduct,
            List<Product> candidates
    ) {
        Set<Long> usedProductIds = new LinkedHashSet<>();
        List<PendingLook> pendingLooks = new ArrayList<>(LOOK_VARIANT_COUNT);

        for (int variantIndex = 1; variantIndex <= LOOK_VARIANT_COUNT; variantIndex++) {
            List<Product> unusedCandidates = candidates.stream()
                    .filter(product -> !usedProductIds.contains(product.getId()))
                    .toList();
            List<Product> availableCandidates = unusedCandidates.isEmpty()
                    ? candidates
                    : unusedCandidates;

            StylingAiInput aiInput = new StylingAiInput(
                    toAiProduct(selectedProduct),
                    availableCandidates.stream().map(this::toAiProduct).toList(),
                    normalize(request.occasion()),
                    normalize(request.mood()),
                    normalizeColors(request.preferredColors()),
                    properties.getRecommendationCount(),
                    variantIndex,
                    LOOK_VARIANT_COUNT
            );
            StylingAiOutput aiOutput = stylingAiClient.generate(aiInput);
            List<StylingRecommendationResponse.RecommendedProduct> recommendations =
                    hydrateAndValidate(aiOutput, availableCandidates);

            if (recommendations.isEmpty()) {
                throw new BusinessException(ErrorCode.STYLING_GENERATION_FAILED,
                        "AI가 현재 재고에서 사용할 수 있는 상품을 추천하지 못했습니다.");
            }

            List<Product> recommendedProducts = recommendations.stream()
                    .map(recommendation -> findRecommendedProduct(availableCandidates, recommendation))
                    .toList();
            recommendedProducts.forEach(product -> usedProductIds.add(product.getId()));

            StylingImageInput imageInput = new StylingImageInput(
                    normalize(aiOutput.lookName()),
                    normalize(aiOutput.stylingTip()),
                    normalize(request.occasion()),
                    normalize(request.mood()),
                    normalizeColors(request.preferredColors()),
                    variantIndex,
                    LOOK_VARIANT_COUNT,
                    toImageProduct(selectedProduct, ""),
                    recommendedProducts.stream()
                            .map(product -> toImageProduct(
                                    product,
                                    recommendationReason(recommendations, product.getId())
                            ))
                            .toList()
            );
            pendingLooks.add(new PendingLook(
                    aiOutput,
                    recommendations,
                    recommendedProducts,
                    imageInput
            ));
        }
        return generateImages(pendingLooks);
    }

    private List<GeneratedLook> generateImages(List<PendingLook> pendingLooks) {
        List<GeneratedLook> generatedLooks = new ArrayList<>(pendingLooks.size());
        // 이미지 편집 API의 동시 생성 한도를 넘기지 않도록 4개 룩을 순서대로 생성한다.
        for (PendingLook pendingLook : pendingLooks) {
            generatedLooks.add(new GeneratedLook(
                    pendingLook.aiOutput(),
                    pendingLook.recommendations(),
                    pendingLook.recommendedProducts(),
                    stylingImageClient.generate(pendingLook.imageInput())
            ));
        }
        return generatedLooks;
    }

    private StylingRecommendationResponse saveLook(
            StylingRecommendationRequest request,
            Product selectedProduct,
            GeneratedLook generatedLook
    ) {
        String kodi = stylingImageStorage.store(generatedLook.imageBytes());
        StylingAiOutput aiOutput = generatedLook.aiOutput();
        StylingRecommendation styling = new StylingRecommendation(
                selectedProduct,
                normalize(request.occasion()),
                normalize(request.mood()),
                normalizeColors(request.preferredColors()),
                normalize(aiOutput.lookName()),
                normalize(aiOutput.stylingTip()),
                kodi
        );
        for (int index = 0; index < generatedLook.recommendations().size(); index++) {
            StylingRecommendationResponse.RecommendedProduct recommendation =
                    generatedLook.recommendations().get(index);
            styling.addItem(
                    generatedLook.recommendedProducts().get(index),
                    recommendation.reason(),
                    index + 1
            );
        }
        stylingRecommendationRepository.save(styling);
        return StylingRecommendationResponse.from(styling);
    }

    public StylingRecommendationResponse findById(Long id) {
        StylingRecommendation styling = stylingRecommendationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STYLING_NOT_FOUND));
        return StylingRecommendationResponse.from(styling);
    }

    public List<StylingRecommendationResponse> findLatest(String mood, int limit) {
        return stylingRecommendationRepository
                .findByMoodIgnoreCaseOrderByIdDesc(mood.trim(), PageRequest.of(0, limit))
                .stream()
                .map(StylingRecommendationResponse::from)
                .toList();
    }

    @Transactional
    public StylingRecommendationResponse select(Long id) {
        StylingRecommendation styling = stylingRecommendationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STYLING_NOT_FOUND));
        styling.select();
        stylingRecommendationRepository.flush();
        return StylingRecommendationResponse.from(styling);
    }

    private List<StylingRecommendationResponse.RecommendedProduct> hydrateAndValidate(
            StylingAiOutput aiOutput,
            List<Product> candidates
    ) {
        if (aiOutput == null || aiOutput.recommendations() == null) {
            return List.of();
        }

        Map<Long, Product> candidatesById = new LinkedHashMap<>();
        candidates.forEach(product -> candidatesById.put(product.getId(), product));
        Map<Long, Boolean> seen = new LinkedHashMap<>();

        return aiOutput.recommendations().stream()
                .filter(item -> item != null && item.productId() != null)
                .filter(item -> candidatesById.containsKey(item.productId()))
                .filter(item -> seen.putIfAbsent(item.productId(), Boolean.TRUE) == null)
                .limit(properties.getRecommendationCount())
                .map(item -> new StylingRecommendationResponse.RecommendedProduct(
                        ProductResponse.from(candidatesById.get(item.productId())),
                        normalizeReason(item.reason())
                ))
                .toList();
    }

    private StylingAiInput.AiProduct toAiProduct(Product product) {
        return new StylingAiInput.AiProduct(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getColor(),
                truncate(product.getDescription())
        );
    }

    private StylingImageInput.ImageProduct toImageProduct(Product product, String recommendationReason) {
        return new StylingImageInput.ImageProduct(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getColor(),
                truncate(product.getDescription()),
                product.getImageUrl(),
                recommendationReason
        );
    }

    private Product findRecommendedProduct(
            List<Product> candidates,
            StylingRecommendationResponse.RecommendedProduct recommendation
    ) {
        return candidates.stream()
                .filter(candidate -> candidate.getId().equals(recommendation.product().getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.STYLING_GENERATION_FAILED));
    }

    private String recommendationReason(
            List<StylingRecommendationResponse.RecommendedProduct> recommendations,
            Long productId
    ) {
        return recommendations.stream()
                .filter(recommendation -> recommendation.product().getId().equals(productId))
                .map(StylingRecommendationResponse.RecommendedProduct::reason)
                .findFirst()
                .orElse("");
    }

    private String truncate(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() <= DESCRIPTION_LIMIT
                ? trimmed
                : trimmed.substring(0, DESCRIPTION_LIMIT);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private List<String> normalizeColors(List<String> colors) {
        if (colors == null) {
            return List.of();
        }
        return colors.stream()
                .filter(color -> color != null && !color.isBlank())
                .map(String::trim)
                .toList();
    }

    private String normalizeReason(String value) {
        String normalized = normalize(value);
        return normalized.isBlank() ? "선택한 상품과 조화로운 코디 아이템입니다." : normalized;
    }

    private record GeneratedLook(
            StylingAiOutput aiOutput,
            List<StylingRecommendationResponse.RecommendedProduct> recommendations,
            List<Product> recommendedProducts,
            byte[] imageBytes
    ) {
    }

    private record PendingLook(
            StylingAiOutput aiOutput,
            List<StylingRecommendationResponse.RecommendedProduct> recommendations,
            List<Product> recommendedProducts,
            StylingImageInput imageInput
    ) {
    }
}
