# Policy Billing System Development Plan

## Executive Summary

This document outlines the development strategy for a microservice-based policy billing system with accompanying UI applications for testing and demonstration. The system prioritizes developer productivity, API validation, and iterative delivery while addressing production-ready concerns around testing, security, and maintainability.

---

## Strategic Architecture Overview

### System Vision
The UI applications are designed to exercise the billing system APIs with a focus on developer ergonomics and simplicity. In a production environment, these would likely be SPAs with a backend-for-frontend service, but for development and testing purposes, we're creating simple HTML applications that demonstrate API functionality.

### Service Architecture
- **Policy Service (Port 8081)**: Manages policy metadata, customer relationships, and premium schedules
- **Billing Service (Port 8082)**: Handles billing calculations, delinquency tracking, and grace period management  
- **Payment Service (Port 8083)**: Processes payments, retry logic, and transaction state management
- **Notification Service (Port 8084)**: Manages event-driven notifications and communication
- **Payment Gateway Mock (Port 8085)**: Simulates third-party payment processing

### Core Business Capabilities
- Calculate recurring premiums based on policy metadata
- Trigger payment reminders and retry logic with exponential backoff
- Record and track payments with comprehensive delinquency status
- Integrate with third-party payment providers through abstracted interfaces
- Event-driven notifications with pub/sub architecture for scalable messaging
- Grace period handling with configurable business rules
- Extensible payment channel support (credit card, ACH, digital wallets)

---

## Shared Model Strategy

### Current State Assessment
**Existing Models:**
- `PolicyDto` - Currently in shared-models module, used by Policy Service

**Missing Models Identified:**
Based on test compilation errors and business requirements, the following models need to be created:

### Phase 2.5: Shared Model Library Development (NEW)
**Timeline**: 1 week
**Resources**: 1 developer

#### Core Domain Models
1. **`BillingDto`** - Billing calculations and premium details
   - Fields: `policyId`, `baseAmount`, `fees`, `lateFees`, `totalDue`, `dueDate`, `gracePeriodEnd`
   - Used by: Billing Service, Payment Service

2. **`DelinquentPolicyDto`** - Delinquent policy tracking
   - Fields: `policyId`, `customerId`, `customerName`, `daysOverdue`, `amountOverdue`, `lastPaymentDate`, `gracePeriodExpiry`
   - Used by: Billing Service, Admin UI

3. **`PaymentTransactionDto`** - Payment processing records
   - Fields: `transactionId`, `policyId`, `amount`, `paymentMethod`, `status`, `attemptedAt`, `completedAt`, `failureReason`
   - Used by: Payment Service, Billing Service

4. **`PremiumCalculationDto`** - Premium calculation results
   - Fields: `policyType`, `baseAmount`, `riskFactors`, `discounts`, `totalPremium`, `effectiveDate`, `frequency`
   - Used by: Billing Service, Policy Service

5. **`NotificationEventDto`** - Event-driven notification data
   - Fields: `eventType`, `policyId`, `customerId`, `message`, `channel`, `scheduledFor`, `sentAt`
   - Used by: Notification Service, all services

#### Business Rule Enums
1. **`PolicyStatus`** - Policy lifecycle states
   - Values: `ACTIVE`, `OVERDUE`, `GRACE_PERIOD`, `SUSPENDED`, `CANCELLED`

2. **`PaymentStatus`** - Payment transaction states
   - Values: `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`, `CANCELLED`, `REFUNDED`

3. **`NotificationType`** - Notification categories
   - Values: `PAYMENT_DUE`, `PAYMENT_OVERDUE`, `GRACE_PERIOD_WARNING`, `PAYMENT_CONFIRMED`, `POLICY_SUSPENDED`

#### Validation and Serialization
- **Jakarta Validation**: Add `@NotNull`, `@Size`, `@Positive` annotations
- **Jackson Configuration**: Custom serializers for dates and monetary amounts
- **Builder Pattern**: Lombok-based builders for immutable DTOs
- **Test Support**: Factory methods for test data generation

#### Implementation Strategy
1. **Week 1**: Create core DTOs with validation annotations
2. **Validation**: Unit tests for all model validation rules
3. **Documentation**: JavaDoc with usage examples for each model
4. **Integration**: Update all services to use shared models
5. **Migration**: Replace ad-hoc Map<String, Object> responses with proper DTOs

---

## Application Design Strategy

### Customer UI Application
**Target Users:** Insurance policyholders, customer service representatives, QA testers validating customer flows

**Core Functions:**
- View policy details and premium schedules with real-time status
- Make payments and view comprehensive payment history
- Manage payment methods and autopay settings with validation
- Receive billing notifications and alerts with preference management

**Hosting Service:** **Policy Service (Port 8081)**
- Rationale: Customer UI primarily deals with policy data and associated billing
- Policy service already handles policy metadata and premium schedules
- Natural fit for customer-centric operations with direct data access
- Can efficiently proxy billing/payment API calls to other services

### Admin UI Application
**Target Users:** Billing operations team, customer service managers, system administrators, developers testing internal APIs

**Core Functions:**
- View delinquent policies dashboard with filtering and search
- Manage payment retry operations with manual override capabilities
- Monitor payment processing status with real-time updates
- Generate billing reports and analytics with export capabilities
- Trigger manual billing operations with audit logging

**Hosting Service:** **Billing Service (Port 8082)**
- Rationale: Admin UI focuses on billing operations and system management
- Billing service handles premium calculations and delinquency tracking
- Contains the business logic for retry scheduling and grace period management
- Appropriate for internal operational tools with enhanced security requirements

---

## Technical Implementation Architecture

### API Design Standards
- **Versioning Strategy**: Semantic versioning (v1, v2) with backward compatibility
- **Documentation**: OpenAPI 3.0 specification with Swagger UI integration
- **Response Format**: Standardized JSON with consistent error handling using shared DTOs
- **Authentication**: JWT-based with role-based access control (RBAC)
- **Rate Limiting**: Per-service limits with graceful degradation

### Error Handling Strategy
- **UI-to-API Communication**: Centralized error handling with user-friendly messages
- **Service-to-Service**: Circuit breaker pattern with fallback mechanisms
- **Logging**: Structured logging with correlation IDs for request tracing
- **Monitoring**: Health checks, metrics collection, and alerting thresholds

### Event-Driven Architecture
- **Message Broker**: Apache Kafka for reliable event streaming
- **Event Types**: 
  - `payment.attempted`, `payment.succeeded`, `payment.failed`
  - `policy.delinquent`, `retry.scheduled`, `notification.sent`
- **Consumer Groups**: Service-specific with configurable retry policies
- **Schema Registry**: Avro schemas for event structure validation
- **Event DTOs**: Shared models for event payloads ensuring consistency

---

## Testing Strategy

### Testing Pyramid Implementation

#### Unit Testing (70% coverage target)
- **Service Layer**: Business logic validation with mocked dependencies
- **Controller Layer**: HTTP request/response handling with MockMvc
- **Data Layer**: Repository pattern testing with test containers
- **Shared Models**: Validation rules and serialization testing
- **Test Profiles**: All tests use `@ActiveProfiles("test")` for consistent configuration

#### Integration Testing (20% coverage target)
- **API Integration**: Full HTTP endpoint testing with real database
- **Service Communication**: Cross-service integration with embedded Kafka
- **Database Integration**: Transaction handling and data consistency
- **Event Flow**: End-to-end event processing validation
- **Shared Model Integration**: DTO serialization across service boundaries

#### Contract Testing
- **Provider Contracts**: Pact-based testing between services using shared DTOs
- **Consumer Contracts**: API client validation against specifications
- **Schema Validation**: Event structure compatibility testing
- **Model Evolution**: Backward compatibility testing for DTO changes

#### End-to-End Testing (10% coverage target)
- **User Journey**: Complete customer payment workflows
- **Admin Operations**: Delinquency management and retry scenarios
- **Error Scenarios**: Failure handling and recovery testing
- **Cross-Service Workflows**: Full business process validation

### Unit Testing Implementation Guide

Based on the established patterns in policy-service, here's how to implement unit tests for billing-service and payment-service:

#### Billing Service Unit Tests

**1. Controller Layer Testing (`BillingControllerTest.java`)**
```java
@WebMvcTest(BillingController.class)
@Import(WebSecurityTestConfig.class)
class BillingControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private BillingService billingService;
    
    @Test
    void shouldCalculatePremiumForPolicy() throws Exception {
        BillingDto mockBilling = BillingDto.builder()
            .policyId("POLICY-123")
            .baseAmount(new BigDecimal("156.00"))
            .lateFees(new BigDecimal("15.00"))
            .totalDue(new BigDecimal("171.00"))
            .dueDate(LocalDate.now().plusDays(30))
            .build();
            
        when(billingService.calculatePremium("POLICY-123"))
            .thenReturn(mockBilling);
            
        mockMvc.perform(get("/api/billing/POLICY-123/calculate"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.policyId").value("POLICY-123"))
            .andExpect(jsonPath("$.totalDue").value(171.00));
    }
    
    @Test
    void shouldGetDelinquentPolicies() throws Exception {
        List<DelinquentPolicyDto> delinquentPolicies = Arrays.asList(
            DelinquentPolicyDto.builder()
                .policyId("POLICY-123")
                .customerId("CUST-001")
                .daysOverdue(15)
                .amountOverdue(new BigDecimal("171.00"))
                .build()
        );
        
        when(billingService.getDelinquentPolicies())
            .thenReturn(delinquentPolicies);
            
        mockMvc.perform(get("/api/billing/delinquent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].policyId").value("POLICY-123"))
            .andExpect(jsonPath("$[0].daysOverdue").value(15));
    }
}
```

**2. Service Layer Testing (`BillingServiceImplTest.java`)**
```java
@ExtendWith(MockitoExtension.class)
class BillingServiceImplTest {
    
    @Mock
    private BillingRepository billingRepository;
    
    @Mock
    private PolicyService policyService;
    
    @InjectMocks
    private BillingServiceImpl billingService;
    
    @Test
    void shouldCalculatePremiumWithLateFees() {
        // Given
        PolicyDto policy = PolicyDto.builder()
            .policyId("POLICY-123")
            .status(PolicyDto.PolicyStatus.OVERDUE)
            .premiumAmount(new BigDecimal("156.00"))
            .build();
            
        when(policyService.getPolicyById("POLICY-123"))
            .thenReturn(Optional.of(policy));
            
        // When
        BillingDto result = billingService.calculatePremium("POLICY-123");
        
        // Then
        assertThat(result.getPolicyId()).isEqualTo("POLICY-123");
        assertThat(result.getLateFees()).isEqualTo(new BigDecimal("15.00"));
        assertThat(result.getTotalDue()).isEqualTo(new BigDecimal("171.00"));
    }
    
    @Test
    void shouldIdentifyDelinquentPolicies() {
        // Given
        List<BillingRecord> overdueRecords = Arrays.asList(
            createOverdueBillingRecord("POLICY-123", 15)
        );
        
        when(billingRepository.findOverduePolicies())
            .thenReturn(overdueRecords);
            
        // When
        List<DelinquentPolicyDto> result = billingService.getDelinquentPolicies();
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPolicyId()).isEqualTo("POLICY-123");
        assertThat(result.get(0).getDaysOverdue()).isEqualTo(15);
    }
}
```

**3. Test Configuration (`WebSecurityTestConfig.java`)**
```java
// Copy the same WebSecurityTestConfig.java from policy-service to:
// billing-service/src/test/java/com.insurance/billing/config/WebSecurityTestConfig.java
package com.insurance.billing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityTestConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
```

**4. Test Properties (`application-test.properties`)**
```properties
# Copy the same test configuration pattern from policy-service:
# billing-service/src/test/resources/application-test.properties
spring.security.enabled=false
spring.main.allow-bean-definition-overriding=true
spring.jpa.hibernate.ddl-auto=create-drop
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.url=jdbc:h2:mem:billingdb;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.flyway.enabled=false
logging.level.com.insurance=DEBUG
```

#### Payment Service Unit Tests

**1. Controller Layer Testing (`PaymentControllerTest.java`)**
```java
@WebMvcTest(PaymentController.class)
@Import(WebSecurityTestConfig.class)
class PaymentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PaymentService paymentService;
    
    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {
        PaymentTransactionDto transaction = PaymentTransactionDto.builder()
            .transactionId("TXN-123")
            .policyId("POLICY-123")
            .amount(new BigDecimal("156.00"))
            .status(PaymentStatus.COMPLETED)
            .paymentMethod("CREDIT_CARD")
            .build();
            
        when(paymentService.processPayment(any(PaymentTransactionDto.class)))
            .thenReturn(transaction);
            
        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPaymentRequest())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactionId").value("TXN-123"))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    
    @Test
    void shouldRetryFailedPayment() throws Exception {
        when(paymentService.retryPayment("TXN-FAILED"))
            .thenReturn(createRetryResult());
            
        mockMvc.perform(post("/api/payments/TXN-FAILED/retry"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.retryAttempt").value(2))
            .andExpected(jsonPath("$.nextRetryAt").exists());
    }
}
```

**2. Service Layer Testing (`PaymentServiceImplTest.java`)**
```java
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
    
    @Mock
    private PaymentGateway paymentGateway;
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private PaymentServiceImpl paymentService;
    
    @Test
    void shouldProcessPaymentWithRetryLogic() {
        // Given
        PaymentTransactionDto transaction = createPaymentTransaction();
        when(paymentGateway.processPayment(any())).thenReturn(PaymentResult.success());
        
        // When
        PaymentTransactionDto result = paymentService.processPayment(transaction);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(notificationService).sendPaymentConfirmation(any());
    }
    
    @Test
    void shouldCalculateExponentialBackoffForRetries() {
        // Given
        PaymentTransactionDto failedTransaction = createFailedTransaction();
        
        // When
        LocalDateTime nextRetry = paymentService.calculateNextRetryTime(2);
        
        // Then
        assertThat(nextRetry).isAfter(LocalDateTime.now().plusMinutes(4));
        assertThat(nextRetry).isBefore(LocalDateTime.now().plusMinutes(6));
    }
}
```

#### Key Testing Patterns to Follow

1. **Test Structure**: Use the same structure as `PolicyControllerTest.java`:
   - `@WebMvcTest` for controller tests
   - `@Import(WebSecurityTestConfig.class)` for security config
   - `@MockBean` for service dependencies
   - Proper test data setup using builder patterns

2. **Security Configuration**: Create identical `WebSecurityTestConfig.java` in each service's test package

3. **Test Properties**: Use consistent `application-test.properties` with H2 database and disabled security

4. **Mocking Strategy**: Mock service dependencies and focus on testing the specific layer
   - Controllers: Mock services, test HTTP handling
   - Services: Mock repositories and external services, test business logic

5. **Assertion Patterns**: Use AssertJ and JSONPath for clear, readable assertions

6. **Test Data**: Create builder-based factory methods for consistent test data creation

### Performance Testing
- **Service Level Objectives (SLOs)**:
  - API Response Time: < 200ms (95th percentile)
  - Payment Processing: < 5 seconds end-to-end
  - Event Processing: < 1 second message delivery
- **Load Testing**: Payment processing under peak loads
- **Stress Testing**: System behavior under resource constraints

### Security Testing
- **Authentication**: JWT token validation and expiration
- **Authorization**: Role-based access control enforcement
- **Input Validation**: SQL injection and XSS prevention using shared model validation
- **Data Encryption**: Sensitive data protection in transit and at rest

### Test Data Management
- **Mock Data**: Realistic test scenarios with edge cases using shared DTO factories
- **Data Isolation**: Test environment separation from production
- **Data Cleanup**: Automated test data lifecycle management

---

## Operational Excellence

### Monitoring and Observability
- **Application Metrics**: Business KPIs and technical performance
- **Infrastructure Monitoring**: Resource utilization and availability
- **Distributed Tracing**: Request flow across service boundaries
- **Log Aggregation**: Centralized logging with search capabilities
- **Model Metrics**: DTO validation failure rates and serialization performance

### Documentation Standards
- **API Documentation**: Living documentation with examples using shared models
- **Architectural Decision Records (ADRs)**: Decision tracking and rationale
- **Runbooks**: Operational procedures and troubleshooting guides
- **API Changelog**: Version history and breaking changes
- **Model Documentation**: JavaDoc for all shared DTOs with usage examples

### Deployment Strategy
- **Containerization**: Docker containers with multi-stage builds
- **Orchestration**: Kubernetes deployment with service mesh
- **CI/CD Pipeline**: Automated testing, building, and deployment
- **Blue-Green Deployment**: Zero-downtime service updates
- **Model Versioning**: Semantic versioning for shared-models module

---

## Project Execution Plan

### Phase 1: Foundation and Infrastructure (Sprint 1-2)
**Timeline**: 2 weeks
**Resources**: 2 developers, 1 DevOps engineer

**Deliverables:**
- Service skeleton projects with Spring Boot configuration
- Database schema design and migration scripts
- Docker containerization with development compose files
- CI/CD pipeline setup with automated testing
- Kafka cluster configuration for local development

**Risk Mitigation:**
- Infrastructure automation to prevent environment drift
- Documentation templates for consistent knowledge capture

### Phase 2: Core API Development (Sprint 3-5)
**Timeline**: 3 weeks  
**Resources**: 3 developers, 1 QA engineer

**Deliverables:**
- Policy Service API with full CRUD operations
- Payment Service with retry logic implementation
- Billing Service with delinquency tracking
- Comprehensive unit test suite (>80% coverage)
- API documentation with Swagger integration

**Dependencies:**
- Phase 1 infrastructure completion
- Database schema approval
- Event schema definitions

### Phase 2.5: Shared Model Library (Sprint 6) ⭐ NEW
**Timeline**: 1 week
**Resources**: 1 developer

**Deliverables:**
- Complete shared-models module with all business DTOs
- Validation annotations and custom serializers
- Builder patterns and factory methods for testing
- Migration of all services to use shared models
- Documentation and usage examples

**Quality Gates:**
- 100% test coverage for all shared models
- Backward compatibility validation
- Performance benchmarks for serialization

### Phase 3: Integration and Events (Sprint 7-8)
**Timeline**: 2 weeks
**Resources**: 3 developers, 1 QA engineer

**Deliverables:**
- Event-driven communication between services using shared DTOs
- Integration test suite with real service interactions
- Payment gateway mock service implementation
- Cross-service error handling and resilience patterns

**Risk Assessment:**
- Event ordering and processing guarantees
- Service communication failure scenarios
- Data consistency across service boundaries

### Phase 4: User Interface Implementation (Sprint 9-10)
**Timeline**: 2 weeks
**Resources**: 2 developers, 1 UI/UX designer

**Deliverables:**
- Customer UI with payment flow implementation
- Admin UI with operational dashboards
- JavaScript API integration with error handling
- Responsive design for mobile accessibility

**Quality Gates:**
- UI accessibility compliance (WCAG 2.1)
- Cross-browser compatibility testing
- API integration validation

### Phase 5: Security and Performance (Sprint 11-12)
**Timeline**: 2 weeks
**Resources**: 2 developers, 1 security specialist, 1 performance engineer

**Deliverables:**
- Authentication and authorization implementation
- Security testing and vulnerability assessment
- Performance testing and optimization
- Production readiness checklist completion

**Success Criteria:**
- Security scan with zero critical vulnerabilities
- Performance SLOs met under simulated load
- Production deployment preparation

### Phase 6: Production Preparation (Sprint 13)
**Timeline**: 1 week
**Resources**: Full team

**Deliverables:**
- Production environment setup and validation
- Monitoring and alerting configuration
- Documentation finalization and handover
- Production deployment and go-live support

---

## Current Implementation Status

### ✅ Completed
- Policy Service controller unit tests with full API coverage
- Payment Service controller unit tests including retry logic
- Billing Service controller unit tests with delinquency handling
- Policy Service API stubs with realistic mock data
- Payment Service API stubs with payment processing simulation
- PolicyDto implementation in shared-models module
- Test profile configuration fixes (`@ActiveProfiles("test")`)

### 🚧 In Progress (Phase 2.5)
- **Shared Model Development**: Creating comprehensive DTO library
- **Service Migration**: Updating controllers to use proper DTOs instead of Map<String, Object>
- **Validation Framework**: Adding Jakarta validation to all models

### 📋 Next Sprint Priorities
1. **Shared Model Completion**: Finish all business DTOs with validation
2. **Service Updates**: Replace ad-hoc responses with proper DTOs
3. **Integration Testing**: Update tests to use shared models
4. **Documentation**: Complete JavaDoc for all shared models

### Key Achievements
1. All core API endpoints implemented with mock data
2. Realistic test scenarios including failure cases and edge conditions
3. Retry logic simulation for failed payments with exponential backoff
4. Proper timestamp handling and overdue calculations with timezone support
5. Cross-origin support for UI integration with security considerations
6. Test configuration standardization with proper profiles

### Technical Debt Items
- [ ] **HIGH**: Complete shared model library (Phase 2.5)
- [ ] Replace Map<String, Object> responses with proper DTOs
- [ ] Implement proper pagination for large datasets
- [ ] Add request/response compression for API efficiency
- [ ] Enhance error messages with internationalization support
- [ ] Implement API rate limiting and throttling

---

## Risk Management

### Technical Risks
**High Priority:**
- Service communication failures during peak load
- Data consistency issues in distributed transactions
- Event processing bottlenecks affecting user experience
- **NEW**: Shared model breaking changes affecting multiple services

**Mitigation Strategies:**
- Circuit breaker implementation with graceful degradation
- Saga pattern for distributed transaction management
- Kafka partitioning strategy for event processing scalability
- **NEW**: Semantic versioning and backward compatibility testing for shared models

### Resource Risks
**Medium Priority:**
- Developer availability during critical sprints
- Infrastructure cost optimization requirements
- Third-party service integration delays
- **NEW**: Model design decisions requiring cross-team coordination

**Contingency Plans:**
- Cross-training team members on multiple services
- Cloud resource auto-scaling with cost monitoring
- Mock service implementations for external dependencies
- **NEW**: Clear model ownership and change approval process

### Business Risks
**Low Priority:**
- Changing compliance requirements affecting data handling
- User experience feedback requiring significant UI changes
- Performance requirements exceeding initial estimations

---

## Success Metrics

### Technical Metrics
- **API Performance**: < 200ms response time for 95% of requests
- **System Availability**: > 99.5% uptime during business hours
- **Event Processing**: < 1 second end-to-end event delivery
- **Test Coverage**: > 80% code coverage across all services
- **NEW**: **Model Consistency**: 100% DTO usage across all API responses

### Business Metrics
- **Payment Processing**: < 5% failure rate for valid transactions
- **User Satisfaction**: > 4.0/5.0 rating for UI usability
- **Operational Efficiency**: 50% reduction in manual billing operations
- **Developer Productivity**: < 2 hours for new developer environment setup

---

## Alternative Architecture Considerations

### Option 1: Dedicated UI Service
**Pros:** Clean separation, independent scaling, unified asset management
**Cons:** Additional service complexity, extra deployment, overkill for simple UIs

### Option 2: API Gateway
**Pros:** Centralized routing, security, caching
**Cons:** Gateway becomes stateful, static asset handling complexity

### Option 3: Notification Service
**Pros:** Low traffic service, simple hosting
**Cons:** Doesn't align with UI functionality, confusing service boundaries

### Recommended Implementation
Start with proposed architecture (Policy Service ↔ Customer UI, Billing Service ↔ Admin UI) for simplicity and clear service boundaries, with option to migrate to dedicated UI service as system scales.

This approach prioritizes developer productivity and clear API exercise capabilities while maintaining production-ready architectural patterns with proper shared model governance.

## Service Layer Architecture

### Design Patterns
- Each service follows interface-based design for loose coupling and testability
- Service implementations handle business logic and transaction boundaries
- Repository layer handles data access concerns

### Service Interface Template
```java
public interface ServiceName {
    // CRUD operations
    Optional<EntityDto> getById(String id);
    List<EntityDto> getByFilter(FilterDTO filter);
    EntityDto create(EntityDto dto);
    EntityDto update(String id, EntityDto dto);
    void delete(String id);
    
    // Business operations
    Map<String, Object> calculateMetrics(String id);
    void validateBusinessRules(EntityDto dto);
}
```

### Implementation Guidelines
1. Services:
   - **Policy Service**
     - PolicyService / PolicyServiceImpl
     - Handles policy metadata and premium schedules
   - **Billing Service**
     - BillingService / BillingServiceImpl
     - Manages billing cycles and payment status
   - **Payment Service**
     - PaymentService / PaymentServiceImpl
     - Processes payments and handles gateway integration
   - **Notification Service**
     - NotificationService / NotificationServiceImpl
     - Manages communication and event publishing

2. Common Patterns:
   - Use constructor injection for dependencies
   - Implement @Transactional boundaries
   - Include comprehensive logging
   - Handle exceptions with @ControllerAdvice
   - Document with Javadoc and OpenAPI annotations

3. Testing Strategy:
   - Unit tests with mocked dependencies
   - Integration tests with test containers
   - API contract tests with Spring Cloud Contract