package com.billing.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Testcontainers
class BillingSystemE2ETest {

    @Container
    static DockerComposeContainer<?> environment = new DockerComposeContainer<>(
            new File("docker-compose.yml"))
            .withExposedService("policy-service", 8080, 
                Wait.forHttp("/actuator/health")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(3)))
            .withExposedService("billing-service", 8080, 
                Wait.forHttp("/actuator/health")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(3)))
            .withExposedService("payment-service", 8080, 
                Wait.forHttp("/actuator/health")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(3)))
            .withExposedService("notification-service", 8080, 
                Wait.forHttp("/actuator/health")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(3)))
            .withExposedService("payment-gateway-mock", 8080, 
                Wait.forHttp("/actuator/health")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(2)));

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
}