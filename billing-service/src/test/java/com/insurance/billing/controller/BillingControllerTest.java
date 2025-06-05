package com.insurance.billing.controller;

import com.insurance.shared.enums.PaymentStatus;
import com.insurance.billing.service.BillingService;
import com.insurance.shared.dto.BillingDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.insurance.shared.dto.PaymentRequestDto;
import com.insurance.shared.enums.BillingStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(BillingController.class)
class BillingControllerTest {

    @BeforeEach
    void setUp() {
        // Mock premium details
        when(billingService.getPremiumDetails(anyString()))
                .thenReturn(Optional.of(Map.of(
                        "policyId", "POLICY-123",
                        "premiumAmount", 150.00,
                        "frequency", "MONTHLY",
                        "message", "Premium calculation endpoint - implementation pending"
                )));

        // Mock calculate premium
        when(billingService.calculatePremiumFromRequest(any()))
                .thenReturn(Map.of(
                        "calculatedPremium", 150.00,
                        "frequency", "MONTHLY",
                        "effectiveDate", LocalDateTime.now(),
                        "message", "Premium calculation completed - implementation pending"
                ));

        // Mock due premiums
        when(billingService.getDuePremiums())
                .thenReturn(Map.of(
                        "duePremiums", "None",
                        "message", "Due premiums endpoint - implementation pending"
                ));
    }
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BillingService billingService;

    @Test
    void shouldHandleHealthCheck() throws Exception {
        mockMvc.perform(get("/api/billing/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service").value("billing-service"))
                .andExpect(jsonPath("$.message").value("Hello from Billing Service!"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldGetPremiumForPolicy() throws Exception {
        // Test with a policy ID
        mockMvc.perform(get("/api/billing/POLICY-123/premium"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.policyId").value("POLICY-123"))
                .andExpect(jsonPath("$.premiumAmount").value(150.00))
                .andExpect(jsonPath("$.frequency").value("MONTHLY"))
                .andExpect(jsonPath("$.message").value("Premium calculation endpoint - implementation pending"));
    }

    @Test
    void shouldCalculatePremium() throws Exception {
        String requestBody = "{"
                + "\"policyType\":\"AUTO_INSURANCE\","
                + "\"coverageAmount\":50000,"
                + "\"riskFactors\":[\"GOOD_DRIVER\"]"
                + "}";

        mockMvc.perform(post("/api/billing/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.calculatedPremium").value(150.00))
                .andExpect(jsonPath("$.frequency").value("MONTHLY"))
                .andExpect(jsonPath("$.effectiveDate").exists())
                .andExpect(jsonPath("$.message").value("Premium calculation completed - implementation pending"));
    }

    @Test
    void shouldGetDuePremiums() throws Exception {
        mockMvc.perform(get("/api/billing/due"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.duePremiums").value("None"))
                .andExpect(jsonPath("$.message").value("Due premiums endpoint - implementation pending"));
    }

    @Test
    void shouldHandleDifferentPolicyIds() throws Exception {
        // Test with different policy IDs to ensure path variable handling
        String[] policyIds = { "POLICY-456", "AUTO-789", "HOME-123" };

        for (String policyId : policyIds) {
            mockMvc.perform(get("/api/billing/" + policyId + "/premium"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.policyId").value(policyId))
                    .andExpect(jsonPath("$.premiumAmount").value(150.00));
        }
    }

    @Test
    void shouldAcceptEmptyCalculateRequest() throws Exception {
        // Test with minimal request
        String emptyRequest = "{}";

        mockMvc.perform(post("/api/billing/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculatedPremium").value(150.00))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldValidateJsonContentType() throws Exception {
        // Test POST endpoint requires JSON content type
        mockMvc.perform(post("/api/billing/calculate")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void shouldGetCustomerBillings() throws Exception {
        when(billingService.getBillingsByCustomer("CUST-1"))
                .thenReturn(List.of(createTestBillingDto()));

        mockMvc.perform(get("/api/billing/customer/CUST-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].policyId").value("POL-1"))
                .andExpect(jsonPath("$[0].amount").value(100.00));
    }

    @Test
    void shouldSubmitPayment() throws Exception {
        PaymentRequestDto request = PaymentRequestDto.builder()
                .billId("BILL-1")
                .amount(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/billing/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private BillingDto createTestBillingDto() {
        return BillingDto.builder()
                .id("BILL-1")
                .policyId("POL-1")
                .customerId("CUST-1")
                .amount(new BigDecimal("100.00"))
                .dueDate(LocalDate.now().plusDays(10))
                .status(BillingStatus.PENDING)
                .build();
    }
}
