# Policy Billing System

A microservice-based policy billing and collections system that manages recurring premiums, payment processing, retry logic, and delinquency tracking through event-driven architecture.

---

## Quick Start

### Prerequisites
- **Docker** and **Docker Compose** for containerized infrastructure.
- **Java 21** for modern language features and performance.
- **Maven 3.9+** for building and managing dependencies.

### Running the System

1. **Clone and setup**:
   ```bash
   git clone <repository>
   cd policy-billing-system
   ```

2. **Start infrastructure only** (for local development):
   ```bash
   docker-compose up mariadb kafka zookeeper redis -d
   ```

3. **Start all services**:
   ```bash
   docker-compose up --build
   ```

4. **Verify services are running**:
   ```bash
   curl http://localhost:8081/actuator/health  # Policy Service
   curl http://localhost:8082/actuator/health  # Billing Service  
   curl http://localhost:8083/actuator/health  # Payment Service
   curl http://localhost:8084/actuator/health  # Notification Service
   curl http://localhost:8090/actuator/health  # Payment Gateway Mock
   ```

---

## Development Workflow

### Local Development

1. **Start infrastructure**:
   ```bash
   docker-compose up mariadb kafka zookeeper redis -d
   ```

2. **Run services locally** (in separate terminals):
   ```bash
   mvn clean install
   cd policy-service && mvn spring-boot:run -Dspring.profiles.active=dev
   cd billing-service && mvn spring-boot:run -Dspring.profiles.active=dev  
   cd payment-service && mvn spring-boot:run -Dspring.profiles.active=dev
   cd notification-service && mvn spring-boot:run -Dspring.profiles.active=dev
   ```

3. **Or run from IDE**:
   - Import the project into your IDE (e.g., IntelliJ IDEA or Eclipse).
   - Configure Spring Boot run configurations for each service with the `dev` profile.

---

## Architecture

### Services Overview

| Service               | Port  | Responsibility                                      |
|-----------------------|-------|----------------------------------------------------|
| **Policy Service**    | 8081  | Manages policy metadata and premium schedules.     |
| **Billing Service**   | 8082  | Handles premium calculations and billing cycles.   |
| **Payment Service**   | 8083  | Processes payments and manages retry logic.        |
| **Notification Service** | 8084 | Sends payment reminders and notifications.         |
| **Payment Gateway Mock** | 8090 | Simulates a third-party payment provider.          |

### Infrastructure Components

| Component   | Port  | Purpose                                      |
|-------------|-------|----------------------------------------------|
| **MariaDB** | 3306  | Primary database for persistent storage.     |
| **Kafka**   | 9092  | Event streaming for inter-service messaging. |
| **Zookeeper** | 2181 | Coordinates Kafka brokers.                  |
| **Redis**   | 6379  | Caching and session management.              |

---

## Event-Driven Architecture

### Kafka Topics

The system uses Kafka for inter-service communication. Below are the key topics:

| Topic Name                  | Description                                      |
|-----------------------------|--------------------------------------------------|
| `billing.policy.created`    | Triggered when a new policy is created.          |
| `billing.premium.calculated`| Triggered after premium calculation.             |
| `billing.payment.attempted` | Triggered when a payment attempt is made.        |
| `billing.payment.succeeded` | Triggered when a payment is successful.          |
| `billing.payment.failed`    | Triggered when a payment fails.                  |
| `billing.retry.scheduled`   | Triggered when a retry is scheduled.             |
| `billing.notification.requested` | Triggered to request a notification.        |
| `billing.policy.delinquent` | Triggered when a policy becomes delinquent.      |

### Event Flow

1. **Policy Creation**:
   - Policy Service → `billing.policy.created` → Billing Service.

2. **Payment Processing**:
   - Payment Service → `billing.payment.attempted` → Notification Service.

3. **Failed Payment**:
   - Payment Service → `billing.payment.failed` → Billing Service (retry logic).

4. **Retry Scheduling**:
   - Billing Service → `billing.retry.scheduled` → Payment Service.

---

## Developer Tips

### Debugging

1. **Check Logs**:
   - Use `docker-compose logs <service>` to view logs for a specific service.
   - Example:
     ```bash
     docker-compose logs payment-service
     ```

2. **Enable Debug Mode**:
   - Add `-Ddebug` to Spring Boot run commands for detailed logs.

3. **Inspect Kafka Messages**:
   - Use tools like `kafkacat` or Kafka UI to inspect messages in Kafka topics.

### Testing Locally

1. **Unit Tests**:
   - Run with:
     ```bash
     mvn test
     ```

2. **Integration Tests**:
   - Use TestContainers for database and Kafka integration.
   - Run with:
     ```bash
     mvn verify -Pintegration
     ```

3. **End-to-End Tests**:
   - Use Docker Compose to spin up the full system and test user journeys.
   - Run with:
     ```bash
     mvn verify -Pe2e
     ```

---

## API Endpoints

### Payment Service (Port 8083)

| Endpoint                          | Method | Description                              |
|-----------------------------------|--------|------------------------------------------|
| `/api/payments/hello`             | GET    | Health check.                            |
| `/api/payments/attempt`           | POST   | Process a payment.                       |
| `/api/payments/{paymentId}/status`| GET    | Get the status of a payment.             |
| `/api/payments/{paymentId}/retry` | POST   | Retry a failed payment.                  |
| `/api/payments/delinquent`        | GET    | List delinquent policies.                |

---

## Testing Strategy

### Test Types

1. **Unit Tests**:
   - Test individual components in isolation.
   - Use `@WebMvcTest` for controllers and mock dependencies.

2. **Integration Tests**:
   - Test database operations and Kafka messaging with TestContainers.

3. **End-to-End Tests**:
   - Test complete workflows using Docker Compose.

### Running Tests

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify -Pintegration

# Run end-to-end tests
mvn verify -Pe2e
```

---

## Development Guidelines

### Adding New Endpoints

1. Define the endpoint in the appropriate controller.
2. Add unit tests using `@WebMvcTest`.
3. Add integration tests for database or messaging logic.
4. Update the API documentation in this README.

### Adding New Kafka Events

1. Define the event schema in `shared-models`.
2. Add a producer in the source service.
3. Add a consumer in the target service.
4. Write integration tests for the event flow.

---

## Monitoring and Metrics

### Health Checks

All services expose health checks at `/actuator/health`:

```bash
curl http://localhost:8083/actuator/health
```

### Metrics

Metrics are available at `/actuator/metrics`:

```bash
curl http://localhost:8083/actuator/metrics
```

---

## Troubleshooting

### Common Issues

1. **Database Connection Issues**:
   - Verify MariaDB is running:
     ```bash
     docker-compose ps mariadb
     ```
   - Check logs:
     ```bash
     docker-compose logs mariadb
     ```

2. **Kafka Connection Issues**:
   - Verify Kafka is running:
     ```bash
     docker-compose ps kafka
     ```
   - Check logs:
     ```bash
     docker-compose logs kafka
     ```

3. **Service Not Starting**:
   - Check if the port is already in use:
     ```bash
     netstat -tulpn | grep :8083
     ```

---

## Next Steps

- [ ] Implement JWT-based authentication for all services.
- [ ] Add database migrations with Flyway.
- [ ] Enhance retry logic with configurable backoff strategies.
- [ ] Add support for multiple notification channels (e.g., SMS, email).
- [ ] Implement advanced monitoring with Prometheus and Grafana.