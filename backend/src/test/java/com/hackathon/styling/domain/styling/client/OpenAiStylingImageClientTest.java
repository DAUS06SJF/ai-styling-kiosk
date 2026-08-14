package com.hackathon.styling.domain.styling.client;

import com.hackathon.styling.domain.styling.config.OpenAiProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiStylingImageClientTest {

    @Test
    @DisplayName("Image API의 base64 코디 이미지를 바이트로 변환한다")
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

        server.expect(once(), requestTo("https://api.openai.com/v1/images/generations"))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("gpt-image-2"))
                .andExpect(jsonPath("$.size").value("1024x1024"))
                .andExpect(jsonPath("$.quality").value("medium"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        byte[] image = client.generate(input());

        assertThat(image).isEqualTo(expectedImage);
        server.verify();
    }

    private StylingImageInput input() {
        StylingImageInput.ImageProduct selected = new StylingImageInput.ImageProduct(
                "백팩", "BACKPACK", "Black", "검정 백팩", ""
        );
        StylingImageInput.ImageProduct recommended = new StylingImageInput.ImageProduct(
                "티셔츠", "TSHIRT_TOP", "White", "흰 티셔츠", "선명한 대비"
        );
        return new StylingImageInput(
                "시티 룩",
                "색을 통일하세요.",
                "데이트",
                "미니멀",
                List.of("검정", "흰색"),
                selected,
                List.of(recommended)
        );
    }
}
