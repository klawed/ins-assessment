package com.billing.policy.controller;

import com.billing.policy.service.PolicyService;
import com.billing.shared.dto.PolicyDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {
    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello() {
        Map<String, String> response = Map.of(
                "service", "policy-service",
                "message", "Hello from Policy Service!",
                "status", "UP"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<PolicyDto> getPolicyById(@PathVariable("policyId") String policyId) {
        Optional<PolicyDto> policy = findMockPolicy(policyId);
        
        if (policy.isPresent()) {
            return ResponseEntity.ok(policy.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{policyId}/premium-schedule")
    public ResponseEntity<Map<String, Object>> getPremiumSchedule(@PathVariable("policyId") String policyId) {
        Optional<PolicyDto> policy = findMockPolicy(policyId);
        
        if (policy.isPresent()) {
            Map<String, Object> schedule = createMockPremiumSchedule(policy.get());
            return ResponseEntity.ok(schedule);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PolicyDto>> getPoliciesForCustomer(@PathVariable String customerId) {
        List<PolicyDto> policies = getMockPoliciesForCustomer(customerId);
        return ResponseEntity.ok(policies);
    }

    // Mock data methods
    private Optional<PolicyDto> findMockPolicy(String policyId) {
        List<PolicyDto> allPolicies = getAllMockPolicies();
        return allPolicies.stream()
                .filter(p -> p.getPolicyId().equals(policyId))
                .findFirst();
    }

    private List<PolicyDto> getMockPoliciesForCustomer(String customerId) {
        List<PolicyDto> allPolicies = getAllMockPolicies();
        return allPolicies.stream()
                .filter(p -> p.getCustomerId().equals(customerId))
                .toList();
    }

    private List<PolicyDto> getAllMockPolicies() {
        return Arrays.asList(
            createMockPolicy("POLICY-123", "CUST-001", "AUTO_INSURANCE", 
                new BigDecimal("156.00"), PolicyDto.PolicyStatus.OVERDUE, LocalDate.now().minusDays(3)),
            createMockPolicy("POLICY-456", "CUST-001", "HOME_INSURANCE", 
                new BigDecimal("89.00"), PolicyDto.PolicyStatus.ACTIVE, LocalDate.now().plusDays(15)),
            createMockPolicy("POLICY-789", "CUST-001", "LIFE_INSURANCE", 
                new BigDecimal("156.00"), PolicyDto.PolicyStatus.ACTIVE, LocalDate.now().plusDays(15)),
            createMockPolicy("POLICY-111", "CUST-002", "AUTO_INSURANCE", 
                new BigDecimal("175.00"), PolicyDto.PolicyStatus.GRACE_PERIOD, LocalDate.now().minusDays(7)),
            createMockPolicy("POLICY-222", "CUST-003", "HOME_INSURANCE", 
                new BigDecimal("120.00"), PolicyDto.PolicyStatus.ACTIVE, LocalDate.now().plusDays(10))
        );
    }

    private PolicyDto createMockPolicy(String id, String customerId, String policyType, 
                                   BigDecimal premiumAmount, PolicyDto.PolicyStatus status, LocalDate nextDueDate) {
        return PolicyDto.builder()
                .policyId(id)
                .policyNumber("POL-" + id.replace("POLICY-", ""))
                .customerId(customerId)
                .policyType(policyType)
                .status(status)
                .effectiveDate(LocalDate.now().minusMonths(6))
                .expirationDate(LocalDate.now().plusMonths(6))
                .premiumAmount(premiumAmount)
                .frequency("MONTHLY")
                .gracePeriodDays(10)
                .nextDueDate(nextDueDate)
                .build();
    }

    private Map<String, Object> createMockPremiumSchedule(PolicyDto policy) {
        boolean isOverdue = policy.getStatus() == PolicyDto.PolicyStatus.OVERDUE;
        long daysOverdue = isOverdue ? 
            LocalDate.now().toEpochDay() - policy.getNextDueDate().toEpochDay() : 0;
        
        BigDecimal lateFee = isOverdue ? new BigDecimal("15.00") : BigDecimal.ZERO;
        BigDecimal totalDue = policy.getPremiumAmount().add(lateFee);

        List<Map<String, Object>> scheduleItems = Arrays.asList(
            Map.of(
                "dueDate", policy.getNextDueDate().toString(),
                "amount", policy.getPremiumAmount(),
                "status", isOverdue ? "overdue" : "pending",
                "lateFee", lateFee
            ),
            Map.of(
                "dueDate", policy.getNextDueDate().plusMonths(1).toString(),
                "amount", policy.getPremiumAmount(),
                "status", "pending",
                "lateFee", BigDecimal.ZERO
            ),
            Map.of(
                "dueDate", policy.getNextDueDate().plusMonths(2).toString(),
                "amount", policy.getPremiumAmount(),
                "status", "pending",
                "lateFee", BigDecimal.ZERO
            )
        );

        // Use HashMap instead of Map.of() to avoid the 10 key-value pair limit
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("policyId", policy.getPolicyId());
        schedule.put("policyType", policy.getPolicyType());
        schedule.put("premiumAmount", policy.getPremiumAmount());
        schedule.put("billingFrequency", policy.getFrequency());
        schedule.put("nextDueDate", policy.getNextDueDate().toString());
        schedule.put("gracePeriodDays", policy.getGracePeriodDays());
        schedule.put("status", policy.getStatus().toString());
        schedule.put("daysOverdue", (int) daysOverdue);
        schedule.put("lateFee", lateFee);
        schedule.put("totalAmountDue", totalDue);
        schedule.put("schedule", scheduleItems);
        
        return schedule;
    }
}
