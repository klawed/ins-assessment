package com.insurance.policy.unit.controller;

import com.insurance.shared.dto.PolicyDto;
import com.insurance.shared.dto.PremiumScheduleDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.policy.controller.PolicyController;
import com.insurance.policy.service.PolicyService;
import com.insurance.shared.enums.PaymentFrequency;
import com.insurance.shared.enums.PolicyStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(
    value = PolicyController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@ActiveProfiles("test")
@WithMockUser
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment env;

    @MockBean
    private PolicyService policyService;

    @Test
    void shouldGetPolicyById() throws Exception {
        PolicyDto mockPolicy = PolicyDto.builder()
            .id("POLICY-123")
            .customerId("CUST-001")
            .policyType("AUTO_INSURANCE")
            .premiumAmount(new BigDecimal("156.00"))
            .status(PolicyStatus.OVERDUE)
            .frequency(PaymentFrequency.MONTHLY)
            .gracePeriodDays(10)
            .build();

        when(policyService.getPolicyById("POLICY-123")).thenReturn(Optional.of(mockPolicy));

        mockMvc.perform(get("/api/policies/POLICY-123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("POLICY-123"))
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
        PremiumScheduleDto mockSchedule = PremiumScheduleDto.builder()
                .policyId("POLICY-123")
                .premiumAmount(new BigDecimal("200.00"))
                .billingFrequency("MONTHLY")
                .nextDueDate(LocalDate.now().plusDays(5))
                .gracePeriodDays(10)
                .status("ACTIVE")
                .daysOverdue(null)
                .lateFee(null)
                .totalAmountDue(null)
                .schedule(List.of())
                .build();

        when(policyService.getPremiumScheduleForPolicy("POLICY-123")).thenReturn(Optional.of(mockSchedule));

        mockMvc.perform(get("/api/policies/POLICY-123/premium-schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyId").value("POLICY-123"))
                .andExpect(jsonPath("$.premiumAmount").value(200.00))
                .andExpect(jsonPath("$.billingFrequency").value("MONTHLY"))
                .andExpect(jsonPath("$.nextDueDate").exists())
                .andExpect(jsonPath("$.gracePeriodDays").value(10))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldGetPremiumScheduleForActivePolicy() throws Exception {
        PremiumScheduleDto mockSchedule = PremiumScheduleDto.builder()
                .policyId("POLICY-456")
                .premiumAmount(new BigDecimal("300.00"))
                .billingFrequency("MONTHLY")
                .nextDueDate(LocalDate.now().plusDays(10))
                .gracePeriodDays(15)
                .status("ACTIVE")
                .daysOverdue(null)
                .lateFee(null)
                .totalAmountDue(null)
                .schedule(List.of())
                .build();

        when(policyService.getPremiumScheduleForPolicy("POLICY-456")).thenReturn(Optional.of(mockSchedule));

        mockMvc.perform(get("/api/policies/POLICY-456/premium-schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyId").value("POLICY-456"))
                .andExpect(jsonPath("$.premiumAmount").value(300.00))
                .andExpect(jsonPath("$.billingFrequency").value("MONTHLY"))
                .andExpect(jsonPath("$.nextDueDate").exists())
                .andExpect(jsonPath("$.gracePeriodDays").value(15))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldGetPoliciesForCustomer() throws Exception {
        List<PolicyDto> mockPolicies = List.of(
            PolicyDto.builder().id("POLICY-001").customerId("CUST-001").build(),
            PolicyDto.builder().id("POLICY-002").customerId("CUST-001").build(),
            PolicyDto.builder().id("POLICY-003").customerId("CUST-001").build()
        );

        when(policyService.getPoliciesForCustomer("CUST-001")).thenReturn(mockPolicies);

        mockMvc.perform(get("/api/policies/customer/CUST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldGetPoliciesForCustomerWithSinglePolicy() throws Exception {
        List<PolicyDto> mockPolicies = List.of(
            PolicyDto.builder()
                .id("POLICY-001")
                .customerId("CUST-002")
                .policyNumber("PN-001")
                .policyType("AUTO")
                .status(PolicyStatus.ACTIVE)
                .effectiveDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(1))
                .premiumAmount(new BigDecimal("100.00"))
                .frequency(PaymentFrequency.MONTHLY)
                .nextDueDate(LocalDate.now().plusMonths(1))
                .gracePeriodDays(10)
                .build()
        );

        when(policyService.getPoliciesForCustomer("CUST-002")).thenReturn(mockPolicies);

        mockMvc.perform(get("/api/policies/customer/CUST-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("POLICY-001"))
                .andExpect(jsonPath("$[0].customerId").value("CUST-002"));
    }

    @Test
    void shouldReturnEmptyListWhenCustomerHasNoPolicies() throws Exception {
        when(policyService.getPoliciesForCustomer("CUST-NONE")).thenReturn(List.of());

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
        PremiumScheduleDto mockSchedule = PremiumScheduleDto.builder()
                .policyId("POLICY-123")
                .premiumAmount(new BigDecimal("200.00"))
                .billingFrequency("MONTHLY")
                .nextDueDate(LocalDate.now().plusDays(5))
                .gracePeriodDays(10)
                .status("ACTIVE")
                .daysOverdue(null)
                .lateFee(null)
                .totalAmountDue(null)
                .schedule(List.of())
                .build();

        when(policyService.getPremiumScheduleForPolicy("POLICY-123")).thenReturn(Optional.of(mockSchedule));

        mockMvc.perform(get("/api/policies/POLICY-123/premium-schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyId").value("POLICY-123"))
                .andExpect(jsonPath("$.premiumAmount").value(200.00))
                .andExpect(jsonPath("$.billingFrequency").value("MONTHLY"))
                .andExpect(jsonPath("$.nextDueDate").exists())
                .andExpect(jsonPath("$.gracePeriodDays").value(10))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

   
}
