package com.hackathon.styling.domain.styling.client;

import java.util.List;

public record StylingImageInput(
        String lookName,
        String stylingTip,
        String occasion,
        String mood,
        List<String> preferredColors,
        int variantIndex,
        int variantCount,
        ImageProduct selectedProduct,
        List<ImageProduct> recommendedProducts
) {
    public record ImageProduct(
            Long productId,
            String name,
            String category,
            String color,
            String description,
            String imageUrl,
            String recommendationReason
    ) {
    }
}
