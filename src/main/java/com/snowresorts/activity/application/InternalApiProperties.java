package com.snowresorts.activity.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "snow.internal-api")
public record InternalApiProperties(String secret, String header) {

    public InternalApiProperties {
        if (secret == null || secret.isBlank()) {
            secret = "dev-internal-secret";
        }
        if (header == null || header.isBlank()) {
            header = "X-Internal-Secret";
        }
    }
}
