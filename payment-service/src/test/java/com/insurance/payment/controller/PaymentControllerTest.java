package com.insurance.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.payment.service.PaymentService;
import com.insurance.shared.dto.PaymentRequestDto;
import com.insurance.shared.dto.PaymentDto;
import com.insurance.shared.enums.PaymentStatus;
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

        PaymentDto response = PaymentDto.builder()
                .id("TXN-12345")
                .policyId("POLICY-123")
                .amount(new BigDecimal("171.00"))
                .status(PaymentStatus.COMPLETED)
                .paymentMethod("CREDIT_CARD")
                .build();

        when(paymentService.processPayment(any(PaymentRequestDto.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("TXN-12345"))
                .andExpect(jsonPath("$.policyId").value("POLICY-123"))
                .andExpect(jsonPath("$.amount").value(171.00))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"));
    }

    @Test
    void shouldGetPaymentHistory() throws Exception {
        // Given
        List<PaymentDto> mockPayments = List.of(
                PaymentDto.builder()
                        .id("TXN-12345")
                        .policyId("POLICY-123")
                        .amount(new BigDecimal("171.00"))
                        .status(PaymentStatus.SUCCESS)
                        .paymentMethod("CREDIT_CARD")
                        .build(),
                PaymentDto.builder()
                        .id("TXN-12346")
                        .policyId("POLICY-123")
                        .amount(new BigDecimal("200.00"))
                        .status(PaymentStatus.SUCCESS)
                        .paymentMethod("CREDIT_CARD")
                        .build()
        );

        when(paymentService.getPaymentHistory("POLICY-123", "SUCCESS", 10, 0)).thenReturn(mockPayments);

        // When & Then
        mockMvc.perform(get("/api/payments/history")
                .param("policyId", "POLICY-123")
                .param("status", "SUCCESS")
                .param("limit", "10")
                .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("TXN-12345"))
                .andExpect(jsonPath("$[1].id").value("TXN-12346"));
    }

    @Test
    void shouldGetDelinquentPolicies() throws Exception {
        // Given
        when(paymentService.getDelinquentPolicies(50, 0, 1, null)).thenReturn(Map.of(
                "totalCount", 2,
                "delinquentPolicies", List.of("POLICY-123", "POLICY-456")));

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
                "billId", "BILL-1"));

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