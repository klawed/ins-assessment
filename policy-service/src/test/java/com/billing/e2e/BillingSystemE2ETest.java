package com.billing.e2e;

import com.billing.policy.PolicyServiceApplication;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;
import java.io.File;
import java.time.Duration;
import java.util.Map;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.containers.wait.strategy.Wait;
@SpringBootTest(
    classes = PolicyServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.flyway.enabled=false"
    }
)
@Testcontainers
class BillingSystemE2ETest {

    private static final Logger log = LoggerFactory.getLogger(BillingSystemE2ETest.class);

    @Container
    private static final DockerComposeContainer<?> environment = new DockerComposeContainer<>(
            new File("../docker-compose.yml"))
            .withLocalCompose(true)
            .withPull(false)
            .withExposedService("mariadb", 3306)
            .withExposedService("policy-service", 8080)
            .withExposedService("billing-service", 8080)
            .withExposedService("payment-service", 8080)
            .withExposedService("notification-service", 8080)
            .withEnv("COMPOSE_PROJECT_NAME", "billing-test")
            // Add wait strategies for all services
            .waitingFor("mariadb", 
                Wait.forLogMessage(".*ready for connections.*\\n", 1)
                    .withStartupTimeout(Duration.ofMinutes(2)))
            .waitingFor("policy-service",
                Wait.forLogMessage(".*Started PolicyServiceApplication.*\\n", 1)
                    .withStartupTimeout(Duration.ofMinutes(2)))
            .waitingFor("billing-service",
                Wait.forLogMessage(".*Started BillingServiceApplication.*\\n", 1)
                    .withStartupTimeout(Duration.ofMinutes(2)))
            .waitingFor("payment-service",
                Wait.forLogMessage(".*Started PaymentServiceApplication.*\\n", 1)
                    .withStartupTimeout(Duration.ofMinutes(2)))
            .waitingFor("notification-service",
                Wait.forLogMessage(".*Started NotificationServiceApplication.*\\n", 1)
                    .withStartupTimeout(Duration.ofMinutes(2)))
            .withLogConsumer("mariadb", new Slf4jLogConsumer(log))
            .withLogConsumer("policy-service", new Slf4jLogConsumer(log))
            .withLogConsumer("billing-service", new Slf4jLogConsumer(log))
            .withLogConsumer("payment-service", new Slf4jLogConsumer(log))
            .withLogConsumer("notification-service", new Slf4jLogConsumer(log));

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.driver-class-name", 
            () -> "org.mariadb.jdbc.Driver");
        registry.add("spring.datasource.url", () -> 
            String.format("jdbc:mariadb://%s:%d/billing_system?allowPublicKeyRetrieval=true&useSSL=false",
                environment.getServiceHost("mariadb", 3306),
                environment.getServicePort("mariadb", 3306)));
        registry.add("spring.datasource.username", () -> "billing_user");
        registry.add("spring.datasource.password", () -> "billing_password");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.datasource.hikari.initializationFailTimeout", () -> "60000");
        registry.add("spring.datasource.hikari.connectionTimeout", () -> "30000");
        registry.add("testcontainers.reuse.enable", () -> "true");
    }

    @BeforeEach
    void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void shouldVerifyAllServicesAreHealthy() {
        // Test Policy Service
        given()
            .when()
                .get("http://localhost:" + environment.getServicePort("policy-service", 8080) + "/actuator/health")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"));

        // Test Billing Service  
        given()
            .when()
                .get("http://localhost:" + environment.getServicePort("billing-service", 8080) + "/actuator/health")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"));

        // Test Payment Service
        given()
            .when()
                .get("http://localhost:" + environment.getServicePort("payment-service", 8080) + "/actuator/health")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"));

        // Test Notification Service
        given()
            .when()
                .get("http://localhost:" + environment.getServicePort("notification-service", 8080) + "/actuator/health")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    void shouldTestBasicPolicyWorkflow() {
        int policyServicePort = environment.getServicePort("policy-service", 8080);
        String policyId = "E2E-POLICY-001";

        // Test Policy Service hello endpoint
        given()
            .when()
                .get("http://localhost:" + policyServicePort + "/api/policies/hello")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("service", equalTo("policy-service"))
                .body("status", equalTo("UP"));

        // Test Policy retrieval
        given()
            .when()
                .get("http://localhost:" + policyServicePort + "/api/policies/" + policyId)
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("policyId", equalTo(policyId))
                .body("status", equalTo("ACTIVE"));

        // Test Policy schedule
        given()
            .when()
                .get("http://localhost:" + policyServicePort + "/api/policies/" + policyId + "/schedule")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("policyId", equalTo(policyId));
    }

    @Test
    void shouldTestCrossServiceCommunication() {
        // This test will be expanded when we implement actual business logic
        // For now, just verify that services can be reached independently
        
        int policyPort = environment.getServicePort("policy-service", 8080);
        int billingPort = environment.getServicePort("billing-service", 8080);
        int paymentPort = environment.getServicePort("payment-service", 8080);

        // Verify all services respond to hello endpoints
        given().get("http://localhost:" + policyPort + "/api/policies/hello")
               .then().statusCode(200);
               
        given().get("http://localhost:" + billingPort + "/api/billing/hello")
               .then().statusCode(200);
               
        given().get("http://localhost:" + paymentPort + "/api/payments/hello")
               .then().statusCode(200);
    }

    @Test
    void shouldStartAllServices() {
        // Check if MariaDB is running and accessible
        assertThat(environment.getServicePort("mariadb", 3306)).isPositive();
        
        // Verify all required services are exposed
        assertThat(environment.getServicePort("policy-service", 8080)).isPositive();
        assertThat(environment.getServicePort("billing-service", 8080)).isPositive();
        assertThat(environment.getServicePort("payment-service", 8080)).isPositive();
        assertThat(environment.getServicePort("notification-service", 8080)).isPositive();
    }

    @Test
    void healthCheckShouldReturnUp() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "/actuator/health",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "UP");
    }
}