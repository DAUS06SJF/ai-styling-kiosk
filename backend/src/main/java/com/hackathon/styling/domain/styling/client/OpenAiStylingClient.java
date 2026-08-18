package com.hackathon.styling.domain.styling.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiStylingClient implements StylingAiClient {

    private static final String SYSTEM_INSTRUCTIONS = """
            당신은 럭셔리 패션 매장의 전문 스타일리스트입니다.
            selectedProduct와 자연스럽게 어울리는 상품을 candidateProducts에서만 고르세요.
            candidateProducts에 없는 productId를 만들거나 selectedProduct 자체를 추천하면 안 됩니다.
            색상 조화, 카테고리 조합, 사용 상황을 고려하고 같은 종류만 반복해서 고르지 마세요.
            mood가 나타내는 스타일을 가장 중요한 기준으로 적용하세요.
            variantIndex에 맞춰 같은 요청의 다른 코디와 구분되는 독립적인 상품 조합을 만드세요.
            추천 이유와 스타일링 팁은 키오스크 고객이 바로 이해할 수 있는 자연스러운 한국어로 작성하세요.
            """;

    private final RestClient openAiRestClient;
    private final ObjectMapper objectMapper;
    private final OpenAiProperties properties;

    @Override
    public StylingAiOutput generate(StylingAiInput input) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new BusinessException(ErrorCode.OPENAI_NOT_CONFIGURED,
                    "OPENAI_API_KEY 환경변수를 설정한 뒤 다시 요청해 주세요.");
        }

        try {
            JsonNode response = openAiRestClient.post()
                    .uri("/v1/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createRequestBody(input))
                    .retrieve()
                    .body(JsonNode.class);

            String outputText = extractOutputText(response);
            return objectMapper.readValue(outputText, StylingAiOutput.class);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            log.warn("OpenAI Responses API call failed: {}", exception.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.STYLING_GENERATION_FAILED,
                    "OpenAI API 요청에 실패했습니다. 잠시 후 다시 시도해 주세요.");
        } catch (JsonProcessingException exception) {
            log.warn("Could not parse OpenAI structured output");
            throw new BusinessException(ErrorCode.STYLING_GENERATION_FAILED,
                    "AI 추천 결과를 처리하지 못했습니다.");
        }
    }

    private Map<String, Object> createRequestBody(StylingAiInput input) throws JsonProcessingException {
        String userInput = "다음 JSON을 바탕으로 %d개의 코디 상품을 추천하세요.\n%s"
                .formatted(input.recommendationCount(), objectMapper.writeValueAsString(input));

        return Map.of(
                "model", properties.getModel(),
                "instructions", SYSTEM_INSTRUCTIONS,
                "input", userInput,
                "max_output_tokens", 1200,
                "text", Map.of("format", structuredOutputFormat(input.recommendationCount()))
        );
    }

    private Map<String, Object> structuredOutputFormat(int recommendationCount) {
        Map<String, Object> recommendation = Map.of(
                "type", "object",
                "properties", Map.of(
                        "productId", Map.of("type", "integer"),
                        "reason", Map.of("type", "string")
                ),
                "required", List.of("productId", "reason"),
                "additionalProperties", false
        );

        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "lookName", Map.of("type", "string"),
                        "stylingTip", Map.of("type", "string"),
                        "recommendations", Map.of(
                                "type", "array",
                                "minItems", 1,
                                "maxItems", recommendationCount,
                                "items", recommendation
                        )
                ),
                "required", List.of("lookName", "stylingTip", "recommendations"),
                "additionalProperties", false
        );

        return Map.of(
                "type", "json_schema",
                "name", "styling_recommendation",
                "strict", true,
                "schema", schema
        );
    }

    private String extractOutputText(JsonNode response) {
        if (response == null) {
            throw new BusinessException(ErrorCode.STYLING_GENERATION_FAILED);
        }

        for (JsonNode outputItem : response.path("output")) {
            for (JsonNode contentItem : outputItem.path("content")) {
                if ("output_text".equals(contentItem.path("type").asText())
                        && !contentItem.path("text").asText().isBlank()) {
                    return contentItem.path("text").asText();
                }
            }
        }

        throw new BusinessException(ErrorCode.STYLING_GENERATION_FAILED,
                "OpenAI API가 추천 내용을 반환하지 않았습니다.");
    }
}
