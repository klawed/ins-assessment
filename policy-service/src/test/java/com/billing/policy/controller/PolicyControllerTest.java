package com.billing.policy.controller;

import com.billing.shared.dto.PolicyDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyController.class)
@ActiveProfiles("test")
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment env;

    @Test
    void shouldGetPolicyById() throws Exception {
        // Test with existing mock policy ID
        mockMvc.perform(get("/api/policies/POLICY-123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.policyId").value("POLICY-123"))
                .andExpect(jsonPath("$.customerId").value("CUST-001"))
                .andExpect(jsonPath("$.policyType").value("AUTO_INSURANCE"))
                .andExpect(jsonPath("$.premiumAmount").value(156.00))
                .andExpect(jsonPath("$.status").value("OVERDUE"))
                .andExpect(jsonPath("$.frequency").value("MONTHLY"))
                .andExpect(jsonPath("$.gracePeriodDays").value(10));
    }

    @Test
    void shouldReturn404WhenPolicyNotFound() throws Exception {
        // Test with non-existent policy ID
        mockMvc.perform(get("/api/policies/NONEXISTENT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetPremiumScheduleForPolicy() throws Exception {
        // Test with existing mock policy ID
        mockMvc.perform(get("/api/policies/POLICY-123/premium-schedule"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.policyId").value("POLICY-123"))
                .andExpect(jsonPath("$.premiumAmount").value(156.00))
                .andExpect(jsonPath("$.billingFrequency").value("MONTHLY"))
                .andExpect(jsonPath("$.nextDueDate").exists())
                .andExpect(jsonPath("$.gracePeriodDays").value(10))
                .andExpect(jsonPath("$.status").value("OVERDUE"))
                .andExpect(jsonPath("$.daysOverdue").exists())
                .andExpect(jsonPath("$.lateFee").value(15.00))
                .andExpect(jsonPath("$.totalAmountDue").value(171.00))
                .andExpect(jsonPath("$.schedule").isArray())
                .andExpect(jsonPath("$.schedule[0].status").value("overdue"));
    }

    @Test
    void shouldGetPremiumScheduleForActivePolicy() throws Exception {
        // Test with active policy
        mockMvc.perform(get("/api/policies/POLICY-456/premium-schedule"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.policyId").value("POLICY-456"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.daysOverdue").value(0))
                .andExpect(jsonPath("$.lateFee").value(0))
                .andExpect(jsonPath("$.schedule[0].status").value("pending"));
    }

    @Test
    void shouldGetPoliciesForCustomer() throws Exception {
        // Test with customer who has multiple policies
        mockMvc.perform(get("/api/policies/customer/CUST-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].policyId").value("POLICY-123"))
                .andExpect(jsonPath("$[0].customerId").value("CUST-001"))
                .andExpect(jsonPath("$[1].policyId").value("POLICY-456"))
                .andExpect(jsonPath("$[1].policyType").value("HOME_INSURANCE"))
                .andExpect(jsonPath("$[2].policyId").value("POLICY-789"))
                .andExpect(jsonPath("$[2].policyType").value("LIFE_INSURANCE"));
    }

    @Test
    void shouldGetPoliciesForCustomerWithSinglePolicy() throws Exception {
        // Test with customer who has one policy
        mockMvc.perform(get("/api/policies/customer/CUST-002"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].policyId").value("POLICY-111"))
                .andExpect(jsonPath("$[0].customerId").value("CUST-002"))
                .andExpect(jsonPath("$[0].status").value("GRACE_PERIOD"));
    }

    @Test
    void shouldReturnEmptyListWhenCustomerHasNoPolicies() throws Exception {
        // Test with customer who has no policies
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service").value("policy-service"))
                .andExpect(jsonPath("$.message").value("Hello from Policy Service!"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldReturn404ForPremiumScheduleWhenPolicyNotFound() throws Exception {
        mockMvc.perform(get("/api/policies/NONEXISTENT/premium-schedule"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateGracePeriodPolicy() throws Exception {
        // Test grace period policy behavior
        mockMvc.perform(get("/api/policies/POLICY-111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyId").value("POLICY-111"))
                .andExpect(jsonPath("$.status").value("GRACE_PERIOD"))
                .andExpect(jsonPath("$.customerId").value("CUST-002"));
        
        // Test grace period premium schedule
        mockMvc.perform(get("/api/policies/POLICY-111/premium-schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GRACE_PERIOD"))
                .andExpect(jsonPath("$.daysOverdue").value(0))
                .andExpect(jsonPath("$.lateFee").value(0));
    }

    @Test
    void verifyTestProfile() {
        assertThat(Arrays.asList(env.getActiveProfiles())).contains("test");
    }
}
