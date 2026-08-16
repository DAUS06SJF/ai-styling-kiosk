package com.hackathon.styling.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 키오스크·관리자·모바일 3개 프론트가 각각 다른 포트에서 붙기 때문에 CORS 허용이 필요하다.
 * 허용 주소는 application.yml 의 cors.allowed-origins 에서 관리한다.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public CorsConfig(@Value("${cors.allowed-origins}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        addFrontendCors(registry, "/api/**");
        addFrontendCors(registry, "/admin/**");
    }

    private void addFrontendCors(CorsRegistry registry, String pathPattern) {
        registry.addMapping(pathPattern)
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
