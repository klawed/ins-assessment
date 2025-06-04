package com.insurance.policy.config;

import com.insurance.policy.entity.PolicyEntity;
import com.insurance.policy.repository.PolicyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestDataSeeder implements CommandLineRunner {

    private final PolicyRepository policyRepository;

    public TestDataSeeder(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Override
    public void run(String... args) {
        seedData();
    }

    public void seedData() {
        if (policyRepository.count() == 0) {
            List<PolicyEntity> policies = List.of(
                    PolicyEntity.builder()
                            .id("POLICY-001")
                            .policyNumber("PN-12345")
                            .customerId("CUST-001")
                            .policyType("LIFE")
                            .build()
            );

            policyRepository.saveAll(policies);
            System.out.println("Test data seeded successfully");
        }
    }
}