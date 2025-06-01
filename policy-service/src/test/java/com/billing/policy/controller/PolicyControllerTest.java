package com.billing.policy.controller;

import com.billing.policy.service.PolicyService;
import com.billing.shared.model.Policy;
import com.billing.shared.model.PremiumSchedule;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolicyService policyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetPolicyById() throws Exception {
        // Given
        Policy policy = createTestPolicy();
        when(policyService.findById("POLICY-123")).thenReturn(Optional.of(policy));

        // When & Then
        mockMvc.perform(get("/api/policies/POLICY-123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("POLICY-123"))
                .andExpect(jsonPath("$.customerId").value("CUST-001"))
                .andExpect(jsonPath("$.policyType").value("AUTO_INSURANCE"))
                .andExpect(jsonPath("$.premiumAmount").value(156.00))
                .andExpect(jsonPath("$.status").value("CURRENT"));
    }

    @Test
    void shouldReturn404WhenPolicyNotFound() throws Exception {
        // Given
        when(policyService.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/policies/NONEXISTENT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetPremiumScheduleForPolicy() throws Exception {
        // Given
        PremiumSchedule schedule = createTestPremiumSchedule();
        when(policyService.getPremiumSchedule("POLICY-123")).thenReturn(schedule);

        // When & Then
        mockMvc.perform(get("/api/policies/POLICY-123/premium-schedule"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.policyId").value("POLICY-123"))
                .andExpect(jsonPath("$.premiumAmount").value(156.00))
                .andExpect(jsonPath("$.billingFrequency").value("MONTHLY"))
                .andExpect(jsonPath("$.nextDueDate").exists())
                .andExpect(jsonPath("$.gracePeriodDays").value(10))
                .andExpect(jsonPath("$.status").value("CURRENT"));
    }

    @Test
    void shouldGetPoliciesForCustomer() throws Exception {
        // Given
        Policy policy1 = createTestPolicy();
        Policy policy2 = createTestPolicy();
        policy2.setId("POLICY-456");
        policy2.setPolicyType("HOME_INSURANCE");
        
        when(policyService.findByCustomerId("CUST-001"))
                .thenReturn(Arrays.asList(policy1, policy2));

        // When & Then
        mockMvc.perform(get("/api/policies/customer/CUST-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("POLICY-123"))
                .andExpect(jsonPath("$[1].id").value("POLICY-456"))
                .andExpect(jsonPath("$[1].policyType").value("HOME_INSURANCE"));
    }

    @Test
    void shouldReturnEmptyListWhenCustomerHasNoPolicies() throws Exception {
        // Given
        when(policyService.findByCustomerId("CUST-NONE"))
                .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/policies/customer/CUST-NONE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldHandleHealthCheck() throws Exception {
        mockMvc.perform(get("/api/policies/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Policy Service is running"));
    }

    private Policy createTestPolicy() {
        Policy policy = new Policy();
        policy.setId("POLICY-123");
        policy.setCustomerId("CUST-001");
        policy.setPolicyType("AUTO_INSURANCE");
        policy.setPremiumAmount(new BigDecimal("156.00"));
        policy.setBillingFrequency("MONTHLY");
        policy.setStatus(PolicyStatus.CURRENT);
        policy.setNextDueDate(LocalDate.now().plusDays(15));
        policy.setGracePeriodDays(10);
        return policy;
    }

    private PremiumSchedule createTestPremiumSchedule() {
        PremiumSchedule schedule = new PremiumSchedule();
        schedule.setPolicyId("POLICY-123");
        schedule.setPremiumAmount(new BigDecimal("156.00"));
        schedule.setBillingFrequency("MONTHLY");
        schedule.setNextDueDate(LocalDate.now().plusDays(15));
        schedule.setGracePeriodDays(10);
        schedule.setStatus("CURRENT");
        schedule.setDaysOverdue(0);
        schedule.setLateFee(BigDecimal.ZERO);
        schedule.setTotalAmountDue(new BigDecimal("156.00"));
        return schedule;
    }
}
