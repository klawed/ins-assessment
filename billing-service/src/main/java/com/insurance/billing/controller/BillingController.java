package com.insurance.billing.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.insurance.shared.dto.BillingDto;
import com.insurance.shared.dto.PaymentDto;
import com.insurance.shared.dto.PaymentRequestDto;
import com.insurance.billing.service.BillingService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/billing")
@Slf4j
@RequiredArgsConstructor
public class BillingController {
    private final BillingService billingService;

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

        Optional<Map<String, Object>> premiumDetails = billingService.getPremiumDetails(policyId);

        if (premiumDetails.isPresent()) {
            return ResponseEntity.ok(premiumDetails.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculatePremium(@RequestBody Map<String, Object> request) {
        log.info("Calculating premium for request: {}", request);

        Map<String, Object> result = billingService.calculatePremiumFromRequest(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/due")
    public ResponseEntity<Map<String, Object>> getDuePremiums() {
        log.info("Getting due premiums");

        Map<String, Object> result = billingService.getDuePremiums();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/delinquent")
    public ResponseEntity<List<Map<String, Object>>> getDelinquentPolicies() {
        log.info("Getting delinquent policies");

        List<Map<String, Object>> delinquentPolicies = billingService.getDelinquentPolicies();
        return ResponseEntity.ok(delinquentPolicies);
    }

    @GetMapping("/{policyId}/calculate")
    public ResponseEntity<Map<String, Object>> calculatePremiumForPolicy(@PathVariable String policyId) {
        log.info("Calculating premium for policy ID: {}", policyId);

        Map<String, Object> result = billingService.calculatePremium(policyId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{policyId}/status")
    public ResponseEntity<Map<String, Object>> updateBillingStatus(
            @PathVariable String policyId,
            @RequestBody Map<String, Object> statusUpdate) {
        log.info("Updating billing status for policy {} with: {}", policyId, statusUpdate);

        String paymentStatus = (String) statusUpdate.get("paymentStatus");
        billingService.updatePolicyBillingStatus(policyId, paymentStatus);

        return ResponseEntity.ok(Map.of(
                "policyId", policyId,
                "status", "updated",
                "timestamp", LocalDateTime.now()
        ));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BillingDto>> getBillingsByCustomer(@PathVariable String customerId) {
        log.info("Fetching billings for customer: {}", customerId);
        return ResponseEntity.ok(billingService.getBillingsByCustomer(customerId));
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<BillingDto>> getBillingsByPolicy(@PathVariable String policyId) {
        return ResponseEntity.ok(billingService.getBillingsByPolicy(policyId));
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentDto> submitPayment(@Valid @RequestBody PaymentRequestDto request) {
        return ResponseEntity.ok(billingService.processPayment(request));
    }
}