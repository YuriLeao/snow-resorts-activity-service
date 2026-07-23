package com.snowresorts.activity.infrastructure.user;

import com.snowresorts.activity.application.InternalApiProperties;
import com.snowresorts.activity.application.UserServiceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({UserServiceProperties.class, InternalApiProperties.class})
public class UserServiceClientConfig {

    @Bean
    RestClient userServiceRestClient(RestClient.Builder restClientBuilder, UserServiceProperties properties) {
        return restClientBuilder
                .baseUrl(properties.baseUrl())
                .build();
    }
}
