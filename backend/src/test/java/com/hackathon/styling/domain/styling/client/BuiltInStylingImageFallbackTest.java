package com.hackathon.styling.domain.styling.client;

import com.hackathon.styling.domain.styling.storage.StylingImageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BuiltInStylingImageFallbackTest {

    @TempDir
    Path tempDirectory;

    @Test
    void loadsConsistentY2kImageForRequestedVariant() throws Exception {
        Path fallbackDirectory = Files.createDirectories(
                tempDirectory.resolve("mannequin-batch-20260816")
        );
        byte[] expected = new byte[]{1, 2, 3};
        Files.write(fallbackDirectory.resolve("y2k-02-consistent.png"), expected);
        StylingImageProperties properties = new StylingImageProperties();
        properties.setStorageDirectory(tempDirectory.toString());

        byte[] actual = new BuiltInStylingImageFallback(properties).load(input("Y2K", 2));

        assertThat(actual).isEqualTo(expected);
    }

    private StylingImageInput input(String mood, int variant) {
        StylingImageInput.ImageProduct selected = new StylingImageInput.ImageProduct(
                1L, "백팩", "BACKPACK", "Black", "검정 백팩", "https://example.com/a.png", ""
        );
        return new StylingImageInput(
                "Y2K LOOK", "", "", mood, List.of(), variant, 4, selected, List.of()
        );
    }
}
