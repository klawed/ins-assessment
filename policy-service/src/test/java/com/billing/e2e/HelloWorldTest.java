import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HelloWorldTest {

    private static final DockerComposeContainer<?> composeContainer =
            new DockerComposeContainer<>(new File("../docker-compose.yml"))
                    .withExposedService("notification-service", 8080);

    @Test
    void testContainerStartup() {
        composeContainer.start();
        boolean isNotificationServiceRunning = composeContainer.getServicePort("notification-service", 8080) > 0;
        assertTrue(isNotificationServiceRunning, "Notification service should be running");
        composeContainer.stop();
    }
}