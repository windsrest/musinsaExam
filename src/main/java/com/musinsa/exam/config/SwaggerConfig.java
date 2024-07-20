package com.musinsa.exam.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI musinsaOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Musinsa Exam API")
                        .description("무신사 과제 API 문서")
                        .version("v0.0.1"));
    }
}