package com.hackathon.styling.domain.styling.service;

import com.hackathon.styling.domain.admin.domain.Product;
import com.hackathon.styling.domain.admin.dto.ProductResponse;
import com.hackathon.styling.domain.admin.repository.ProductRepository;
import com.hackathon.styling.domain.styling.client.StylingAiClient;
import com.hackathon.styling.domain.styling.client.StylingAiInput;
import com.hackathon.styling.domain.styling.client.StylingAiOutput;
import com.hackathon.styling.domain.styling.client.StylingImageClient;
import com.hackathon.styling.domain.styling.client.StylingImageInput;
import com.hackathon.styling.domain.styling.client.BuiltInStylingImageFallback;
import com.hackathon.styling.domain.styling.config.OpenAiProperties;
import com.hackathon.styling.domain.styling.domain.StylingRecommendation;
import com.hackathon.styling.domain.styling.dto.StylingRecommendationRequest;
import com.hackathon.styling.domain.styling.dto.StylingRecommendationResponse;
import com.hackathon.styling.domain.styling.repository.StylingRecommendationRepository;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StylingRecommendationService {

    private static final int DESCRIPTION_LIMIT = 240;
    private static final int LOOK_VARIANT_COUNT = 4;
    private static final Long DEMO_ANCHOR_PRODUCT_ID = 260L;
    private static final Pattern FALLBACK_VARIANT_PATTERN = Pattern.compile(
            "/(?:minimal|street|vintage)-(?:0)?([1-4])(?:-v2|-consistent)?\\.png$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Map<String, List<List<Long>>> CURATED_PRODUCT_IDS = Map.of(
            "MINIMAL", List.of(
                    List.of(281L, 242L, 89L, 239L),
                    List.of(280L, 247L, 99L, 204L),
                    List.of(282L, 243L, 125L, 205L),
                    List.of(279L, 251L, 106L, 237L)
            ),
            "STREET", List.of(
                    List.of(275L, 280L, 249L, 17L),
                    List.of(258L, 281L, 250L, 106L),
                    List.of(279L, 251L, 18L, 237L),
                    List.of(300L, 280L, 252L, 214L)
            ),
            "VINTAGE", List.of(
                    List.of(303L, 280L, 240L, 16L),
                    List.of(256L, 279L, 241L, 104L),
                    List.of(280L, 243L, 121L, 205L),
                    List.of(282L, 247L, 125L, 204L)
            )
    );

    private final ProductRepository productRepository;
    private final StylingRecommendationRepository stylingRecommendationRepository;
    private final StylingAiClient stylingAiClient;
    private final StylingImageClient stylingImageClient;
    private final BuiltInStylingImageFallback builtInStylingImageFallback;
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
                    pendingLook.imageInput(),
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
        String kodi = properties.isImageFallbackOnly()
                ? builtInStylingImageFallback.publicUrl(generatedLook.imageInput())
                : "";
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
        stylingRecommendationRepository.saveAndFlush(styling);
        if (!properties.isImageFallbackOnly()) {
            styling.attachGeneratedImage(generatedLook.imageBytes());
        }
        return StylingRecommendationResponse.from(styling);
    }

    public StylingRecommendationResponse findById(Long id) {
        StylingRecommendation styling = stylingRecommendationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STYLING_NOT_FOUND));
        return adaptLegacyFallback(styling, resolveVariant(styling));
    }

    public List<StylingRecommendationResponse> findLatest(String mood, int limit) {
        List<StylingRecommendation> stylings = stylingRecommendationRepository
                .findByMoodIgnoreCaseOrderByIdDesc(mood.trim(), PageRequest.of(0, limit));
        return java.util.stream.IntStream.range(0, stylings.size())
                .mapToObj(index -> adaptLegacyFallback(
                        stylings.get(index),
                        resolveVariant(stylings.get(index), stylings.size() - index)
                ))
                .toList();
    }

    @Transactional
    public StylingRecommendationResponse select(Long id) {
        StylingRecommendation styling = stylingRecommendationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STYLING_NOT_FOUND));
        int variant = resolveVariant(styling);
        if (shouldAdaptLegacyFallback(styling)) {
            styling.select(builtInStylingImageFallback.publicUrl(styleKey(styling), variant));
        } else {
            styling.select();
        }
        stylingRecommendationRepository.flush();
        return adaptLegacyFallback(styling, variant);
    }

    public StoredStylingImage findImage(Long id) {
        StylingRecommendation styling = stylingRecommendationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STYLING_NOT_FOUND));
        byte[] imageBytes = styling.getKodiImage();
        if (imageBytes == null || imageBytes.length == 0) {
            throw new BusinessException(ErrorCode.STYLING_NOT_FOUND,
                    "저장된 코디 이미지가 없습니다.");
        }
        String contentType = styling.getKodiImageContentType();
        return new StoredStylingImage(
                imageBytes.clone(),
                contentType == null || contentType.isBlank() ? "image/png" : contentType
        );
    }

    private StylingRecommendationResponse adaptLegacyFallback(
            StylingRecommendation styling,
            int variant
    ) {
        StylingRecommendationResponse original = StylingRecommendationResponse.from(styling);
        if (!shouldAdaptLegacyFallback(styling)) {
            return original;
        }

        String style = styleKey(styling);
        List<Long> recommendedIds = CURATED_PRODUCT_IDS.get(style).get(variant - 1);
        List<Long> allProductIds = new ArrayList<>();
        allProductIds.add(DEMO_ANCHOR_PRODUCT_ID);
        allProductIds.addAll(recommendedIds);

        Map<Long, Product> productsById = new LinkedHashMap<>();
        productRepository.findAllById(allProductIds)
                .forEach(product -> productsById.put(product.getId(), product));
        Product anchor = productsById.get(DEMO_ANCHOR_PRODUCT_ID);
        if (anchor == null || !productsById.keySet().containsAll(recommendedIds)) {
            return original;
        }

        String imageUrl = builtInStylingImageFallback.publicUrl(style, variant);
        return new StylingRecommendationResponse(
                original.id(),
                ProductResponse.from(anchor),
                original.occasion(),
                original.mood(),
                original.preferredColors(),
                original.lookName(),
                original.stylingTip(),
                recommendedIds.stream()
                        .map(productsById::get)
                        .map(product -> new StylingRecommendationResponse.RecommendedProduct(
                                ProductResponse.from(product),
                                "선택한 MCM 티셔츠와 조화되는 매장 재고 상품입니다."
                        ))
                        .toList(),
                imageUrl,
                original.kodiSelected() == null ? null : imageUrl
        );
    }

    private boolean shouldAdaptLegacyFallback(StylingRecommendation styling) {
        String style = styleKey(styling);
        if (!CURATED_PRODUCT_IDS.containsKey(style)) {
            return false;
        }
        String kodi = styling.getKodi();
        return kodi != null
                && kodi.contains("/generated-stylings/")
                && !kodi.contains("/api/styling/recommendations/");
    }

    private int resolveVariant(StylingRecommendation styling) {
        Matcher matcher = FALLBACK_VARIANT_PATTERN.matcher(normalize(styling.getKodi()));
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        List<StylingRecommendation> latest = stylingRecommendationRepository
                .findByMoodIgnoreCaseOrderByIdDesc(
                        styling.getMood(),
                        PageRequest.of(0, LOOK_VARIANT_COUNT)
                );
        int index = java.util.stream.IntStream.range(0, latest.size())
                .filter(value -> latest.get(value).getId().equals(styling.getId()))
                .findFirst()
                .orElse(0);
        return Math.max(1, Math.min(LOOK_VARIANT_COUNT, latest.size() - index));
    }

    private int resolveVariant(StylingRecommendation styling, int fallbackVariant) {
        Matcher matcher = FALLBACK_VARIANT_PATTERN.matcher(normalize(styling.getKodi()));
        return matcher.find()
                ? Integer.parseInt(matcher.group(1))
                : Math.max(1, Math.min(LOOK_VARIANT_COUNT, fallbackVariant));
    }

    private String styleKey(StylingRecommendation styling) {
        return normalize(styling.getMood()).toUpperCase(java.util.Locale.ROOT);
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
            StylingImageInput imageInput,
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

    public record StoredStylingImage(byte[] bytes, String contentType) {
    }
}
