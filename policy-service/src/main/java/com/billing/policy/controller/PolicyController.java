package com.billing.policy.controller;

import com.billing.shared.dto.PolicyDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

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
    public ResponseEntity<PolicyDto> getPolicyById(@PathVariable String policyId) {
        // Mock policy data for testing
        PolicyDto policy = PolicyDto.builder()
                .policyId(policyId)
                .policyNumber("POL-" + policyId.replace("POLICY-", ""))
                .customerId("CUST-123")
                .policyType("AUTO")
                .status(PolicyDto.PolicyStatus.ACTIVE)
                .effectiveDate(LocalDate.now().minusMonths(6))
                .expirationDate(LocalDate.now().plusMonths(6))
                .premiumAmount(new BigDecimal("500.00"))
                .frequency("MONTHLY")
                .gracePeriodDays(30)
                .build();

        return ResponseEntity.ok(policy);
    }

    @GetMapping("/{policyId}/schedule")
    public ResponseEntity<Map<String, Object>> getPolicySchedule(@PathVariable String policyId) {
        Map<String, Object> schedule = Map.of(
                "policyId", policyId,
                "premiumSchedule", "Monthly - $100"
        );
        return ResponseEntity.ok(schedule);
    }
}
