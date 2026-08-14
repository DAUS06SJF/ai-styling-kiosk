package com.hackathon.styling.domain.styling.storage;

import com.hackathon.styling.global.error.BusinessException;
import com.hackathon.styling.global.error.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StylingImageStorage {

    private final StylingImageProperties properties;

    @PostConstruct
    void initialize() {
        try {
            Files.createDirectories(storageDirectory());
        } catch (IOException exception) {
            throw new IllegalStateException("코디 이미지 저장 폴더를 생성할 수 없습니다.", exception);
        }
    }

    public String store(byte[] imageBytes) {
        String fileName = UUID.randomUUID() + ".png";
        try {
            Files.write(
                    storageDirectory().resolve(fileName),
                    imageBytes,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.STYLING_GENERATION_FAILED,
                    "AI 코디 이미지를 저장하지 못했습니다.");
        }
        return stripTrailingSlash(properties.getPublicBaseUrl()) + "/" + fileName;
    }

    public Path storageDirectory() {
        return Path.of(properties.getStorageDirectory()).toAbsolutePath().normalize();
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
