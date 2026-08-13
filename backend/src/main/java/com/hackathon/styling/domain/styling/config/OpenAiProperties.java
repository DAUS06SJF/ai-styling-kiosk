package com.hackathon.styling.domain.styling.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {

    private String apiKey = "";
    private String model = "gpt-5.6-luna";
    private String baseUrl = "https://api.openai.com";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(30);

    @Min(3)
    @Max(50)
    private int candidateLimit = 24;

    @Min(1)
    @Max(5)
    private int recommendationCount = 3;
}
