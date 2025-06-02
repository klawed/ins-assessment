package com.insurance.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.payment.controller.PaymentController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {
        // Given
        Map<String, Object> request = createPaymentRequest();

        // When & Then
        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.amount").value(171.00))
                .andExpect(jsonPath("$.policyId").value("POLICY-123"));
    }

    @Test
    void shouldRetryFailedPayment() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/payments/TXN-2024-001236/retry"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.originalTransactionId").value("TXN-2024-001236"))
                .andExpect(jsonPath("$.newTransactionId").exists())
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.retryAttempt").value(1));
    }

    @Test
    void shouldGetPaymentHistory() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/payments/history")
                .param("policyId", "POLICY-123")
                .param("status", "SUCCESS")
                .param("limit", "10")
                .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetDelinquentPolicies() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/payments/delinquent")
                .param("limit", "50")
                .param("offset", "0")
                .param("minDaysOverdue", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCount").exists())
                .andExpect(jsonPath("$.delinquentPolicies").isArray());
    }

    @Test
    void shouldGetPaymentStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/payments/TXN-2024-001235/status"))
                .andExpect(status().isNotFound()); // Will be 404 since it's not in mock data
    }

    @Test
    void shouldHandleHealthCheck() throws Exception {
        mockMvc.perform(get("/api/payments/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service").value("payment-service"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    private Map<String, Object> createPaymentRequest() {
        return Map.of(
            "policyId", "POLICY-123",
            "amount", 171.00,
            "paymentMethod", Map.of(
                "type", "credit_card",
                "cardNumber", "4532123456789012"
            )
        );
    }
}
