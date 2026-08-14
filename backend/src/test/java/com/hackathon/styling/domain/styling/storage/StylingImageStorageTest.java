package com.hackathon.styling.domain.styling.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class StylingImageStorageTest {

    @TempDir
    Path tempDirectory;

    @Test
    void storeImageAndReturnPublicUrl() throws Exception {
        StylingImageProperties properties = new StylingImageProperties();
        properties.setStorageDirectory(tempDirectory.toString());
        properties.setPublicBaseUrl("http://localhost:8080/generated-stylings/");
        StylingImageStorage storage = new StylingImageStorage(properties);
        storage.initialize();

        String url = storage.store(new byte[]{1, 2, 3});
        String fileName = url.substring(url.lastIndexOf('/') + 1);

        assertThat(url).startsWith("http://localhost:8080/generated-stylings/");
        assertThat(Files.readAllBytes(tempDirectory.resolve(fileName))).containsExactly(1, 2, 3);
    }
}
