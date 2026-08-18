package com.hackathon.styling.domain.styling.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.hackathon.styling.domain.styling.config.OpenAiProperties;
import com.hackathon.styling.global.error.BusinessException;
import com.hackathon.styling.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiStylingImageClient implements StylingImageClient {

    private static final int MAX_REFERENCE_IMAGE_BYTES = 20 * 1024 * 1024;

    private final RestClient openAiRestClient;
    private final OpenAiProperties properties;

    @Override
    public byte[] generate(StylingImageInput input) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new BusinessException(ErrorCode.OPENAI_NOT_CONFIGURED,
                    "OPENAI_API_KEY 환경변수를 설정한 뒤 다시 요청해 주세요.");
        }

        try {
            List<ReferenceImage> references = downloadReferences(input);
            JsonNode response = openAiRestClient.post()
                    .uri("/v1/images/edits")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(createMultipartBody(input, references))
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
        } catch (RestClientResponseException exception) {
            HttpHeaders responseHeaders = exception.getResponseHeaders();
            String requestId = responseHeaders == null
                    ? ""
                    : responseHeaders.getFirst("x-request-id");
            log.warn("OpenAI Image API call failed: status={}, requestId={}, details={}",
                    exception.getStatusCode().value(),
                    requestId,
                    summarizeError(exception.getResponseBodyAsString()));
            throw new BusinessException(ErrorCode.STYLING_GENERATION_FAILED,
                    "OpenAI 코디 이미지 생성에 실패했습니다. 잠시 후 다시 시도해 주세요.");
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

    private String summarizeError(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "empty response";
        }
        String normalized = responseBody.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 700
                ? normalized
                : normalized.substring(0, 700);
    }

    private List<ReferenceImage> downloadReferences(StylingImageInput input) {
        List<StylingImageInput.ImageProduct> products = new ArrayList<>();
        products.add(input.selectedProduct());
        products.addAll(input.recommendedProducts());

        List<ReferenceImage> references = new ArrayList<>(products.size());
        for (int index = 0; index < products.size(); index++) {
            StylingImageInput.ImageProduct product = products.get(index);
            URI imageUri = validatedImageUri(product);
            ResponseEntity<byte[]> response = openAiRestClient.get()
                    .uri(imageUri)
                    .retrieve()
                    .toEntity(byte[].class);
            byte[] imageBytes = response.getBody();
            if (imageBytes == null || imageBytes.length == 0 || imageBytes.length > MAX_REFERENCE_IMAGE_BYTES) {
                throw new BusinessException(ErrorCode.STYLING_GENERATION_FAILED,
                        "상품 원본 이미지를 AI 참조 이미지로 사용할 수 없습니다.");
            }
            MediaType mediaType = resolveImageMediaType(response.getHeaders().getContentType(), imageBytes);
            String role = index == 0 ? "anchor" : "recommended-" + index;
            references.add(new ReferenceImage(
                    imageBytes,
                    mediaType,
                    role + "-" + product.productId() + imageExtension(mediaType)
            ));
        }
        return references;
    }

    private URI validatedImageUri(StylingImageInput.ImageProduct product) {
        if (product.imageUrl() == null || product.imageUrl().isBlank()) {
            throw new BusinessException(ErrorCode.STYLING_GENERATION_FAILED,
                    "상품 원본 이미지가 등록되지 않았습니다: " + product.name());
        }
        try {
            URI uri = URI.create(product.imageUrl().trim());
            if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))) {
                throw new IllegalArgumentException("Unsupported image URL scheme");
            }
            return uri;
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.STYLING_GENERATION_FAILED,
                    "상품 원본 이미지 URL이 올바르지 않습니다: " + product.name());
        }
    }

    private MediaType resolveImageMediaType(MediaType responseType, byte[] imageBytes) {
        if (responseType != null && "image".equalsIgnoreCase(responseType.getType())) {
            String subtype = responseType.getSubtype().toLowerCase();
            if ("png".equals(subtype)) {
                return MediaType.IMAGE_PNG;
            }
            if ("jpeg".equals(subtype) || "jpg".equals(subtype)) {
                return MediaType.IMAGE_JPEG;
            }
            if ("webp".equals(subtype)) {
                return MediaType.parseMediaType("image/webp");
            }
        }
        if (isPng(imageBytes)) {
            return MediaType.IMAGE_PNG;
        }
        if (isJpeg(imageBytes)) {
            return MediaType.IMAGE_JPEG;
        }
        if (isWebp(imageBytes)) {
            return MediaType.parseMediaType("image/webp");
        }
        throw new BusinessException(ErrorCode.STYLING_GENERATION_FAILED,
                "상품 원본 이미지 형식을 확인할 수 없습니다.");
    }

    private MultiValueMap<String, Object> createMultipartBody(
            StylingImageInput input,
            List<ReferenceImage> references
    ) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("model", properties.getImageModel());
        parts.add("prompt", createPrompt(input));
        parts.add("size", properties.getImageSize());
        parts.add("quality", properties.getImageQuality());
        parts.add("n", "1");

        references.forEach(reference -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(reference.mediaType());
            parts.add("image[]", new HttpEntity<>(
                    new NamedByteArrayResource(reference.bytes(), reference.fileName()),
                    headers
            ));
        });
        return parts;
    }

    private String createPrompt(StylingImageInput input) {
        StringBuilder prompt = new StringBuilder("""
                Create one polished, photorealistic full-body fashion styling image for a retail kiosk.
                Dress a completely faceless, featureless full-body mannequin and keep the entire mannequin visible.
                Use a clean neutral studio background, balanced editorial composition, and soft realistic lighting.
                Reference image 1 is the locked anchor product selected by the customer. Reproduce that exact product
                without redesigning it: preserve its shape, material, color, proportions, pattern, hardware, and logo.
                The remaining reference images are the only companion products allowed in this outfit. Represent those
                products faithfully and do not invent, replace, or add any garment, accessory, caption, label, or watermark.
                """);
        prompt.append("\nLook name: ").append(input.lookName());
        prompt.append("\nStyling direction: ").append(input.stylingTip());
        prompt.append("\nOccasion: ").append(input.occasion());
        prompt.append("\nMood: ").append(input.mood());
        prompt.append("\nPreferred colors: ").append(String.join(", ", input.preferredColors()));
        prompt.append("\nThis is distinct outfit variant ")
                .append(input.variantIndex()).append(" of ").append(input.variantCount()).append(".");
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
                .append("productId=").append(product.productId())
                .append("; name=").append(product.name()).append("; category=").append(product.category())
                .append("; color=").append(product.color())
                .append("; description=").append(product.description());
        if (product.recommendationReason() != null && !product.recommendationReason().isBlank()) {
            prompt.append("; styling role=").append(product.recommendationReason());
        }
    }

    private boolean isPng(byte[] bytes) {
        return bytes.length >= 8
                && bytes[0] == (byte) 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47;
    }

    private boolean isJpeg(byte[] bytes) {
        return bytes.length >= 3
                && bytes[0] == (byte) 0xFF
                && bytes[1] == (byte) 0xD8
                && bytes[2] == (byte) 0xFF;
    }

    private boolean isWebp(byte[] bytes) {
        return bytes.length >= 12
                && bytes[0] == 'R'
                && bytes[1] == 'I'
                && bytes[2] == 'F'
                && bytes[3] == 'F'
                && bytes[8] == 'W'
                && bytes[9] == 'E'
                && bytes[10] == 'B'
                && bytes[11] == 'P';
    }

    private String imageExtension(MediaType mediaType) {
        return switch (mediaType.getSubtype().toLowerCase()) {
            case "jpeg", "jpg" -> ".jpg";
            case "webp" -> ".webp";
            default -> ".png";
        };
    }

    private record ReferenceImage(byte[] bytes, MediaType mediaType, String fileName) {
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {

        private final String fileName;

        private NamedByteArrayResource(byte[] byteArray, String fileName) {
            super(byteArray);
            this.fileName = fileName;
        }

        @Override
        public String getFilename() {
            return fileName;
        }
    }
}
