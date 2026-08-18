package com.hackathon.styling.domain.styling.client;

import com.hackathon.styling.domain.styling.config.OpenAiProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiStylingImageClientTest {

    @Test
    @DisplayName("상품 원본을 참조 이미지로 전달하고 생성 결과를 바이트로 변환한다")
    void generateStylingImage() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("test-key");
        properties.setImageModel("gpt-image-2");
        properties.setImageSize("1024x1024");
        properties.setImageQuality("medium");
        OpenAiStylingImageClient client = new OpenAiStylingImageClient(builder.build(), properties);

        byte[] expectedImage = "fake-png".getBytes(StandardCharsets.UTF_8);
        String response = "{\"data\":[{\"b64_json\":\""
                + Base64.getEncoder().encodeToString(expectedImage)
                + "\"}]}";

        byte[] referenceImage = "fake-reference".getBytes(StandardCharsets.UTF_8);
        server.expect(once(), requestTo("https://example.com/selected.png"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(referenceImage, MediaType.IMAGE_PNG));
        server.expect(once(), requestTo("https://example.com/recommended.png"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(referenceImage, MediaType.IMAGE_PNG));
        server.expect(once(), requestTo("https://api.openai.com/v1/images/edits"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(request -> assertThat(request.getHeaders().getContentType())
                        .isNotNull()
                        .satisfies(type -> assertThat(type.isCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                                .isTrue()))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        byte[] image = client.generate(input());

        assertThat(image).isEqualTo(expectedImage);
        server.verify();
    }

    private StylingImageInput input() {
        StylingImageInput.ImageProduct selected = new StylingImageInput.ImageProduct(
                1L,
                "백팩",
                "BACKPACK",
                "Black",
                "검정 백팩",
                "https://example.com/selected.png",
                ""
        );
        StylingImageInput.ImageProduct recommended = new StylingImageInput.ImageProduct(
                2L,
                "티셔츠",
                "TSHIRT_TOP",
                "White",
                "흰 티셔츠",
                "https://example.com/recommended.png",
                "선명한 대비"
        );
        return new StylingImageInput(
                "시티 룩",
                "색을 통일하세요.",
                "데이트",
                "미니멀",
                List.of("검정", "흰색"),
                1,
                4,
                selected,
                List.of(recommended)
        );
    }
}
