package com.billing.policy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class PolicyServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.0")
            .withDatabaseName("billing_system")
            .withUsername("billing_user")
            .withPassword("billing_password")
            .withReuse(true)  // Enable container reuse
            .withLabel("reuse.UUID", "mariadb-test");  // Unique identifier for reuse

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withReuse(true)  // Enable container reuse
            .withLabel("reuse.UUID", "kafka-test");  // Unique identifier for reuse

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Test
    void shouldStartApplicationSuccessfully() {
        // Test that application context loads and basic endpoints work
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/policies/hello", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("service")).isEqualTo("policy-service");
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }

    @Test
    void healthCheckShouldReturnUp() {
        // Given
        String url = "http://localhost:" + port + "/actuator/health";

        // When
        ResponseEntity<Map<String, Object>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }

    @Test
    void shouldHandlePolicyRequests() {
        String policyId = "TEST-POLICY-123";

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/policies/" + policyId, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("policyId")).isEqualTo(policyId);
    }

    @Test
    void shouldHandlePolicyScheduleRequests() {
        String policyId = "TEST-POLICY-123";

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/policies/" + policyId + "/schedule", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("policyId")).isEqualTo(policyId);
    }
}