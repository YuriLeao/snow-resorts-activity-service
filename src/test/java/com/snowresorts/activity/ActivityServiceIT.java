package com.snowresorts.activity;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration test for activity-service. Compile-only in this environment (Docker is unavailable);
 * it exercises the full Spring context + Flyway against a Postgres (PostGIS image) Testcontainer.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class ActivityServiceIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("imresamu/postgis:16-3.4").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("snow_resorts")
            .withUsername("snow")
            .withPassword("snow");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /runs/start without a JWT is rejected with 401")
    void startRun_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/snow-resort-service/v1/runs/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /runs/start with a valid JWT starts an ACTIVE run (201)")
    void startRun_withJwt_returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(post("/snow-resort-service/v1/runs/start")
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"resortId":"%s"}""".formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.runId", notNullValue()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
