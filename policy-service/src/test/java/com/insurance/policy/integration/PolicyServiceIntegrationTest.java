package com.insurance.policy.integration;

import com.insurance.policy.PolicyServiceApplication;
import com.insurance.policy.service.PolicyService;
import com.insurance.shared.dto.PolicyDto;
import com.insurance.shared.enums.PaymentFrequency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = PolicyServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class PolicyServiceIntegrationTest {

    @Autowired
    private PolicyService policyService;

    void shouldCreateAndRetrievePolicy() {
        PolicyDto policy = PolicyDto.builder()
            .id("TEST-POL-1")
            .policyNumber("TEST-123")
            .customerId("TEST-CUST-1")
            .policyType("AUTO")
            .effectiveDate(LocalDate.now())
            .expirationDate(LocalDate.now().plusYears(1))
            .premiumAmount(new BigDecimal("100.00"))
            .frequency(PaymentFrequency.MONTHLY)
            .build();

        PolicyDto created = policyService.createPolicy(policy);
        PolicyDto retrieved = policyService.getPolicyById(created.getId())
            .orElseThrow();

        assertThat(retrieved)
            .usingRecursiveComparison()
            .isEqualTo(created);
    }
}