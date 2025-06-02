// BillingSystemE2ETest.java
package com.billing.e2e;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll; // Changed from BeforeEach for base URLs
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;
// import java.util.Map; // Only if needed for a direct HTTP client response

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Testcontainers
@ActiveProfiles("test")
@ActiveProfiles("test")
class BillingSystemE2ETest {

    private static final Logger log = LoggerFactory.getLogger(BillingSystemE2ETest.class);
    private static final Duration SERVICE_STARTUP_TIMEOUT = Duration.ofMinutes(8); 

    // Define service names as constants for clarity
    private static final String MARIADB_SERVICE_NAME = "mariadb";
    private static final int MARIADB_PORT = 3306;
    private static final String POLICY_SERVICE_NAME = "policy-service";
    private static final String BILLING_SERVICE_NAME = "billing-service";
    private static final String PAYMENT_SERVICE_NAME = "payment-service";
    private static final String NOTIFICATION_SERVICE_NAME = "notification-service";
    private static final int APP_PORT = 8080; // Assuming all your Spring apps use this internal port

        @Container
    private static final DockerComposeContainer<?> environment = new DockerComposeContainer<>(
            new File("../docker-compose.yml"),
            new File("../docker-compose-e2e.yml"))
            .withLocalCompose(true)
            .withPull(false)
            // Wait for MariaDB first using its log message
            .withExposedService(MARIADB_SERVICE_NAME, MARIADB_PORT, // Using actual service name from compose
                    Wait.forLogMessage(".*mariadbd: ready for connections.*\\n", 1)
                            .withStartupTimeout(Duration.ofMinutes(8))) // Can be shorter if MariaDB is fast
            // Then define waits for your application services
            .withExposedService(POLICY_SERVICE_NAME, APP_PORT,
                    Wait.forHttp("/actuator/health").forStatusCode(200)
                            .withStartupTimeout(SERVICE_STARTUP_TIMEOUT)) // 3 minutes
            .withExposedService(BILLING_SERVICE_NAME, APP_PORT,
                    Wait.forHttp("/actuator/health").forStatusCode(200)
                            .withStartupTimeout(SERVICE_STARTUP_TIMEOUT))
            .withExposedService(PAYMENT_SERVICE_NAME, APP_PORT,
                    Wait.forHttp("/actuator/health").forStatusCode(200)
                            .withStartupTimeout(SERVICE_STARTUP_TIMEOUT))
            .withExposedService(NOTIFICATION_SERVICE_NAME, APP_PORT, // This is the one failing socat
                    Wait.forHttp("/actuator/health").forStatusCode(200)
                            .withStartupTimeout(SERVICE_STARTUP_TIMEOUT))
                    .withLogConsumer(MARIADB_SERVICE_NAME, new Slf4jLogConsumer(log).withPrefix(MARIADB_SERVICE_NAME))
            .withLogConsumer(POLICY_SERVICE_NAME, new Slf4jLogConsumer(log).withPrefix(POLICY_SERVICE_NAME))
            .withLogConsumer(BILLING_SERVICE_NAME, new Slf4jLogConsumer(log).withPrefix(BILLING_SERVICE_NAME))
            .withLogConsumer(PAYMENT_SERVICE_NAME, new Slf4jLogConsumer(log).withPrefix(PAYMENT_SERVICE_NAME))
            .withLogConsumer(NOTIFICATION_SERVICE_NAME,
                    new Slf4jLogConsumer(log).withPrefix(NOTIFICATION_SERVICE_NAME));
    // .withOptions("--compatibility"); // Add if your compose file uses v2 syntax
    // like `links` not in `depends_on` networks

    // Store base URLs
    private static String policyServiceBaseUrl;
    private static String billingServiceBaseUrl;
    private static String paymentServiceBaseUrl;
    private static String notificationServiceBaseUrl;

    @BeforeAll // Run once after containers are up
    static void setUpServices() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        policyServiceBaseUrl = String.format("http://%s:%d",
                environment.getServiceHost(POLICY_SERVICE_NAME, APP_PORT),
                environment.getServicePort(POLICY_SERVICE_NAME, APP_PORT));

        billingServiceBaseUrl = String.format("http://%s:%d",
                environment.getServiceHost(BILLING_SERVICE_NAME, APP_PORT),
                environment.getServicePort(BILLING_SERVICE_NAME, APP_PORT));

        paymentServiceBaseUrl = String.format("http://%s:%d",
                environment.getServiceHost(PAYMENT_SERVICE_NAME, APP_PORT),
                environment.getServicePort(PAYMENT_SERVICE_NAME, APP_PORT));

        notificationServiceBaseUrl = String.format("http://%s:%d",
                environment.getServiceHost(NOTIFICATION_SERVICE_NAME, APP_PORT),
                environment.getServicePort(NOTIFICATION_SERVICE_NAME, APP_PORT));

        log.info("{} running at: {}", POLICY_SERVICE_NAME, policyServiceBaseUrl);
        log.info("{} running at: {}", BILLING_SERVICE_NAME, billingServiceBaseUrl);
        log.info("{} running at: {}", PAYMENT_SERVICE_NAME, paymentServiceBaseUrl);
        log.info("{} running at: {}", NOTIFICATION_SERVICE_NAME, notificationServiceBaseUrl);
    }

    @Test
    void shouldVerifyAllServicesAreHealthy() {
        given().when().get(policyServiceBaseUrl + "/actuator/health")
                .then().statusCode(200).body("status", equalTo("UP"));

        given().when().get(billingServiceBaseUrl + "/actuator/health")
                .then().statusCode(200).body("status", equalTo("UP"));

        given().when().get(paymentServiceBaseUrl + "/actuator/health")
                .then().statusCode(200).body("status", equalTo("UP"));

        given().when().get(notificationServiceBaseUrl + "/actuator/health")
                .then().statusCode(200).body("status", equalTo("UP"));
    }

    @Test
    void shouldTestBasicPolicyWorkflow() {
        String policyId = "E2E-POLICY-001";

        given().when().get(policyServiceBaseUrl + "/api/policies/hello")
                .then().statusCode(200).contentType(ContentType.JSON)
                .body("service", equalTo("policy-service")).body("status", equalTo("UP"));

        // Assuming your docker-compose policy-service is pre-populated or can create
        // this
        // This might require changes if the policy isn't there by default
        given().when().get(policyServiceBaseUrl + "/api/policies/" + policyId)
                .then().statusCode(200).contentType(ContentType.JSON)
                .body("policyId", equalTo(policyId)).body("status", equalTo("ACTIVE"));

        given().when().get(policyServiceBaseUrl + "/api/policies/" + policyId + "/schedule")
                .then().statusCode(200).contentType(ContentType.JSON)
                .body("policyId", equalTo(policyId));
    }

    @Test
    void shouldTestCrossServiceCommunication() {
        given().get(policyServiceBaseUrl + "/api/policies/hello").then().statusCode(200);
        given().get(billingServiceBaseUrl + "/api/billing/hello").then().statusCode(200);
        given().get(paymentServiceBaseUrl + "/api/payments/hello").then().statusCode(200);
        // Assuming notification-service also has a hello endpoint
        // given().get(notificationServiceBaseUrl +
        // "/api/notifications/hello").then().statusCode(200);
    }

    @Test
    void shouldStartAllServices() {
        assertThat(environment.getServicePort(MARIADB_SERVICE_NAME, MARIADB_PORT)).isPositive();
        assertThat(environment.getServicePort(POLICY_SERVICE_NAME, APP_PORT)).isPositive();
        assertThat(environment.getServicePort(BILLING_SERVICE_NAME, APP_PORT)).isPositive();
        assertThat(environment.getServicePort(PAYMENT_SERVICE_NAME, APP_PORT)).isPositive();
        assertThat(environment.getServicePort(NOTIFICATION_SERVICE_NAME, APP_PORT)).isPositive();
    }

    // This healthCheckShouldReturnUp test was for the @SpringBootTest context.
    // It's redundant now as shouldVerifyAllServicesAreHealthy covers the composed
    // services.
    // You can remove it or adapt it if you specifically want to use a different
    // HTTP client.
    /*
     * @Test
     * void healthCheckShouldReturnUp() {
     * // This test would need to use a generic HTTP client or RestAssured targeting
     * one of the service URLs
     * // Example using RestAssured for policy-service:
     * given()
     * .when()
     * .get(policyServiceBaseUrl + "/actuator/health")
     * .then()
     * .statusCode(200)
     * .body("status", equalTo("UP"));
     * }
     */
}