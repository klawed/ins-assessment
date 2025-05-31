package com.billing.billing.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@Slf4j
public class BillingController {

    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        log.info("Hello endpoint called at {}", LocalDateTime.now());
        
        return ResponseEntity.ok(Map.of(
            "service", "billing-service",
            "message", "Hello from Billing Service!",
            "timestamp", LocalDateTime.now(),
            "status", "UP"
        ));
    }

    @GetMapping("/{policyId}/premium")
    public ResponseEntity<Map<String, Object>> getPremium(@PathVariable String policyId) {
        log.info("Getting premium for policy ID: {}", policyId);
        
        // TODO: Implement actual premium calculation
        return ResponseEntity.ok(Map.of(
            "policyId", policyId,
            "premiumAmount", new BigDecimal("150.00"),
            "frequency", "MONTHLY",
            "message", "Premium calculation endpoint - implementation pending"
        ));
    }

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculatePremium(@RequestBody Map<String, Object> request) {
        log.info("Calculating premium for request: {}", request);
        
        // TODO: Implement actual premium calculation logic
        return ResponseEntity.ok(Map.of(
            "calculatedPremium", new BigDecimal("150.00"),
            "frequency", "MONTHLY",
            "effectiveDate", LocalDateTime.now(),
            "message", "Premium calculation completed - implementation pending"
        ));
    }

    @GetMapping("/due")
    public ResponseEntity<Map<String, Object>> getDuePremiums() {
        log.info("Getting due premiums");
        
        // TODO: Implement actual due premium retrieval
        return ResponseEntity.ok(Map.of(
            "duePremiums", "None",
            "message", "Due premiums endpoint - implementation pending"
        ));
    }
}