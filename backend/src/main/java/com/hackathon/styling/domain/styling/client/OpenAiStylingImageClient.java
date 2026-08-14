package com.hackathon.styling.domain.styling.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.hackathon.styling.domain.styling.config.OpenAiProperties;
import com.hackathon.styling.global.error.BusinessException;
import com.hackathon.styling.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiStylingImageClient implements StylingImageClient {

    private final RestClient openAiRestClient;
    private final OpenAiProperties properties;

    @Override
    public byte[] generate(StylingImageInput input) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new BusinessException(ErrorCode.OPENAI_NOT_CONFIGURED,
                    "OPENAI_API_KEY 환경변수를 설정한 뒤 다시 요청해 주세요.");
        }

        try {
            JsonNode response = openAiRestClient.post()
                    .uri("/v1/images/generations")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", properties.getImageModel(),
                            "prompt", createPrompt(input),
                            "size", properties.getImageSize(),
                            "quality", properties.getImageQuality(),
                            "n", 1
                    ))
                    .retrieve()
                    .body(JsonNode.class);

            String imageBase64 = response == null
                    ? ""
                    : response.path("data").path(0).path("b64_json").asText();
            if (imageBase64.isBlank()) {
                throw new BusinessException(ErrorCode.STYLING_GENERATION_FAILED,
                        "OpenAI API가 코디 이미지를 반환하지 않았습니다.");
            }
            return Base64.getDecoder().decode(imageBase64);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            log.warn("OpenAI Image API call failed: {}", exception.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.STYLING_GENERATION_FAILED,
                    "OpenAI 코디 이미지 생성에 실패했습니다. 잠시 후 다시 시도해 주세요.");
        } catch (IllegalArgumentException exception) {
            log.warn("Could not decode OpenAI image output");
            throw new BusinessException(ErrorCode.STYLING_GENERATION_FAILED,
                    "AI 코디 이미지를 처리하지 못했습니다.");
        }
    }

    private String createPrompt(StylingImageInput input) {
        StringBuilder prompt = new StringBuilder("""
                Create one polished, photorealistic luxury fashion flat-lay styling image for a retail kiosk.
                Use a clean neutral studio background, balanced editorial composition, soft realistic lighting,
                and show the complete outfit without a person. Do not add captions, labels, watermarks, or extra products.
                """);
        prompt.append("\nLook name: ").append(input.lookName());
        prompt.append("\nStyling direction: ").append(input.stylingTip());
        prompt.append("\nOccasion: ").append(input.occasion());
        prompt.append("\nMood: ").append(input.mood());
        prompt.append("\nPreferred colors: ").append(String.join(", ", input.preferredColors()));
        appendProduct(prompt, "Selected anchor product", input.selectedProduct());
        for (int index = 0; index < input.recommendedProducts().size(); index++) {
            appendProduct(prompt, "Recommended product " + (index + 1), input.recommendedProducts().get(index));
        }
        return prompt.toString();
    }

    private void appendProduct(
            StringBuilder prompt,
            String heading,
            StylingImageInput.ImageProduct product
    ) {
        prompt.append("\n").append(heading).append(": ")
                .append(product.name()).append("; category=").append(product.category())
                .append("; color=").append(product.color())
                .append("; description=").append(product.description());
        if (product.recommendationReason() != null && !product.recommendationReason().isBlank()) {
            prompt.append("; styling role=").append(product.recommendationReason());
        }
    }
}
