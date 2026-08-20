package com.hackathon.styling.domain.styling.client;

import com.hackathon.styling.domain.styling.storage.StylingImageProperties;
import com.hackathon.styling.global.error.BusinessException;
import com.hackathon.styling.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuiltInStylingImageFallback {

    private static final String FALLBACK_DIRECTORY = "mannequin-batch-20260816";

    private final StylingImageProperties properties;

    public byte[] load(StylingImageInput input) {
        String style = resolveStyle(input.mood(), input.lookName());
        int variant = Math.max(1, Math.min(4, input.variantIndex()));
        String fileName = fileName(style, variant);
        Path storageDirectory = Path.of(properties.getStorageDirectory()).toAbsolutePath().normalize();
        Path imagePath = storageDirectory.resolve(FALLBACK_DIRECTORY).resolve(fileName).normalize();

        if (!imagePath.startsWith(storageDirectory)) {
            throw fallbackUnavailable();
        }
        try {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            if (imageBytes.length == 0) {
                throw fallbackUnavailable();
            }
            log.info("Using bundled styling image fallback: style={}, variant={}", style, variant);
            return imageBytes;
        } catch (IOException exception) {
            log.warn("Bundled styling image fallback is unavailable: {}", imagePath.getFileName());
            throw fallbackUnavailable();
        }
    }

    public String publicUrl(StylingImageInput input) {
        String style = resolveStyle(input.mood(), input.lookName());
        int variant = Math.max(1, Math.min(4, input.variantIndex()));
        return publicUrl(style, variant);
    }

    public String publicUrl(String style, int variant) {
        String normalizedStyle = resolveStyle(style, "");
        int normalizedVariant = Math.max(1, Math.min(4, variant));
        return stripTrailingSlash(properties.getPublicBaseUrl())
                + "/" + FALLBACK_DIRECTORY
                + "/" + fileName(normalizedStyle, normalizedVariant);
    }

    private String resolveStyle(String mood, String lookName) {
        String value = ((mood == null ? "" : mood) + " " + (lookName == null ? "" : lookName))
                .toLowerCase(Locale.ROOT);
        if (value.contains("y2k")) {
            return "y2k";
        }
        if (value.contains("luxury") || value.contains("럭셔리")) {
            return "luxury";
        }
        if (value.contains("street") || value.contains("스트리트")) {
            return "street";
        }
        if (value.contains("vintage") || value.contains("빈티지")) {
            return "vintage";
        }
        return "minimal";
    }

    private String fileName(String style, int variant) {
        return "%s-%02d-consistent.png".formatted(style, variant);
    }

    private BusinessException fallbackUnavailable() {
        return new BusinessException(ErrorCode.STYLING_GENERATION_FAILED,
                "OpenAI 이미지 생성 한도가 제한되었고 임시 코디 이미지도 사용할 수 없습니다.");
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
