# Policy Billing System - Architecture Document

## Overview
A microservice-based policy billing and collections system that manages recurring premiums, payment processing, retry logic, and delinquency tracking through event-driven architecture.

## System Requirements

### Core REST API Requirements
- **Retrieve premium schedule** for a policy
- **Record payment attempt** and its result (success/failure)
- **Return list of delinquent policies**
- **Trigger retry action** for failed payments

### System Requirements
- Event-driven/pub-sub architecture for notifications
- Grace period handling and retry schedule logic
- Extensibility for multiple payment channels
- Integration with third-party payment providers

## Architecture Decisions

### Technology Stack
- **Framework**: Spring Boot 3.2 with WebMVC (not WebFlux - keeping it simple)
- **Database**: MariaDB 11.0 with JPA/Hibernate
- **Messaging**: Apache Kafka for event-driven communication
- **Security**: Spring Security with JWT
- **Testing**: JUnit 5, TestContainers, RestAssured
- **Containerization**: Docker with Docker Compose
- **Build**: Maven multi-module project

### Development Approach
- **Docker-first**: All infrastructure and services run in containers
- **Database**: Real MariaDB (no in-memory databases)
- **Testing Strategy**: Unit → Integration → E2E with TestContainers

## Microservices Architecture

### Service Breakdown

#### 1. Policy Service (`policy-service`)
**Responsibility**: Manages policy metadata and premium schedules
- **Port**: 8081
- **Database**: `policies`, `policy_schedules` tables
- **APIs**:
  - `GET /api/policies/{policyId}`
  - `GET /api/policies/{policyId}/schedule`
  - `POST /api/policies`

#### 2. Billing Service (`billing-service`)  
**Responsibility**: Calculates premiums and manages billing cycles
- **Port**: 8082
- **Database**: `billing_cycles`, `premium_calculations` tables
- **APIs**:
  - `GET /api/billing/{policyId}/premium`
  - `POST /api/billing/calculate`
  - `GET /api/billing/due`

#### 3. Payment Service (`payment-service`)
**Responsibility**: Handles payment processing and retry logic
- **Port**: 8083
- **Database**: `payments`, `payment_attempts`, `retry_schedules` tables
- **APIs**:
  - `POST /api/payments/attempt`
  - `GET /api/payments/{paymentId}/status`
  - `POST /api/payments/{paymentId}/retry`
  - `GET /api/payments/delinquent`

#### 4. Notification Service (`notification-service`)
**Responsibility**: Manages payment reminders and notifications
- **Port**: 8084
- **Database**: `notifications`, `notification_templates` tables
- **APIs**:
  - `POST /api/notifications/send`
  - `GET /api/notifications/{policyId}`

#### 5. Payment Gateway Mock (`payment-gateway-mock`)
**Responsibility**: Simulates third-party payment provider
- **Port**: 8090
- **APIs**:
  - `POST /api/gateway/charge`
  - `GET /api/gateway/status/{transactionId}`

## Event-Driven Architecture

### Kafka Topics

```
billing.policy.created
billing.premium.calculated
billing.payment.attempted
billing.payment.succeeded
billing.payment.failed
billing.retry.scheduled
billing.notification.requested
billing.policy.delinquent
```

### Event Flow Example

1. **Policy Creation**:
   ```
   Policy Service → billing.policy.created → Billing Service
   ```

2. **Payment Processing**:
   ```
   Payment Service → billing.payment.attempted → Notification Service
   Payment Service → billing.payment.failed → Billing Service (retry logic)
   ```

3. **Retry Logic**:
   ```
   Billing Service → billing.retry.scheduled → Payment Service
   ```

## Database Design

### Schema Ownership
- Each microservice owns its database tables
- No direct database access between services
- Communication through events and REST APIs

### Key Tables

#### Policy Service
```sql
-- policies table
id, policy_number, customer_id, policy_type, status, effective_date, expiration_date

-- policy_schedules table  
id, policy_id, premium_amount, frequency, due_date, grace_period_days
```

#### Payment Service
```sql
-- payments table
id, policy_id, amount, status, payment_method, created_at, processed_at

-- payment_attempts table
id, payment_id, attempt_number, status, error_message, attempted_at

-- retry_schedules table
id, payment_id, next_retry_date, retry_count, max_retries, backoff_strategy
```

#### Billing Service
```sql
-- billing_cycles table
id, policy_id, billing_period_start, billing_period_end, amount_due, status

-- delinquency_tracking table
id, policy_id, days_delinquent, grace_period_end, status
```

## Grace Period & Retry Logic

### Grace Period Handling
- Configurable grace period per policy type (default: 30 days)
- Grace period starts after premium due date
- Policy remains active during grace period
- Notifications sent at configurable intervals

### Retry Schedule Strategy
- **Immediate retry**: For transient failures
- **Exponential backoff**: 1 hour, 4 hours, 1 day, 3 days, 7 days
- **Maximum retries**: 5 attempts
- **Dead letter queue**: For permanent failures

### Payment States
```
PENDING → PROCESSING → [SUCCESS | FAILED]
                    ↓
              RETRY_SCHEDULED → PROCESSING → [SUCCESS | FAILED | EXHAUSTED]
```

## Security Architecture

### Authentication & Authorization
- **JWT-based authentication** for API access
- **Service-to-service**: Mutual TLS or shared secrets
- **Role-based access**: ADMIN, USER, SYSTEM

### API Security
- All endpoints secured by default
- Health checks publicly accessible
- Rate limiting on payment endpoints

## Deployment Architecture

### Docker Compose Structure
```yaml
services:
  # Infrastructure
  mariadb, kafka, zookeeper, redis
  
  # Microservices  
  policy-service, billing-service, payment-service, notification-service
  
  # External
  payment-gateway-mock
```

### Port Mapping
- MariaDB: 3306
- Kafka: 9092
- Policy Service: 8081
- Billing Service: 8082  
- Payment Service: 8083
- Notification Service: 8084
- Payment Gateway: 8090

## Testing Strategy

### Test Pyramid

#### Unit Tests
- **Framework**: JUnit 5 + Mockito
- **Scope**: Business logic, controllers (MockMvc)
- **No external dependencies**: Mocked services, repositories
- **Coverage**: >80% line coverage

#### Integration Tests  
- **Framework**: JUnit 5 + TestContainers
- **Scope**: Service layer + database + messaging
- **Real dependencies**: MariaDB, Kafka containers
- **Data**: Test data sets, database migrations

#### E2E Tests
- **Framework**: JUnit 5 + TestContainers + RestAssured
- **Scope**: Full system workflows
- **Real services**: All microservices in containers
- **Scenarios**: Complete billing workflows

### Test Examples

```java
// Unit Test
@WebMvcTest(PolicyController.class)
class PolicyControllerTest {
    @Test void shouldReturnPolicySchedule() { }
}

// Integration Test  
@SpringBootTest
@Testcontainers
class PaymentServiceIntegrationTest {
    @Container static MariaDBContainer mariadb;
    @Test void shouldProcessPaymentWithRetry() { }
}

// E2E Test
@SpringBootTest
@Testcontainers  
class BillingSystemE2ETest {
    @Test void shouldCompleteFullBillingCycle() { }
}
```

## Scalability Considerations

### Horizontal Scaling
- **Stateless services**: Can scale independently
- **Database connection pooling**: Configured per service
- **Kafka partitioning**: By policy_id for ordered processing

### Performance Optimizations
- **Caching**: Redis for frequently accessed data
- **Database indexing**: On policy_id, customer_id, due_dates
- **Batch processing**: For bulk notifications and calculations

### Monitoring & Observability
- **Health checks**: Spring Actuator endpoints
- **Metrics**: Micrometer + Prometheus
- **Logging**: Structured JSON logs
- **Tracing**: Spring Cloud Sleuth

## Development Workflow

### Local Development
1. Start infrastructure: `docker-compose up mariadb kafka`
2. Run services in IDE for debugging
3. Use profiles: `local`, `docker`, `test`

### Testing Workflow
1. **Unit tests**: `mvn test`
2. **Integration tests**: `mvn verify -Pintegration`  
3. **E2E tests**: `mvn verify -Pe2e`

### Deployment
1. **Build**: `mvn clean package`
2. **Docker images**: `docker-compose build`
3. **Deploy**: `docker-compose up`

## API Documentation

### OpenAPI/Swagger
- Each service exposes OpenAPI spec at `/v3/api-docs`
- Swagger UI available at `/swagger-ui.html`
- Aggregated documentation for all services

### Example API Flows

#### 1. Create Policy and Calculate Premium
```http
POST /api/policies
GET /api/policies/{id}/schedule  
POST /api/billing/calculate
```

#### 2. Process Payment with Retry
```http
POST /api/payments/attempt
# If failed:
POST /api/payments/{id}/retry
GET /api/payments/delinquent
```

#### 3. Query Delinquent Policies
```http
GET /api/payments/delinquent
GET /api/billing/{policyId}/status
```

## Assumptions

### Business Logic Assumptions
- Premium amounts are calculated monthly
- Grace period is 30 days from due date  
- Maximum 5 retry attempts per payment
- Policies become delinquent after grace period + failed retries

### Technical Assumptions
- Services communicate asynchronously via Kafka
- Each service maintains its own database
- Payment gateway responses are eventually consistent
- System handles moderate load (< 10k policies initially)

### Integration Assumptions
- Payment gateway provides webhook notifications
- Customer data comes from external CRM system
- Email/SMS providers are available for notifications
- Audit logging is required for all payment transactions

## Next Steps

1. **Phase 1**: Core skeleton with health checks
2. **Phase 2**: Basic CRUD operations and database setup
3. **Phase 3**: Event-driven communication via Kafka
4. **Phase 4**: Payment processing and retry logic
5. **Phase 5**: Comprehensive testing suite
6. **Phase 6**: Monitoring and deployment automation