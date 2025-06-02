package com.insurance.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.payment.service.PaymentService;
import com.insurance.shared.dto.PaymentRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {
        // Given
        PaymentRequestDto request = PaymentRequestDto.builder()
                .billId("BILL-1")
                .policyId("POLICY-123")
                .amount(new BigDecimal("171.00"))
                .paymentMethod("CREDIT_CARD")
                .build();

        when(paymentService.processPayment(any())).thenReturn(Map.of(
            "transactionId", "TXN-12345",
            "status", "COMPLETED",
            "amount", 171.00,
            "policyId", "POLICY-123",
            "billId", "BILL-1"
        ));

        // When & Then
        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").value("TXN-12345"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.amount").value(171.00))
                .andExpect(jsonPath("$.policyId").value("POLICY-123"))
                .andExpect(jsonPath("$.billId").value("BILL-1"));
    }

    @Test
    void shouldRetryFailedPayment() throws Exception {
        // Given
        when(paymentService.retryFailedPayment("TXN-2024-001236")).thenReturn(Map.of(
            "originalTransactionId", "TXN-2024-001236",
            "newTransactionId", "TXN-2024-001237",
            "status", "PROCESSING",
            "retryAttempt", 1
        ));

        // When & Then
        mockMvc.perform(post("/api/payments/TXN-2024-001236/retry"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.originalTransactionId").value("TXN-2024-001236"))
                .andExpect(jsonPath("$.newTransactionId").value("TXN-2024-001237"))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.retryAttempt").value(1));
    }

    @Test
    void shouldGetPaymentHistory() throws Exception {
        // Given
        when(paymentService.getPaymentHistory("POLICY-123", "SUCCESS", 10, 0)).thenReturn(List.of(
            Map.of("transactionId", "TXN-12345", "status", "SUCCESS", "amount", 171.00),
            Map.of("transactionId", "TXN-12346", "status", "SUCCESS", "amount", 200.00)
        ));

        // When & Then
        mockMvc.perform(get("/api/payments/history")
                .param("policyId", "POLICY-123")
                .param("status", "SUCCESS")
                .param("limit", "10")
                .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].transactionId").value("TXN-12345"))
                .andExpect(jsonPath("$[1].transactionId").value("TXN-12346"));
    }

    @Test
    void shouldGetDelinquentPolicies() throws Exception {
        // Given
        when(paymentService.getDelinquentPolicies(50, 0, 1, null)).thenReturn(Map.of(
            "totalCount", 2,
            "delinquentPolicies", List.of("POLICY-123", "POLICY-456")
        ));

        // When & Then
        mockMvc.perform(get("/api/payments/delinquent")
                .param("limit", "50")
                .param("offset", "0")
                .param("minDaysOverdue", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.delinquentPolicies").isArray())
                .andExpect(jsonPath("$.delinquentPolicies[0]").value("POLICY-123"))
                .andExpect(jsonPath("$.delinquentPolicies[1]").value("POLICY-456"));
    }

    @Test
    void shouldGetPaymentStatus() throws Exception {
        // Given
        when(paymentService.getPaymentStatus("TXN-2024-001235")).thenReturn(Map.of(
            "transactionId", "TXN-2024-001235",
            "status", "COMPLETED",
            "amount", 171.00,
            "policyId", "POLICY-123",
            "billId", "BILL-1"
        ));

        // When & Then
        mockMvc.perform(get("/api/payments/TXN-2024-001235/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").value("TXN-2024-001235"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.amount").value(171.00))
                .andExpect(jsonPath("$.policyId").value("POLICY-123"))
                .andExpect(jsonPath("$.billId").value("BILL-1"));
    }

    @Test
    void shouldHandleHealthCheck() throws Exception {
        mockMvc.perform(get("/api/payments/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service").value("payment-service"))
                .andExpect(jsonPath("$.status").value("UP"));
    }
}