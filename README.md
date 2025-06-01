# Policy Billing System

A microservice-based policy billing and collections system that manages recurring premiums, payment processing, retry logic, and delinquency tracking through event-driven architecture.

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 21
- Maven 3.9+

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
   # Check all services are healthy
   curl http://localhost:8081/actuator/health  # Policy Service
   curl http://localhost:8082/actuator/health  # Billing Service  
   curl http://localhost:8083/actuator/health  # Payment Service
   curl http://localhost:8084/actuator/health  # Notification Service
   curl http://localhost:8090/actuator/health  # Payment Gateway Mock
   ```

### Testing the API

```bash
# Test Policy Service
curl http://localhost:8081/api/policies/hello
curl http://localhost:8081/api/policies/POLICY-123
curl http://localhost:8081/api/policies/POLICY-123/schedule

# Test other services (when implemented)
curl http://localhost:8082/api/billing/hello
curl http://localhost:8083/api/payments/hello
curl http://localhost:8084/api/notifications/hello
```

## Development Workflow

### Local Development

1. **Start infrastructure**:
   ```bash
   docker-compose up mariadb kafka zookeeper redis -d
   ```

2. **Run services locally** (in separate terminals):
   ```bash
   mvn clean install
   cd policy-service && mvn spring-boot:run
   cd billing-service && mvn spring-boot:run  
   cd payment-service && mvn spring-boot:run
   cd notification-service && mvn spring-boot:run
   ```

3. **Or run from IDE** with Spring Boot configuration

### Testing

```bash
# Unit tests
mvn test

# Integration tests  
mvn verify -Pintegration

# E2E tests (requires Docker Compose)
mvn verify -Pe2e

# All tests
mvn verify
```

### Building

```bash
# Build all modules
mvn clean package

# Build Docker images
docker-compose build

# Build specific service
mvn clean package -pl policy-service -am
```

## Architecture

### Services

| Service | Port | Responsibility |
|---------|------|-----------|
| Policy Service | 8081 | Policy metadata and premium schedules |
| Billing Service | 8082 | Premium calculations and billing cycles |
| Payment Service | 8083 | Payment processing and retry logic |
| Notification Service | 8084 | Payment reminders and notifications |
| Payment Gateway Mock | 8090 | Simulated third-party payment provider |

### Infrastructure

| Component | Port | Purpose |
|-----------|------|---------|
| MariaDB | 3306 | Primary database |
| Kafka | 9092 | Event streaming |
| Zookeeper | 2181 | Kafka coordination |
| Redis | 6379 | Caching and sessions |

### API Endpoints

#### Policy Service (Port 8081)
- `GET /api/policies/hello` - Health check
- `GET /api/policies/{policyId}` - Get policy details
- `GET /api/policies/{policyId}/schedule` - Get premium schedule
- `GET /actuator/health` - Service health

#### Billing Service (Port 8082)
- `GET /api/billing/hello` - Health check  
- `POST /api/billing/calculate` - Calculate premium
- `GET /api/billing/{policyId}/premium` - Get premium details
- `GET /actuator/health` - Service health

#### Payment Service (Port 8083)
- `GET /api/payments/hello` - Health check
- `POST /api/payments/attempt` - Process payment
- `GET /api/payments/{paymentId}/status` - Payment status
- `POST /api/payments/{paymentId}/retry` - Retry failed payment
- `GET /api/payments/delinquent` - List delinquent policies
- `GET /actuator/health` - Service health

#### Notification Service (Port 8084)
- `GET /api/notifications/hello` - Health check
- `POST /api/notifications/send` - Send notification
- `GET /api/notifications/{policyId}` - Get notifications
- `GET /actuator/health` - Service health

## Project Structure

```
policy-billing-system/
├── docker-compose.yml
├── pom.xml (parent)
├── README.md
├── docker/
│   └── mariadb/init/
├── shared-models/
│   └── src/main/java/com/billing/shared/
├── policy-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── billing-service/
├── payment-service/
├── notification-service/
└── payment-gateway-mock/
```

## Technology Stack

- **Framework**: Spring Boot 3.2 with WebMVC
- **Database**: MariaDB 11.0 with JPA/Hibernate  
- **Messaging**: Apache Kafka
- **Security**: Spring Security with JWT
- **Testing**: JUnit 5, TestContainers, RestAssured
- **Build**: Maven multi-module
- **Containerization**: Docker

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

### Event Flow

1. **Policy Creation**: Policy Service → `billing.policy.created` → Billing Service
2. **Payment Processing**: Payment Service → `billing.payment.attempted` → Notification Service
3. **Failed Payment**: Payment Service → `billing.payment.failed` → Billing Service (retry logic)
4. **Retry Scheduling**: Billing Service → `billing.retry.scheduled` → Payment Service

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | mariadb | Database hostname |
| `DB_PORT` | 3306 | Database port |
| `DB_NAME` | billing_system | Database name |
| `DB_USER` | billing_user | Database username |
| `DB_PASSWORD` | billing_password | Database password |
| `KAFKA_BOOTSTRAP_SERVERS` | kafka:9092 | Kafka brokers |
| `REDIS_HOST` | redis | Redis hostname |
| `REDIS_PORT` | 6379 | Redis port |

### Spring Profiles

- `local` - Local development with external infrastructure
- `docker` - Running in Docker containers
- `test` - Unit/integration testing

## Monitoring

### Health Checks

All services expose health checks at `/actuator/health`:

```bash
curl http://localhost:8081/actuator/health
```

### Metrics

Metrics available at `/actuator/metrics`:

```bash
curl http://localhost:8081/actuator/metrics
```

## Troubleshooting

### Common Issues

1. **Services won't start**:
   ```bash
   # Check if ports are available
   netstat -tulpn | grep :8081
   
   # Check Docker logs
   docker-compose logs policy-service
   ```

2. **Database connection issues**:
   ```bash
   # Verify MariaDB is running
   docker-compose ps mariadb
   
   # Check database logs
   docker-compose logs mariadb
   ```

3. **Kafka connection issues**:
   ```bash
   # Verify Kafka is running
   docker-compose ps kafka
   
   # Check Kafka logs
   docker-compose logs kafka
   ```

### Useful Commands

```bash
# View all running containers
docker-compose ps

# Follow logs for specific service
docker-compose logs -f policy-service

# Restart specific service
docker-compose restart policy-service

# Clean up everything
docker-compose down -v
docker system prune -f
```

## Development Guidelines

### Adding New Endpoints

1. Create controller in appropriate service
2. Add unit tests with `@WebMvcTest`
3. Add integration tests with TestContainers
4. Update API documentation
5. Add E2E test scenarios

### Database Changes

1. Create Flyway migration in `src/main/resources/db/migration`
2. Update JPA entities
3. Update integration tests
4. Test migration with Docker

### Adding New Events

1. Define event in `shared-models`
2. Add producer in source service
3. Add consumer in target service  
4. Add integration tests for event flow

## Next Steps

- [ ] Implement complete CRUD operations for policies
- [ ] Add database migrations with Flyway
- [ ] Implement Kafka event producers/consumers
- [ ] Add payment processing logic with retry mechanisms
- [ ] Implement grace period and delinquency tracking
- [ ] Add security with JWT authentication
- [ ] Implement comprehensive monitoring and logging