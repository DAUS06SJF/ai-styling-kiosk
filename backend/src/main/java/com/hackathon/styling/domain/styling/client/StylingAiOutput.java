package com.hackathon.styling.domain.styling.client;

import java.util.List;

public record StylingAiOutput(
        String lookName,
        String stylingTip,
        List<Recommendation> recommendations
) {
    public record Recommendation(Long productId, String reason) {
    }
}
