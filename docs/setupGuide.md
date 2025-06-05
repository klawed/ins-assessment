## How to Build

### Prerequisites
- **Java 21** for modern language features and performance
- **Maven 3.9+** for building and managing dependencies
- **Docker** and **Docker Compose** for containerized infrastructure

### Build Commands

```bash
# Clone the repository
git clone <repository>
cd policy-billing-system

# Build all services
mvn clean install

# Build specific service
cd billing-service && mvn clean package

# Build Docker images
docker-compose build

# Build with tests
mvn clean verify
```

---

## How to Run Tests

### Unit Tests
```bash
# Run all unit tests
mvn test

# Run tests for specific service
cd billing-service && mvn test

# Run with coverage
mvn test jacoco:report
```

### Integration Tests
```bash
# Run integration tests (uses TestContainers)
mvn verify -Pintegration

# Run specific integration test
mvn test -Dtest=BillingServiceIntegrationTest
```

### End-to-End Tests
```bash
# Run full system tests
mvn verify -Pe2e

# Run with Docker Compose
docker-compose -f docker-compose-test.yml up --abort-on-container-exit
```

---

## Development Workflow

### Local Development Setup

1. **Start development infrastructure** (uses embedded H2 database):
   ```bash
   docker-compose -f docker-compose-dev.yml up -d
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
    - Import the project into your IDE (e.g., IntelliJ IDEA or Eclipse)
    - Configure Spring Boot run configurations for each service with the `dev` profile
    - Services will use embedded H2 database for development

4. **Verify services are running** (using admin credentials):
   ```bash
   curl -u admin:adminPassword http://localhost:8081/actuator/health  # Policy Service
   curl -u admin:adminPassword http://localhost:8082/actuator/health  # Billing Service  
   curl -u admin:adminPassword http://localhost:8083/actuator/health  # Payment Service
   curl -u admin:adminPassword http://localhost:8084/actuator/health  # Notification Service
   curl -u admin:adminPassword http://localhost:8090/actuator/health  # Payment Gateway Mock
   ```

### Development Profiles
- **dev**: Local development with embedded H2 and external Kafka/Redis
- **test**: Testing with TestContainers
- **docker**: Full containerized environment

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

| Component   | Port  | Purpose                                      | Notes |
|-------------|-------|----------------------------------------------|-------|
| **MariaDB** | 3306 (13306 in dev) | Primary database for production. | Dev uses embedded H2 |
| **Kafka**   | 9092 (19092 in dev) | Event streaming for inter-service messaging. | |
| **Zookeeper** | 2181 (12181 in dev) | Coordinates Kafka brokers. | |
| **Redis**   | 6379 (16379 in dev) | Caching and session management. | |

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

## Kafka Testing & Development

---

## Troubleshooting Kafka

### Prerequisites for Kafka Testing

Ensure Kafka is running before testing:
```bash
# Start development infrastructure (includes Kafka on port 19092)
docker-compose -f docker-compose-dev.yml up -d

# Verify Kafka is running
docker-compose -f docker-compose-dev.yml ps kafka
docker-compose -f docker-compose-dev.yml logs kafka | tail -20
```

### Method 1: Using Docker Exec with Kafka CLI Tools

#### Access Kafka Container CLI
```bash
# Enter the Kafka container
docker-compose -f docker-compose-dev.yml exec kafka bash

# Or run commands directly
docker-compose -f docker-compose-dev.yml exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic test-topic
```

#### List All Topics
```bash
docker-compose -f docker-compose-dev.yml exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

#### Create a Test Topic
```bash
docker-compose -f docker-compose-dev.yml exec kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic billing.test \
  --partitions 3 \
  --replication-factor 1
```

#### Describe a Topic
```bash
docker-compose -f docker-compose-dev.yml exec kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic billing.policy.created
```

#### Produce Test Messages
```bash
# Interactive producer (type messages and press Enter)
docker-compose -f docker-compose-dev.yml exec kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic billing.policy.created

# Example message to type:
# {"policyId":"POLICY-123","customerId":"CUST-001","policyType":"AUTO","premiumAmount":156.00,"timestamp":"2024-06-04T15:30:00Z"}
```

#### Consume Messages
```bash
# Consume from beginning
docker-compose -f docker-compose-dev.yml exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic billing.policy.created \
  --from-beginning

# Consume latest messages only
docker-compose -f docker-compose-dev.yml exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic billing.payment.failed \
  --timeout-ms 10000
```

#### Consume with Message Keys and Headers
```bash
docker-compose -f docker-compose-dev.yml exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic billing.payment.succeeded \
  --from-beginning \
  --print-key \
  --print-headers \
  --print-timestamp
```

### Method 2: Using kafkacat (kcat) - Connect to Dev Ports

#### Install kafkacat
```bash
# macOS
brew install kcat

# Ubuntu/Debian
sudo apt-get install kafkacat

# Windows (via WSL or use Docker method)
```

#### Produce Messages with kafkacat (using dev port 19092)
```bash
# Produce a single message
echo '{"policyId":"POLICY-456","amount":245.50,"status":"FAILED","timestamp":"2024-06-04T15:45:00Z"}' | \
  kcat -P -b localhost:19092 -t billing.payment.failed

# Produce from file
kcat -P -b localhost:19092 -t billing.policy.created < test-messages.json

# Produce with key
echo "POLICY-789:CUSTOMER-123|{\"policyId\":\"POLICY-789\",\"status\":\"DELINQUENT\"}" | \
  kcat -P -b localhost:19092 -t billing.policy.delinquent -K :|
#### Consume Messages with kafkacat (using dev port 19092)
```bash
# Consume from beginning
kcat -C -b localhost:19092 -t billing.policy.created -o beginning

# Consume latest with timeout
kcat -C -b localhost:19092 -t billing.payment.failed -o latest -e

# Consume with metadata
kcat -C -b localhost:19092 -t billing.payment.succeeded -f 'Topic: %t, Partition: %p, Offset: %o, Key: %k, Value: %s\n'
```

### Method 3: Testing API Endpoints that Trigger Events

#### Test Policy Creation (triggers billing.policy.created)
```bash
# Create a new policy that should trigger events
curl -u admin:adminPassword -X POST http://localhost:8081/api/policies \
  -H "Content-Type: application/json" \
  -d '{
    "policyNumber": "POL-TEST-001",
    "customerId": "CUST-TEST-001", 
    "policyType": "AUTO",
    "premiumAmount": 156.00,
    "frequency": "MONTHLY",
    "gracePeriodDays": 10
  }'
```

#### Test Payment Processing (triggers payment events)
```bash
# Process a payment that should trigger events
curl -u admin:adminPassword -X POST http://localhost:8083/api/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "billingId": "BILL-TEST-001",
    "amount": 156.00,
    "paymentMethod": "CREDIT_CARD"
  }'
```

#### Test Payment Retry (triggers retry events)
```bash
# Retry a failed payment
curl -u admin:adminPassword -X POST http://localhost:8083/api/payments/PAYMENT-TEST-001/retry \
  -H "Content-Type: application/json"
```

#### Get Delinquent Policies
```bash
# Query delinquent policies
curl -u admin:adminPassword http://localhost:8083/api/payments/delinquent
```

### Method 4: Using Kafka UI (Optional)

#### Setup Kafka UI for Visual Management
```bash
# Add to docker-compose-dev.yml or run separately
docker run -d \
  --name kafka-ui \
  -p 8080:8080 \
  -e KAFKA_CLUSTERS_0_NAME=local \
  -e KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=localhost:19092 \
  --network host \
  provectuslabs/kafka-ui:latest

# Access at http://localhost:8080
```

### Kafka Testing Scenarios

#### Scenario 1: Policy Lifecycle Testing
```bash
# 1. Start monitoring events
docker-compose -f docker-compose-dev.yml exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic billing.policy.created \
  --from-beginning &

# 2. Create a policy
curl -u admin:adminPassword -X POST http://localhost:8081/api/policies \
  -H "Content-Type: application/json" \
  -d '{"policyNumber":"POL-001","customerId":"CUST-001","policyType":"AUTO","premiumAmount":200.00}'

# 3. Verify event was published (check consumer output)
```

#### Scenario 2: Payment Failure and Retry Testing
```bash
# 1. Monitor payment events
docker-compose -f docker-compose-dev.yml exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic billing.payment.failed \
  --from-beginning &

# 2. Monitor retry events  
docker-compose -f docker-compose-dev.yml exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic billing.retry.scheduled \
  --from-beginning &

# 3. Simulate a failed payment
curl -u admin:adminPassword -X POST http://localhost:8083/api/payments/process \
  -H "Content-Type: application/json" \
  -d '{"billingId":"BILL-001","amount":999999.99,"paymentMethod":"CREDIT_CARD"}'

# 4. Trigger retry
curl -u admin:adminPassword -X POST http://localhost:8083/api/payments/FAILED-PAYMENT-ID/retry

# 5. Check both consumers for events
```

#### Scenario 3: Delinquency Testing
```bash
# 1. Monitor delinquency events
docker-compose -f docker-compose-dev.yml exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic billing.policy.delinquent \
  --from-beginning &

# 2. Create overdue scenarios by manually publishing events
echo '{"policyId":"POLICY-OVERDUE-001","customerId":"CUST-001","daysOverdue":45,"gracePeriodExpired":true}' | \
  kcat -P -b localhost:19092 -t billing.policy.delinquent

# 3. Query delinquent policies
curl -u admin:adminPassword http://localhost:8083/api/payments/delinquent
```

### Kafka Monitoring and Debugging

#### Check Kafka Health
```bash
# Check if Kafka is responsive
docker-compose -f docker-compose-dev.yml exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# Check topic configurations
docker-compose -f docker-compose-dev.yml exec kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe \
  --topics-with-overrides
```

#### View Consumer Groups
```bash
# List all consumer groups
docker-compose -f docker-compose-dev.yml exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list

# Describe a specific consumer group
docker-compose -f docker-compose-dev.yml exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group billing-service-group
```

#### Reset Consumer Group (for testing)
```bash
# Reset consumer group to beginning
docker-compose -f docker-compose-dev.yml exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group billing-service-group \
  --reset-offsets \
  --to-earliest \
  --topic billing.policy.created \
  --execute
```

### Troubleshooting Kafka Issues

#### Common Problems and Solutions

1. **Consumer Lag Issues**:
```bash
# Check consumer lag
docker-compose -f docker-compose-dev.yml exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group your-service-group
```

2. **Topic Not Found**:
```bash
# Create missing topics manually
docker-compose -f docker-compose-dev.yml exec kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic billing.missing.topic \
  --partitions 1 \
  --replication-factor 1
```

3. **Connection Issues**:
```bash
# Test connectivity
docker-compose -f docker-compose-dev.yml exec kafka kafka-broker-api-versions \
  --bootstrap-server localhost:9092
```

4. **View Kafka Logs**:
```bash
# Check Kafka container logs
docker-compose -f docker-compose-dev.yml logs kafka -f

# Check service logs for Kafka connectivity
docker-compose -f docker-compose-dev.yml logs billing-service | grep -i kafka
```

### Sample Test Messages

Create a file `test-messages.json` with sample events:
```json
{"eventType":"POLICY_CREATED","policyId":"POL-001","customerId":"CUST-001","timestamp":"2024-06-04T10:00:00Z"}
{"eventType":"PAYMENT_FAILED","policyId":"POL-001","paymentId":"PAY-001","amount":156.00,"reason":"INSUFFICIENT_FUNDS","timestamp":"2024-06-04T11:00:00Z"}
{"eventType":"RETRY_SCHEDULED","policyId":"POL-001","paymentId":"PAY-001","retryAttempt":1,"scheduledAt":"2024-06-04T12:00:00Z","timestamp":"2024-06-04T11:05:00Z"}
{"eventType":"POLICY_DELINQUENT","policyId":"POL-001","customerId":"CUST-001","daysOverdue":35,"timestamp":"2024-06-04T13:00:00Z"}
```

Then use it for testing:
```bash
# Publish test messages
kcat -P -b localhost:19092 -t billing.test < test-messages.json
```# Policy Billing System

A microservice-based policy billing and collections system that manages recurring premiums, payment processing, retry logic, and delinquency tracking through event-driven architecture.

---
