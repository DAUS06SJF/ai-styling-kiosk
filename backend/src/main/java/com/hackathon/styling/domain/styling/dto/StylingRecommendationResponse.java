package com.hackathon.styling.domain.styling.dto;

import com.hackathon.styling.domain.admin.dto.ProductResponse;
import com.hackathon.styling.domain.styling.domain.StylingRecommendation;

import java.util.List;

public record StylingRecommendationResponse(
        Long id,
        ProductResponse selectedProduct,
        String occasion,
        String mood,
        List<String> preferredColors,
        String lookName,
        String stylingTip,
        List<RecommendedProduct> recommendations,
        String kodi,
        String kodiSelected
) {
    public static StylingRecommendationResponse from(StylingRecommendation styling) {
        return new StylingRecommendationResponse(
                styling.getId(),
                ProductResponse.from(styling.getSelectedProduct()),
                styling.getOccasion(),
                styling.getMood(),
                styling.getPreferredColorList(),
                styling.getLookName(),
                styling.getStylingTip(),
                styling.getItems().stream()
                        .map(item -> new RecommendedProduct(
                                ProductResponse.from(item.getProduct()),
                                item.getReason()
                        ))
                        .toList(),
                styling.getKodi(),
                styling.getKodiSelected()
        );
    }

    public record RecommendedProduct(ProductResponse product, String reason) {
    }
}
