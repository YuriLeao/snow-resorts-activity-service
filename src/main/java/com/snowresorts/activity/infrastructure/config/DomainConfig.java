package com.snowresorts.activity.infrastructure.config;

import com.snowresorts.activity.domain.metrics.MetricsCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the framework-free domain components into the Spring context. */
@Configuration(proxyBeanMethods = false)
public class DomainConfig {

    @Bean
    MetricsCalculator metricsCalculator() {
        return new MetricsCalculator();
    }
}
