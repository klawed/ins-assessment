package com.insurance.policy.config;

import com.insurance.policy.entity.PolicyEntity;
import com.insurance.policy.repository.PolicyRepository;
import com.insurance.shared.enums.PaymentFrequency;
import com.insurance.shared.enums.PolicyStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
            LocalDateTime now = LocalDateTime.now();

            List<PolicyEntity> policies = List.of(
                    PolicyEntity.builder()
                            .id("POLICY-001")
                            .policyNumber("PN-12345")
                            .customerId("CUST-001")
                            .policyType("LIFE")
                            .status(PolicyStatus.ACTIVE) // Required field
                            .effectiveDate(now) // Required field
                            .expirationDate(now.plusYears(1)) // Set expiration date
                            .premiumAmount(new BigDecimal("500.00")) // Required field
                            .paymentFrequency(PaymentFrequency.MONTHLY) // Required field
                            .gracePeriodDays(30) // Optional but good to set
                            .nextDueDate(LocalDate.now().plusMonths(1)) // Set next due date
                            .build(),

                    PolicyEntity.builder()
                            .id("POLICY-002")
                            .policyNumber("PN-12346")
                            .customerId("CUST-002")
                            .policyType("AUTO")
                            .status(PolicyStatus.ACTIVE)
                            .effectiveDate(now)
                            .expirationDate(now.plusMonths(6))
                            .premiumAmount(new BigDecimal("750.00"))
                            .paymentFrequency(PaymentFrequency.QUARTERLY)
                            .gracePeriodDays(15)
                            .nextDueDate(LocalDate.now().plusMonths(3))
                            .build(),

                    PolicyEntity.builder()
                            .id("POLICY-003")
                            .policyNumber("PN-12347")
                            .customerId("CUST-003")
                            .policyType("HOME")
                            .status(PolicyStatus.PENDING)
                            .effectiveDate(now.plusDays(7))
                            .expirationDate(now.plusYears(1))
                            .premiumAmount(new BigDecimal("1200.00"))
                            .paymentFrequency(PaymentFrequency.ANNUAL)
                            .gracePeriodDays(45)
                            .nextDueDate(LocalDate.now().plusYears(1))
                            .build()
            );

            policyRepository.saveAll(policies);
            System.out.println("Test data seeded successfully - " + policies.size() + " policies created");
        } else {
            System.out.println("Test data already exists - skipping seed");
        }
    }
}