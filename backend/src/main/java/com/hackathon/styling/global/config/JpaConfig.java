package com.hackathon.styling.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** BaseTimeEntity 의 @CreatedDate / @LastModifiedDate 를 동작시키기 위한 설정. */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
