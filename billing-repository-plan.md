# Billing Repository Implementation Plan

## Overview
This document outlines the implementation plan for the billing repository layer that will support the policy billing and collections microservice. The implementation will mirror the existing PolicyRepository pattern with entity, mapper, and repository folders.

## Architecture Reference
Following the established pattern from:
- `policy-service/src/main/java/com/billing/policy/entity/Policy.java`
- `policy-service/src/main/java/com/billing/policy/repository/PolicyRepository.java`  
- `policy-service/src/main/java/com/billing/policy/mapper/PolicyMapper.java`

## Core Requirements
The billing repository must support:
- Calculate recurring premiums based on policy metadata
- Trigger payment reminders and retry logic
- Record and track payments and delinquency status
- Integrate with third-party payment providers
- Event-driven or pub/sub architecture for notifications
- Grace period handling and retry schedule logic
- Extensibility for adding multiple payment channels

## Entity Design

### 1. Billing Entity
Core billing record linking policies to billing cycles.

```java
@Entity
@Table(name = "billings")
public class Billing {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String policyId;
    
    @Column(nullable = false)
    private String customerId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal premiumAmount;
    
    @Column(nullable = false)
    private LocalDate dueDate;
    
    @Column(nullable = false)
    private LocalDate billingPeriodStart;
    
    @Column(nullable = false)
    private LocalDate billingPeriodEnd;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) 
    private BillingFrequency frequency;
    
    private LocalDate gracePeriodEnd;
    private Integer retryCount;
    private LocalDate nextRetryDate;
    
    public enum BillingStatus {
        PENDING, PAID, OVERDUE, GRACE_PERIOD, DELINQUENT, CANCELLED
    }
    
    public enum BillingFrequency {
        MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL
    }
}
```

### 2. Payment Entity
Records all payment attempts and their outcomes.

```java
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String billingId;
    
    @Column(nullable = false)
    private String policyId;
    
    @Column(nullable = false)
    private String customerId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;
    
    @Column(nullable = false)
    private LocalDateTime attemptedAt;
    
    private LocalDateTime processedAt;
    private String transactionId;
    private String gatewayResponse;
    private String failureReason;
    
    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, CANCELLED, REFUNDED
    }
    
    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, ACH, PAYPAL, STRIPE
    }
}
```

### 3. PaymentRetry Entity
Manages retry logic and schedules for failed payments.

```java
@Entity
@Table(name = "payment_retries")
public class PaymentRetry {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String paymentId;
    
    @Column(nullable = false)
    private String billingId;
    
    @Column(nullable = false)
    private Integer retryAttempt;
    
    @Column(nullable = false)
    private LocalDateTime scheduledAt;
    
    private LocalDateTime attemptedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RetryStatus status;
    
    private String failureReason;
    
    public enum RetryStatus {
        SCHEDULED, IN_PROGRESS, SUCCESS, FAILED, SKIPPED, EXHAUSTED
    }
}
```

### 4. BillingEvent Entity
Event sourcing for billing-related events and notifications.

```java
@Entity
@Table(name = "billing_events")
public class BillingEvent {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String billingId;
    
    @Column(nullable = false)
    private String policyId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;
    
    @Column(nullable = false)
    private LocalDateTime occurredAt;
    
    private String payload;
    private String metadata;
    
    public enum EventType {
        BILLING_CREATED, PAYMENT_DUE, PAYMENT_SUCCESS, PAYMENT_FAILED,
        RETRY_SCHEDULED, GRACE_PERIOD_STARTED, DELINQUENT, REMINDER_SENT
    }
}
```

## Repository Interfaces

### 1. BillingRepository
```java
@Repository
public interface BillingRepository extends JpaRepository<Billing, String> {
    List<Billing> findByPolicyId(String policyId);
    List<Billing> findByCustomerId(String customerId);
    List<Billing> findByStatus(BillingStatus status);
    List<Billing> findByDueDateBefore(LocalDate date);
    List<Billing> findByStatusAndDueDateBefore(BillingStatus status, LocalDate date);
    
    @Query("SELECT b FROM Billing b WHERE b.status = 'OVERDUE' AND b.gracePeriodEnd < :date")
    List<Billing> findDelinquentBillings(@Param("date") LocalDate date);
    
    @Query("SELECT b FROM Billing b WHERE b.dueDate BETWEEN :start AND :end")
    List<Billing> findBillingsDueInRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
```

### 2. PaymentRepository
```java
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByBillingId(String billingId);
    List<Payment> findByPolicyId(String policyId);
    List<Payment> findByCustomerId(String customerId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByTransactionId(String transactionId);
    
    @Query("SELECT p FROM Payment p WHERE p.customerId = :customerId AND p.attemptedAt BETWEEN :start AND :end")
    List<Payment> findPaymentHistory(@Param("customerId") String customerId, 
                                   @Param("start") LocalDateTime start, 
                                   @Param("end") LocalDateTime end);
    
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.billingId IN :billingIds")
    List<Payment> findFailedPaymentsForBillings(@Param("billingIds") List<String> billingIds);
}
```

### 3. PaymentRetryRepository
```java
@Repository 
public interface PaymentRetryRepository extends JpaRepository<PaymentRetry, String> {
    List<PaymentRetry> findByPaymentId(String paymentId);
    List<PaymentRetry> findByBillingId(String billingId);
    List<PaymentRetry> findByStatus(RetryStatus status);
    
    @Query("SELECT pr FROM PaymentRetry pr WHERE pr.status = 'SCHEDULED' AND pr.scheduledAt <= :now")
    List<PaymentRetry> findDueRetries(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(pr) FROM PaymentRetry pr WHERE pr.billingId = :billingId")
    Integer countRetriesForBilling(@Param("billingId") String billingId);
}
```

### 4. BillingEventRepository
```java
@Repository
public interface BillingEventRepository extends JpaRepository<BillingEvent, String> {
    List<BillingEvent> findByBillingId(String billingId);
    List<BillingEvent> findByPolicyId(String policyId);
    List<BillingEvent> findByEventType(EventType eventType);
    List<BillingEvent> findByOccurredAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT be FROM BillingEvent be WHERE be.billingId = :billingId ORDER BY be.occurredAt DESC")
    List<BillingEvent> findEventHistoryForBilling(@Param("billingId") String billingId);
}
```

## Mapper Classes

### 1. BillingMapper
```java
@Mapper(componentModel = "spring")
public interface BillingMapper {
    BillingDto toDto(Billing billing);
    Billing toEntity(BillingDto billingDto);
    List<BillingDto> toDtos(List<Billing> billings);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    Billing createFromPolicy(Policy policy, LocalDate dueDate, BigDecimal amount);
}
```

### 2. PaymentMapper
```java
@Mapper(componentModel = "spring") 
public interface PaymentMapper {
    PaymentDto toDto(Payment payment);
    Payment toEntity(PaymentDto paymentDto);
    List<PaymentDto> toDtos(List<Payment> payments);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "attemptedAt", expression = "java(LocalDateTime.now())")
    Payment createFromRequest(PaymentRequestDto request);
}
```

### 3. PaymentRetryMapper
```java
@Mapper(componentModel = "spring")
public interface PaymentRetryMapper {
    PaymentRetryDto toDto(PaymentRetry paymentRetry);
    PaymentRetry toEntity(PaymentRetryDto paymentRetryDto);
    List<PaymentRetryDto> toDtos(List<PaymentRetry> paymentRetries);
}
```

### 4. BillingEventMapper
```java
@Mapper(componentModel = "spring")
public interface BillingEventMapper {
    BillingEventDto toDto(BillingEvent billingEvent);
    BillingEvent toEntity(BillingEventDto billingEventDto);
    List<BillingEventDto> toDtos(List<BillingEvent> billingEvents);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "occurredAt", expression = "java(LocalDateTime.now())")
    BillingEvent createEvent(String billingId, String policyId, EventType eventType, String payload);
}
```

## Directory Structure

```
billing-service/src/main/java/com/billing/
├── entity/
│   ├── Billing.java
│   ├── Payment.java
│   ├── PaymentRetry.java
│   └── BillingEvent.java
├── repository/
│   ├── BillingRepository.java
│   ├── PaymentRepository.java
│   ├── PaymentRetryRepository.java
│   └── BillingEventRepository.java
├── mapper/
│   ├── BillingMapper.java
│   ├── PaymentMapper.java
│   ├── PaymentRetryMapper.java
│   └── BillingEventMapper.java
└── dto/
    ├── BillingDto.java
    ├── PaymentDto.java
    ├── PaymentRetryDto.java
    └── BillingEventDto.java
```

## Implementation Order

### Phase 1: Core Entities
1. Create Billing entity with basic fields
2. Create Payment entity for transaction records
3. Set up basic repository interfaces
4. Implement core mapper interfaces

### Phase 2: Enhanced Features  
1. Add PaymentRetry entity for retry logic
2. Add BillingEvent entity for event sourcing
3. Implement advanced repository queries
4. Add comprehensive mapper methods

### Phase 3: Integration & Testing
1. Database migration scripts
2. Repository integration tests
3. Mapper unit tests
4. Service layer integration

## Database Schema

### Table Creation Order
1. `billings` (core billing records)
2. `payments` (payment transactions)
3. `payment_retries` (retry schedules)
4. `billing_events` (event log)

### Indexes Required
- `billings`: policyId, customerId, status, dueDate
- `payments`: billingId, policyId, customerId, transactionId
- `payment_retries`: paymentId, billingId, scheduledAt
- `billing_events`: billingId, policyId, eventType, occurredAt

## Configuration Dependencies

### Required Dependencies (pom.xml)
- Spring Data JPA
- MapStruct for mapping
- Lombok for entity boilerplate  
- MariaDB connector
- Validation annotations

### Application Properties
- Database connection settings
- JPA/Hibernate configuration
- MapStruct processor configuration

## Integration Points

### With Existing Services
- **PolicyService**: Retrieve policy data for billing calculations
- **NotificationService**: Send payment reminders and alerts
- **PaymentGateway**: Process payments and handle responses

### Event Publishing
- Billing created events
- Payment success/failure events  
- Retry scheduled events
- Delinquency status changes

## Questions for Clarification

1. **Grace Period Configuration**: Should grace periods be policy-specific or system-wide defaults?
2. **Retry Logic**: What is the preferred retry schedule (immediate, exponential backoff, fixed intervals)?
3. **Payment Methods**: Which third-party payment providers should be supported initially?
4. **Event Storage**: Should billing events be stored indefinitely or archived after a certain period?
5. **Currency Support**: Should the system support multiple currencies or just USD?
6. **Audit Requirements**: Are there specific audit trails needed for compliance?

## Success Criteria

### Functional Requirements
- ✅ Store and retrieve billing records by policy/customer
- ✅ Track payment attempts and their outcomes
- ✅ Manage retry schedules for failed payments
- ✅ Identify delinquent accounts efficiently
- ✅ Support multiple payment methods

### Non-Functional Requirements  
- ✅ Database queries execute in under 100ms for single record lookups
- ✅ Batch operations handle 1000+ records efficiently
- ✅ Repository layer is 100% unit tested
- ✅ Integration tests cover all query scenarios
- ✅ Mapper implementations handle null values gracefully

## Timeline Estimate

- **Phase 1**: 4-6 hours (Core entities and repositories)
- **Phase 2**: 3-4 hours (Enhanced features and advanced queries)  
- **Phase 3**: 2-3 hours (Integration and testing)

**Total Estimated Time**: 9-13 hours

## Dependencies & Risks

### Dependencies
- Existing Policy entity structure
- Database schema alignment
- MapStruct configuration in build pipeline

### Risks
- Database performance with large datasets
- Complex retry logic implementation
- Integration with multiple payment providers
- Event ordering and consistency

### Mitigation Strategies
- Database indexing strategy
- Pagination for large result sets
- Circuit breaker pattern for external services
- Event versioning and backward compatibility

## Work Summary & Testing Strategy

### Completed Work
- ✅ Defined core entity structure (Billing, Payment, PaymentRetry, BillingEvent)
- ✅ Designed repository interfaces with key queries
- ✅ Created mapper interfaces for DTO conversions
- ✅ Established directory structure and implementation phases

### Unit Testing Strategy

#### Repository Tests
```java
@DataJpaTest
class BillingRepositoryTest {
    @Autowired
    private BillingRepository billingRepository;
    
    @Test
    void shouldFindBillingsByPolicyId() {
        // Given a billing record
        // When searching by policy ID
        // Then return matching records
    }
    
    @Test
    void shouldIdentifyDelinquentAccounts() {
        // Given overdue billings past grace period
        // When querying for delinquent accounts
        // Then return only truly delinquent ones
    }
}
```

#### Mapper Tests
```java
@SpringBootTest
class BillingMapperTest {
    @Autowired
    private BillingMapper mapper;
    
    @Test
    void shouldMapBillingToDto() {
        // Given a billing entity
        // When mapping to DTO
        // Then all fields should match
    }
}
```

### Integration Testing Strategy

#### Database Integration
```java
@SpringBootTest
@Testcontainers
class BillingDatabaseIntegrationTest {
    @Container
    static MariaDBContainer<?> mariaDB = new MariaDBContainer<>("mariadb:10.6");
    
    @Test
    void shouldHandleConcurrentPaymentUpdates() {
        // Given multiple concurrent payment attempts
        // When processing simultaneously
        // Then maintain data consistency
    }
}
```

#### Event Publishing Tests
```java
@SpringBootTest
@Testcontainers
class BillingEventIntegrationTest {
    @Container
    static KafkaContainer kafka = new KafkaContainer();
    
    @Test
    void shouldPublishBillingEvents() {
        // Given a billing status change
        // When event is triggered
        // Then verify event published to Kafka
    }
}
```

### Required Shared Models

Add to shared-models module:

```java
// filepath: shared-models/src/main/java/com/billing/shared/dto/BillingDto.java
public class BillingDto {
    private String id;
    private String policyId;
    private BigDecimal amount;
    private LocalDate dueDate;
    private BillingStatus status;
    // ...other fields
}
```

```java
// filepath: shared-models/src/main/java/com/billing/shared/event/BillingEvent.java
public class BillingEvent {
    private String billingId;
    private String policyId;
    private BillingEventType type;
    private LocalDateTime timestamp;
    // ...other fields
}
```

```java
// filepath: shared-models/src/main/java/com/billing/shared/enums/BillingStatus.java
public enum BillingStatus {
    PENDING, PAID, OVERDUE, GRACE_PERIOD, DELINQUENT, CANCELLED
}
```

### Test Data Factories

```java
// filepath: billing-service/src/test/java/com/billing/test/TestDataFactory.java
public class TestDataFactory {
    public static Billing createTestBilling() {
        return Billing.builder()
            .id(UUID.randomUUID().toString())
            .policyId("TEST-POLICY-1")
            .amount(new BigDecimal("100.00"))
            .dueDate(LocalDate.now().plusDays(30))
            .status(BillingStatus.PENDING)
            .build();
    }
    
    public static Payment createTestPayment() {
        return Payment.builder()
            .id(UUID.randomUUID().toString())
            .billingId("TEST-BILLING-1")
            .amount(new BigDecimal("100.00"))
            .status(PaymentStatus.PENDING)
            .build();
    }
}
```

### Test Coverage Goals
- Repository Layer: 90%+ coverage
- Mapper Layer: 100% coverage
- Entity Validation: 100% coverage
- Integration Tests: Cover all critical paths
- Event Publishing: Verify all event types

### Test Categories
1. **Unit Tests**
   - Repository method behavior
   - Mapper transformations
   - Entity validation rules
   - Business logic edge cases

2. **Integration Tests**
   - Database transactions
   - Event publishing/consuming
   - Concurrent operations
   - External service integration

3. **Performance Tests**
   - Bulk operations
   - Query optimization
   - Connection pool sizing

### Shared Models Dependencies
Add to shared-models/pom.xml:
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-annotations</artifactId>
    </dependency>
</dependencies>
```