package com.snowresorts.activity.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Direct base URL for user-service (not via the public gateway).
 *
 * <pre>
 * snow:
 *   user-service:
 *     base-url: ${USER_SERVICE_BASE_URL:http://localhost:8082}
 * </pre>
 */
@ConfigurationProperties(prefix = "snow.user-service")
public record UserServiceProperties(String baseUrl) {

    public UserServiceProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8082";
        }
    }
}
