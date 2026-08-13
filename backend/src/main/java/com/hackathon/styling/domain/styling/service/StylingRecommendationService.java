package com.hackathon.styling.domain.styling.service;

import com.hackathon.styling.domain.admin.domain.Product;
import com.hackathon.styling.domain.admin.dto.ProductResponse;
import com.hackathon.styling.domain.admin.repository.ProductRepository;
import com.hackathon.styling.domain.styling.client.StylingAiClient;
import com.hackathon.styling.domain.styling.client.StylingAiInput;
import com.hackathon.styling.domain.styling.client.StylingAiOutput;
import com.hackathon.styling.domain.styling.config.OpenAiProperties;
import com.hackathon.styling.domain.styling.dto.StylingRecommendationRequest;
import com.hackathon.styling.domain.styling.dto.StylingRecommendationResponse;
import com.hackathon.styling.global.error.BusinessException;
import com.hackathon.styling.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StylingRecommendationService {

    private static final int DESCRIPTION_LIMIT = 240;

    private final ProductRepository productRepository;
    private final StylingAiClient stylingAiClient;
    private final OpenAiProperties properties;

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

        StylingAiInput aiInput = new StylingAiInput(
                toAiProduct(selectedProduct),
                candidates.stream().map(this::toAiProduct).toList(),
                normalize(request.occasion()),
                normalize(request.mood()),
                request.preferredColors() == null
                        ? List.of()
                        : request.preferredColors().stream().map(String::trim).toList(),
                properties.getRecommendationCount()
        );

        StylingAiOutput aiOutput = stylingAiClient.generate(aiInput);
        List<StylingRecommendationResponse.RecommendedProduct> recommendations =
                hydrateAndValidate(aiOutput, candidates);

        if (recommendations.isEmpty()) {
            throw new BusinessException(ErrorCode.STYLING_GENERATION_FAILED,
                    "AI가 현재 재고에서 사용할 수 있는 상품을 추천하지 못했습니다.");
        }

        return new StylingRecommendationResponse(
                ProductResponse.from(selectedProduct),
                aiOutput.lookName(),
                aiOutput.stylingTip(),
                recommendations
        );
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

    private String normalizeReason(String value) {
        String normalized = normalize(value);
        return normalized.isBlank() ? "선택한 상품과 조화로운 코디 아이템입니다." : normalized;
    }
}
