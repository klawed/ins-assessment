package com.insurance.policy.controller;

import com.insurance.policy.service.PolicyService;
import com.insurance.shared.dto.PolicyDto;
import com.insurance.shared.dto.PremiumScheduleDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/{id}")
    public ResponseEntity<PolicyDto> getPolicyById(@PathVariable String id) {
        return policyService.getPolicyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<PolicyDto>> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    @PostMapping
    public ResponseEntity<PolicyDto> createPolicy(@RequestBody PolicyDto policyDto) {
        return ResponseEntity.ok(policyService.createPolicy(policyDto));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PolicyDto>> getPoliciesForCustomer(@PathVariable String customerId) {
        List<PolicyDto> policies = policyService.getPoliciesForCustomer(customerId);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/{id}/premium-schedule")
    public ResponseEntity<PremiumScheduleDto> getPremiumScheduleForPolicy(@PathVariable String id) {
        return policyService.getPremiumScheduleForPolicy(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
