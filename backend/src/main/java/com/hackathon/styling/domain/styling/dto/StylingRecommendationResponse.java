package com.hackathon.styling.domain.styling.dto;

import com.hackathon.styling.domain.admin.dto.ProductResponse;

import java.util.List;

public record StylingRecommendationResponse(
        ProductResponse selectedProduct,
        String lookName,
        String stylingTip,
        List<RecommendedProduct> recommendations
) {
    public record RecommendedProduct(ProductResponse product, String reason) {
    }
}
