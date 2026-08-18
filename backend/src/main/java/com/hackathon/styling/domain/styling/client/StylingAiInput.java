package com.hackathon.styling.domain.styling.client;

import java.util.List;

public record StylingAiInput(
        AiProduct selectedProduct,
        List<AiProduct> candidateProducts,
        String occasion,
        String mood,
        List<String> preferredColors,
        int recommendationCount,
        int variantIndex,
        int variantCount
) {
    public record AiProduct(
            Long productId,
            String name,
            String category,
            String color,
            String description
    ) {
    }
}
