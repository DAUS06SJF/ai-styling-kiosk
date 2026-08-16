package com.hackathon.styling.domain.styling.controller;

import com.hackathon.styling.domain.styling.dto.StylingRecommendationRequest;
import com.hackathon.styling.domain.styling.dto.StylingRecommendationResponse;
import com.hackathon.styling.domain.styling.service.StylingRecommendationService;
import com.hackathon.styling.domain.styling.websocket.StylingRecommendationWebSocketHandler;
import com.hackathon.styling.global.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/styling")
@RequiredArgsConstructor
@Validated
public class StylingRecommendationController {

    private final StylingRecommendationService stylingRecommendationService;
    private final StylingRecommendationWebSocketHandler webSocketHandler;

    @PostMapping("/recommendations")
    public ApiResponse<StylingRecommendationResponse> recommend(
            @Valid @RequestBody StylingRecommendationRequest request
    ) {
        StylingRecommendationResponse response = stylingRecommendationService.recommend(request);
        webSocketHandler.broadcastCreated(response);
        return ApiResponse.success(response);
    }

    @GetMapping("/recommendations")
    public ApiResponse<List<StylingRecommendationResponse>> findLatest(
            @RequestParam @NotBlank String mood,
            @RequestParam(defaultValue = "4") @Min(1) @Max(20) int limit
    ) {
        return ApiResponse.success(stylingRecommendationService.findLatest(mood, limit));
    }

    @GetMapping("/recommendations/{id}")
    public ApiResponse<StylingRecommendationResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(stylingRecommendationService.findById(id));
    }

    @PostMapping("/recommendations/{id}/select")
    public ApiResponse<StylingRecommendationResponse> select(@PathVariable Long id) {
        StylingRecommendationResponse response = stylingRecommendationService.select(id);
        webSocketHandler.broadcastSelected(response);
        return ApiResponse.success(response);
    }
}
