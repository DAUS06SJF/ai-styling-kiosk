package com.hackathon.styling.domain.styling.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "styling.images")
public class StylingImageProperties {

    private String storageDirectory = "./generated-stylings";
    private String publicBaseUrl = "http://localhost:8080/generated-stylings";
}
