package com.billing.payment.controller;

import com.billing.payment.service.PaymentService;
import com.billing.shared.model.PaymentAttempt;
import com.billing.shared.model.PaymentStatus;
import com.billing.shared.model.PaymentRequest;
import com.billing.shared.model.PaymentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {
        // Given
        PaymentRequest request = createPaymentRequest();
        PaymentResponse response = createSuccessfulPaymentResponse();
        
        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").value("TXN-2024-001235"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(171.00))
                .andExpect(jsonPath("$.policyId").value("POLICY-123"))
                .andExpect(jsonPath("$.message").value("Payment processed successfully"));
    }

    @Test
    void shouldHandleFailedPayment() throws Exception {
        // Given
        PaymentRequest request = createPaymentRequest();
        PaymentResponse response = createFailedPaymentResponse();
        
        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").value("TXN-2024-001236"))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_FUNDS"))
                .andExpect(jsonPath("$.message").value("Payment declined due to insufficient funds"))
                .andExpect(jsonPath("$.retrySchedule.nextRetryDate").exists());
    }

    @Test
    void shouldRetryFailedPayment() throws Exception {
        // Given
        PaymentResponse retryResponse = createRetryResponse();
        when(paymentService.retryPayment("TXN-2024-001236"))
                .thenReturn(retryResponse);

        // When & Then
        mockMvc.perform(post("/api/payments/TXN-2024-001236/retry"))
                .andExpected(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.originalTransactionId").value("TXN-2024-001236"))
                .andExpect(jsonPath("$.newTransactionId").value("TXN-2024-001237"))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.retryAttempt").value(1));
    }

    @Test
    void shouldGetPaymentStatus() throws Exception {
        // Given
        PaymentAttempt attempt = createPaymentAttempt();
        when(paymentService.getPaymentStatus("TXN-2024-001235"))
                .thenReturn(attempt);

        // When & Then
        mockMvc.perform(get("/api/payments/TXN-2024-001235/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").value("TXN-2024-001235"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(171.00))
                .andExpect(jsonPath("$.policyId").value("POLICY-123"));
    }

    @Test
    void shouldGetPaymentHistory() throws Exception {
        // Given
        List<PaymentAttempt> history = Arrays.asList(
            createPaymentAttempt(),
            createPaymentAttempt()
        );
        
        when(paymentService.getPaymentHistory(anyString(), anyString(), any(), any(), anyInt(), anyInt()))
                .thenReturn(history);

        // When & Then
        mockMvc.perform(get("/api/payments/history")
                .param("policyId", "POLICY-123")
                .param("status", "SUCCESS")
                .param("limit", "10")
                .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].transactionId").value("TXN-2024-001235"))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    void shouldGetDelinquentPolicies() throws Exception {
        // Given
        // This would typically return a list of delinquent policies
        // For now, we'll assume the service handles this
        when(paymentService.getDelinquentPolicies(anyInt(), anyInt(), anyInt(), anyString()))
                .thenReturn(Arrays.asList(/* mock delinquent policies */));

        // When & Then
        mockMvc.perform(get("/api/payments/delinquent")
                .param("limit", "50")
                .param("offset", "0")
                .param("minDaysOverdue", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturn404ForNonexistentPayment() throws Exception {
        // Given
        when(paymentService.getPaymentStatus("NONEXISTENT"))
                .thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/payments/NONEXISTENT/status"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidatePaymentRequest() throws Exception {
        // Given - invalid request with missing required fields
        PaymentRequest invalidRequest = new PaymentRequest();
        
        // When & Then
        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleHealthCheck() throws Exception {
        mockMvc.perform(get("/api/payments/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment Service is running"));
    }

    private PaymentRequest createPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setPolicyId("POLICY-123");
        request.setAmount(new BigDecimal("171.00"));
        // Add payment method details
        return request;
    }

    private PaymentResponse createSuccessfulPaymentResponse() {
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId("TXN-2024-001235");
        response.setStatus("SUCCESS");
        response.setAmount(new BigDecimal("171.00"));
        response.setPolicyId("POLICY-123");
        response.setPaymentMethod("visa_****1234");
        response.setTimestamp(LocalDateTime.now());
        response.setConfirmationCode("CONF-ABC123");
        response.setProcessingFee(BigDecimal.ZERO);
        response.setMessage("Payment processed successfully");
        return response;
    }

    private PaymentResponse createFailedPaymentResponse() {
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId("TXN-2024-001236");
        response.setStatus("FAILED");
        response.setAmount(new BigDecimal("171.00"));
        response.setPolicyId("POLICY-123");
        response.setPaymentMethod("visa_****1234");
        response.setTimestamp(LocalDateTime.now());
        response.setErrorCode("INSUFFICIENT_FUNDS");
        response.setMessage("Payment declined due to insufficient funds");
        // Add retry schedule
        return response;
    }

    private PaymentResponse createRetryResponse() {
        PaymentResponse response = new PaymentResponse();
        response.setOriginalTransactionId("TXN-2024-001236");
        response.setTransactionId("TXN-2024-001237");
        response.setStatus("PROCESSING");
        response.setRetryAttempt(1);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    private PaymentAttempt createPaymentAttempt() {
        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setTransactionId("TXN-2024-001235");
        attempt.setPolicyId("POLICY-123");
        attempt.setAmount(new BigDecimal("171.00"));
        attempt.setStatus(PaymentStatus.SUCCESS);
        attempt.setPaymentMethod("visa_****1234");
        attempt.setTimestamp(LocalDateTime.now());
        attempt.setRetryCount(0);
        return attempt;
    }
}
