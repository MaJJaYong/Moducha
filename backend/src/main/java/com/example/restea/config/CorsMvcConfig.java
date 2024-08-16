package com.example.restea.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

    @Value("${cors.url}")
    private String corsURL;

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        String[] allowedOrigins = Arrays.stream(corsURL.split(","))
                .map(String::trim)
                .toArray(String[]::new);

        corsRegistry.addMapping("/**")
                .exposedHeaders("Set-Cookie")
                .allowedOrigins(allowedOrigins);
    }
}