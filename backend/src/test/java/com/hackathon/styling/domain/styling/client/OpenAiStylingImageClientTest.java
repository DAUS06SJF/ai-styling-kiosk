package com.hackathon.styling.domain.styling.client;

import com.hackathon.styling.domain.styling.config.OpenAiProperties;
import com.hackathon.styling.domain.styling.storage.StylingImageProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class OpenAiStylingImageClientTest {

    @TempDir
    Path tempDirectory;

    @Test
    @DisplayName("폴백 전용 모드에서는 OpenAI 호출 없이 서버 기본 이미지를 반환한다")
    void useBundledImageImmediatelyInFallbackOnlyMode() throws Exception {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenAiProperties properties = new OpenAiProperties();
        properties.setImageFallbackOnly(true);
        byte[] expectedImage = "fallback-only".getBytes(StandardCharsets.UTF_8);
        Path fallbackDirectory = Files.createDirectories(
                tempDirectory.resolve("mannequin-batch-20260816")
        );
        Files.write(fallbackDirectory.resolve("minimal-01.png"), expectedImage);
        OpenAiStylingImageClient client = new OpenAiStylingImageClient(
                builder.build(), properties, builtInFallback(tempDirectory)
        );

        byte[] image = client.generate(input());

        assertThat(image).isEqualTo(expectedImage);
        server.verify();
    }

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
        OpenAiStylingImageClient client = new OpenAiStylingImageClient(
                builder.build(), properties, builtInFallback(Path.of("generated-stylings"))
        );

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

    @Test
    @DisplayName("입력 이미지 한도가 0이면 일반 이미지 생성 API로 자동 전환한다")
    void fallBackToImageGenerationWhenInputImageLimitIsUnavailable() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("test-key");
        properties.setImageModel("gpt-image-2");
        properties.setImageSize("1024x1024");
        properties.setImageQuality("medium");
        OpenAiStylingImageClient client = new OpenAiStylingImageClient(
                builder.build(), properties, builtInFallback(Path.of("generated-stylings"))
        );

        byte[] expectedImage = "fallback-png".getBytes(StandardCharsets.UTF_8);
        String successResponse = "{\"data\":[{\"b64_json\":\""
                + Base64.getEncoder().encodeToString(expectedImage)
                + "\"}]}";
        String limitResponse = """
                {"error":{"message":"Rate limit reached for input-images per min: Limit 0, Requested 1",
                "type":"input-images","code":"rate_limit_exceeded"}}
                """;
        byte[] referenceImage = "fake-reference".getBytes(StandardCharsets.UTF_8);

        server.expect(once(), requestTo("https://example.com/selected.png"))
                .andRespond(withSuccess(referenceImage, MediaType.IMAGE_PNG));
        server.expect(once(), requestTo("https://example.com/recommended.png"))
                .andRespond(withSuccess(referenceImage, MediaType.IMAGE_PNG));
        server.expect(once(), requestTo("https://api.openai.com/v1/images/edits"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(limitResponse));
        server.expect(once(), requestTo("https://api.openai.com/v1/images/generations"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andRespond(withSuccess(successResponse, MediaType.APPLICATION_JSON));

        byte[] image = client.generate(input());

        assertThat(image).isEqualTo(expectedImage);
        server.verify();
    }

    @Test
    @DisplayName("GPT Image 생성도 한도 0이면 서버 기본 코디 이미지로 전환한다")
    void fallBackToBundledImageWhenAllGptImageCallsAreUnavailable() throws Exception {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("test-key");
        properties.setImageModel("gpt-image-2");
        byte[] expectedImage = "bundled-fallback".getBytes(StandardCharsets.UTF_8);
        Path fallbackDirectory = Files.createDirectories(
                tempDirectory.resolve("mannequin-batch-20260816")
        );
        Files.write(fallbackDirectory.resolve("minimal-01.png"), expectedImage);
        OpenAiStylingImageClient client = new OpenAiStylingImageClient(
                builder.build(), properties, builtInFallback(tempDirectory)
        );
        String limitResponse = """
                {"error":{"message":"Rate limit reached for input-images per min: Limit 0, Requested 1",
                "type":"input-images","code":"rate_limit_exceeded"}}
                """;
        byte[] referenceImage = "fake-reference".getBytes(StandardCharsets.UTF_8);

        server.expect(once(), requestTo("https://example.com/selected.png"))
                .andRespond(withSuccess(referenceImage, MediaType.IMAGE_PNG));
        server.expect(once(), requestTo("https://example.com/recommended.png"))
                .andRespond(withSuccess(referenceImage, MediaType.IMAGE_PNG));
        server.expect(once(), requestTo("https://api.openai.com/v1/images/edits"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(limitResponse));
        server.expect(once(), requestTo("https://api.openai.com/v1/images/generations"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(limitResponse));

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

    private BuiltInStylingImageFallback builtInFallback(Path storageDirectory) {
        StylingImageProperties properties = new StylingImageProperties();
        properties.setStorageDirectory(storageDirectory.toString());
        return new BuiltInStylingImageFallback(properties);
    }
}
