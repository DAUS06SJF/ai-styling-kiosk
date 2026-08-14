package com.hackathon.styling.domain.styling.controller;

import com.hackathon.styling.domain.styling.dto.StylingRecommendationRequest;
import com.hackathon.styling.domain.styling.dto.StylingRecommendationResponse;
import com.hackathon.styling.domain.styling.service.StylingRecommendationService;
import com.hackathon.styling.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/styling")
@RequiredArgsConstructor
public class StylingRecommendationController {

    private final StylingRecommendationService stylingRecommendationService;

    @PostMapping("/recommendations")
    public ApiResponse<StylingRecommendationResponse> recommend(
            @Valid @RequestBody StylingRecommendationRequest request
    ) {
        return ApiResponse.success(stylingRecommendationService.recommend(request));
    }

    @GetMapping("/recommendations/{id}")
    public ApiResponse<StylingRecommendationResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(stylingRecommendationService.findById(id));
    }
}
