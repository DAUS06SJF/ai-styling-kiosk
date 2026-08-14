package com.hackathon.styling.domain.styling.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class GeneratedStylingImageConfig implements WebMvcConfigurer {

    private final StylingImageStorage stylingImageStorage;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/generated-stylings/**")
                .addResourceLocations(stylingImageStorage.storageDirectory().toUri().toString());
    }
}
