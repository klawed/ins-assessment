package com.billing.policy.config;

import com.billing.policy.entity.Policy;
import com.billing.policy.repository.PolicyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

@Configuration
@Profile("dev")
public class TestDataSeeder {

    @Bean
    public CommandLineRunner seedData(PolicyRepository policyRepository) {
        return args -> {
            if (policyRepository.count() == 0) {
                Policy policy1 = Policy.builder()
                        .id("POLICY-123")
                        .policyNumber("POL-123")
                        .customerId("CUST-001")
                        .policyType("AUTO")
                        .status(Policy.PolicyStatus.ACTIVE)
                        .effectiveDate(LocalDate.now().minusMonths(6))
                        .expirationDate(LocalDate.now().plusMonths(6))
                        .premiumAmount(new BigDecimal("156.00"))
                        .frequency("MONTHLY")
                        .gracePeriodDays(10)
                        .nextDueDate(LocalDate.now().plusDays(15))
                        .build();

                Policy policy2 = Policy.builder()
                        .id("POLICY-456")
                        .policyNumber("POL-456")
                        .customerId("CUST-001")
                        .policyType("HOME")
                        .status(Policy.PolicyStatus.OVERDUE)
                        .effectiveDate(LocalDate.now().minusMonths(3))
                        .expirationDate(LocalDate.now().plusMonths(9))
                        .premiumAmount(new BigDecimal("245.00"))
                        .frequency("MONTHLY")
                        .gracePeriodDays(10)
                        .nextDueDate(LocalDate.now().minusDays(5))
                        .build();

                policyRepository.saveAll(Arrays.asList(policy1, policy2));
                System.out.println("Test data seeded successfully");
            }
        };
    }
}