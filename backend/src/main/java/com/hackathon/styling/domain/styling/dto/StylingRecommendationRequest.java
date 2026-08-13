package com.hackathon.styling.domain.styling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StylingRecommendationRequest(
        @NotBlank(message = "행거 코드는 필수입니다.")
        @Size(max = 100, message = "행거 코드는 100자 이하여야 합니다.")
        String hangerCode,

        @Size(max = 50, message = "상황은 50자 이하여야 합니다.")
        String occasion,

        @Size(max = 50, message = "분위기는 50자 이하여야 합니다.")
        String mood,

        @Size(max = 5, message = "선호 색상은 최대 5개까지 선택할 수 있습니다.")
        List<@NotBlank(message = "선호 색상은 빈 값일 수 없습니다.")
                @Size(max = 30, message = "선호 색상은 30자 이하여야 합니다.") String> preferredColors
) {
}
