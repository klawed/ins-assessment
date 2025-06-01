package com.billing.billing.controller;

import com.billing.billing.service.BillingService;
import com.billing.shared.model.DelinquentPolicy;
import com.billing.shared.model.PremiumCalculation;
import com.billing.shared.model.PolicyStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BillingController.class)
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillingService billingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetDelinquentPolicies() throws Exception {
        // Given
        List<DelinquentPolicy> delinquentPolicies = Arrays.asList(
            createDelinquentPolicy("POLICY-123", 3),
            createDelinquentPolicy("POLICY-456", 7)
        );
        
        when(billingService.getDelinquentPolicies(anyInt(), anyInt(), anyInt(), anyString()))
                .thenReturn(delinquentPolicies);

        // When & Then
        mockMvc.perform(get("/api/billing/policies/delinquent")
                .param("limit", "50")
                .param("offset", "0")
                .param("minDaysOverdue", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.delinquentPolicies").isArray())
                .andExpect(jsonPath("$.delinquentPolicies[0].policyId").value("POLICY-123"))
                .andExpect(jsonPath("$.delinquentPolicies[0].daysOverdue").value(3))
                .andExpect(jsonPath("$.delinquentPolicies[0].status").value("OVERDUE"))
                .andExpect(jsonPath("$.delinquentPolicies[1].daysOverdue").value(7));
    }

    @Test
    void shouldCalculatePremium() throws Exception {
        // Given
        PremiumCalculation calculation = createPremiumCalculation();
        when(billingService.calculatePremium(any()))
                .thenReturn(calculation);

        String requestBody = "{"
                + "\"policyType\":\"AUTO_INSURANCE\","
                + "\"coverageAmount\":50000,"
                + "\"riskFactors\":[\"GOOD_DRIVER\"]"
                + "}";

        // When & Then
        mockMvc.perform(post("/api/billing/calculate-premium")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.monthlyPremium").value(156.00))
                .andExpect(jsonPath("$.annualPremium").value(1872.00))
                .andExpect(jsonPath("$.policyType").value("AUTO_INSURANCE"))
                .andExpected(jsonPath("$.effectiveDate").exists());
    }

    @Test
    void shouldGetPolicyBillingStatus() throws Exception {
        // Given
        when(billingService.getPolicyBillingStatus("POLICY-123"))
                .thenReturn(createPolicyBillingStatus());

        // When & Then
        mockMvc.perform(get("/api/billing/policies/POLICY-123/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpected(jsonPath("$.policyId").value("POLICY-123"))
                .andExpect(jsonPath("$.status").value("CURRENT"))
                .andExpect(jsonPath("$.nextDueDate").exists())
                .andExpect(jsonPath("$.amountDue").value(156.00));
    }

    @Test
    void shouldGetPremiumDetails() throws Exception {
        // Given
        when(billingService.getPremiumDetails("POLICY-123"))
                .thenReturn(createPremiumDetails());

        // When & Then
        mockMvc.perform(get("/api/billing/POLICY-123/premium"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.policyId").value("POLICY-123"))
                .andExpected(jsonPath("$.basePremium").value(140.00))
                .andExpect(jsonPath("$.fees").value(16.00))
                .andExpect(jsonPath("$.totalPremium").value(156.00));
    }

    @Test
    void shouldFilterDelinquentPoliciesByCustomer() throws Exception {
        // Given
        List<DelinquentPolicy> customerPolicies = Arrays.asList(
            createDelinquentPolicy("POLICY-123", 3)
        );
        
        when(billingService.getDelinquentPolicies(anyInt(), anyInt(), anyInt(), eq("CUST-001")))
                .thenReturn(customerPolicies);

        // When & Then
        mockMvc.perform(get("/api/billing/policies/delinquent")
                .param("customerId", "CUST-001")
                .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.delinquentPolicies").isArray())
                .andExpect(jsonPath("$.delinquentPolicies[0].customerId").value("CUST-001"));
    }

    @Test
    void shouldReturnEmptyListWhenNoDelinquentPolicies() throws Exception {
        // Given
        when(billingService.getDelinquentPolicies(anyInt(), anyInt(), anyInt(), anyString()))
                .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/billing/policies/delinquent"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.delinquentPolicies").isArray())
                .andExpected(jsonPath("$.delinquentPolicies").isEmpty());
    }

    @Test
    void shouldReturn404ForNonexistentPolicy() throws Exception {
        // Given
        when(billingService.getPolicyBillingStatus("NONEXISTENT"))
                .thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/billing/policies/NONEXISTENT/status"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidatePremiumCalculationRequest() throws Exception {
        // Given - invalid request with missing required fields
        String invalidRequest = "{}";

        // When & Then
        mockMvc.perform(post("/api/billing/calculate-premium")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleHealthCheck() throws Exception {
        mockMvc.perform(get("/api/billing/hello"))
                .andExpected(status().isOk())
                .andExpect(content().string("Billing Service is running"));
    }

    private DelinquentPolicy createDelinquentPolicy(String policyId, int daysOverdue) {
        DelinquentPolicy policy = new DelinquentPolicy();
        policy.setPolicyId(policyId);
        policy.setCustomerId("CUST-001");
        policy.setCustomerName("John Doe");
        policy.setPolicyType("AUTO_INSURANCE");
        policy.setPremiumAmount(new BigDecimal("156.00"));
        policy.setDueDate(LocalDate.now().minusDays(daysOverdue));
        policy.setDaysOverdue(daysOverdue);
        policy.setLateFee(new BigDecimal("15.00"));
        policy.setTotalAmountDue(new BigDecimal("171.00"));
        policy.setStatus("OVERDUE");
        policy.setGracePeriodExpires(LocalDate.now().plusDays(10 - daysOverdue));
        policy.setLastPaymentDate(LocalDate.now().minusMonths(1));
        return policy;
    }

    private PremiumCalculation createPremiumCalculation() {
        PremiumCalculation calculation = new PremiumCalculation();
        calculation.setMonthlyPremium(new BigDecimal("156.00"));
        calculation.setAnnualPremium(new BigDecimal("1872.00"));
        calculation.setPolicyType("AUTO_INSURANCE");
        calculation.setEffectiveDate(LocalDate.now());
        calculation.setRiskScore("LOW");
        return calculation;
    }

    private Object createPolicyBillingStatus() {
        // Return a mock billing status object
        return new Object() {
            public String getPolicyId() { return "POLICY-123"; }
            public String getStatus() { return "CURRENT"; }
            public LocalDate getNextDueDate() { return LocalDate.now().plusDays(15); }
            public BigDecimal getAmountDue() { return new BigDecimal("156.00"); }
        };
    }

    private Object createPremiumDetails() {
        // Return a mock premium details object
        return new Object() {
            public String getPolicyId() { return "POLICY-123"; }
            public BigDecimal getBasePremium() { return new BigDecimal("140.00"); }
            public BigDecimal getFees() { return new BigDecimal("16.00"); }
            public BigDecimal getTotalPremium() { return new BigDecimal("156.00"); }
        };
    }
}
