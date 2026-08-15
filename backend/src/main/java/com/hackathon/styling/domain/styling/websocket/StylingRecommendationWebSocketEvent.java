package com.hackathon.styling.domain.styling.websocket;

import com.hackathon.styling.domain.styling.dto.StylingRecommendationResponse;

public record StylingRecommendationWebSocketEvent(
        String type,
        StylingRecommendationResponse data
) {
    public static StylingRecommendationWebSocketEvent created(StylingRecommendationResponse data) {
        return new StylingRecommendationWebSocketEvent("STYLING_RECOMMENDATION_CREATED", data);
    }

    public static StylingRecommendationWebSocketEvent selected(StylingRecommendationResponse data) {
        return new StylingRecommendationWebSocketEvent("STYLING_RECOMMENDATION_SELECTED", data);
    }
}
