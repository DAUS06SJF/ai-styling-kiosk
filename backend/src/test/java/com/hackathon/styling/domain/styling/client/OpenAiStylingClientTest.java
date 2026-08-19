package com.hackathon.styling.domain.styling.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.styling.domain.styling.config.OpenAiProperties;
import com.hackathon.styling.global.error.BusinessException;
import com.hackathon.styling.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiStylingClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Responses API 구조화 출력에서 코디 추천을 파싱한다")
    void parseStructuredOutput() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenAiProperties properties = properties("test-key");
        OpenAiStylingClient client = new OpenAiStylingClient(builder.build(), objectMapper, properties);

        String response = """
                {
                  "output": [{
                    "type": "message",
                    "content": [{
                      "type": "output_text",
                      "text": "{\\\"lookName\\\":\\\"시티 룩\\\",\\\"stylingTip\\\":\\\"색을 통일하세요.\\\",\\\"recommendations\\\":[{\\\"productId\\\":2,\\\"reason\\\":\\\"잘 어울립니다.\\\"}]}"
                    }]
                  }]
                }
                """;

        server.expect(once(), requestTo("https://api.openai.com/v1/responses"))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("gpt-5.6-luna"))
                .andExpect(jsonPath("$.text.format.type").value("json_schema"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        StylingAiOutput output = client.generate(input());

        assertThat(output.lookName()).isEqualTo("시티 룩");
        assertThat(output.recommendations()).extracting(StylingAiOutput.Recommendation::productId)
                .containsExactly(2L);
        server.verify();
    }

    @Test
    @DisplayName("API 키가 없으면 외부 요청 전에 설정 오류를 반환한다")
    void rejectMissingApiKey() {
        OpenAiStylingClient client = new OpenAiStylingClient(
                RestClient.create(), objectMapper, properties("")
        );

        assertThatThrownBy(() -> client.generate(input()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.OPENAI_NOT_CONFIGURED));
    }

    private OpenAiProperties properties(String apiKey) {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey(apiKey);
        properties.setModel("gpt-5.6-luna");
        return properties;
    }

    private StylingAiInput input() {
        StylingAiInput.AiProduct selected =
                new StylingAiInput.AiProduct(1L, "백팩", "BACKPACK", "Black", "검정 백팩");
        StylingAiInput.AiProduct candidate =
                new StylingAiInput.AiProduct(2L, "티셔츠", "TSHIRT_TOP", "White", "흰 티셔츠");
        return new StylingAiInput(
                selected,
                List.of(candidate),
                "데이트",
                "미니멀",
                List.of("검정"),
                3,
                1,
                4
        );
    }
}
