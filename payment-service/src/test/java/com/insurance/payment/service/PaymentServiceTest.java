package com.insurance.payment.service;

import com.insurance.payment.repository.PaymentRepository;
import com.insurance.payment.entity.PaymentEntity;
import com.insurance.shared.dto.PaymentDto;
import com.insurance.shared.dto.PaymentRequestDto;
import com.insurance.shared.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldProcessPayment() {
        PaymentRequestDto request = PaymentRequestDto.builder()
                .policyId("POLICY-123")
                .amount(new BigDecimal("200.00"))
                .paymentMethod("CREDIT_CARD")
                .build();

        PaymentDto savedPayment = PaymentDto.builder()
                .id("PAYMENT-1")
                .policyId("POLICY-123")
                .amount(new BigDecimal("200.00"))
                .status(PaymentStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any(PaymentDto.class))).thenReturn(savedPayment);

        PaymentDto result = paymentService.processPayment(request);

        assertNotNull(result);
        assertEquals("PAYMENT-1", result.getId());
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        verify(paymentRepository).save(any(PaymentDto.class));
    }

    @Test
    void shouldGetPaymentsByPolicy() {
        String policyId = "POLICY-123";

        List<PaymentEntity> mockPayments = List.of(
            new PaymentEntity("PAYMENT-1", policyId, new BigDecimal("200.00"), PaymentStatus.SUCCESS, LocalDateTime.now(), "CREDIT_CARD"),
            new PaymentEntity("PAYMENT-2", policyId, new BigDecimal("150.00"), PaymentStatus.SUCCESS, LocalDateTime.now(), "CREDIT_CARD")
        );

        when(paymentRepository.findByPolicyId(policyId)).thenReturn(mockPayments);

        List<PaymentDto> result = paymentService.getPaymentsByPolicy(policyId);

        assertEquals(2, result.size());
        assertEquals("PAYMENT-1", result.get(0).getId());
        assertEquals(new BigDecimal("200.00"), result.get(0).getAmount());
        verify(paymentRepository).findByPolicyId(policyId);
    }

    @Test
    void shouldGetPaymentById() {
        String paymentId = "PAYMENT-1";

        PaymentDto mockPayment = PaymentDto.builder()
                .id(paymentId)
                .policyId("POLICY-123")
                .amount(new BigDecimal("200.00"))
                .status(PaymentStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(mockPayment));

        Optional<PaymentDto> result = paymentService.getPaymentById(paymentId);

        assertTrue(result.isPresent());
        assertEquals(paymentId, result.get().getId());
        verify(paymentRepository).findById(paymentId);
    }

    @Test
    void shouldHandleFailedPayment() {
        PaymentRequestDto request = PaymentRequestDto.builder()
                .policyId("POLICY-123")
                .amount(new BigDecimal("200.00"))
                .paymentMethod("CREDIT_CARD")
                .build();

        PaymentDto failedPayment = PaymentDto.builder()
                .id("PAYMENT-1")
                .policyId("POLICY-123")
                .amount(new BigDecimal("200.00"))
                .status(PaymentStatus.FAILED)
                .timestamp(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any(PaymentDto.class))).thenReturn(failedPayment);

        PaymentDto result = paymentService.processPayment(request);

        assertNotNull(result);
        assertEquals(PaymentStatus.FAILED, result.getStatus());
        verify(paymentRepository).save(any(PaymentDto.class));
    }

    @Test
    void shouldRetryFailedPayments() {
        List<PaymentDto> failedPayments = List.of(
                PaymentDto.builder().id("PAYMENT-1").policyId("POLICY-123").status(PaymentStatus.FAILED).build(),
                PaymentDto.builder().id("PAYMENT-2").policyId("POLICY-456").status(PaymentStatus.FAILED).build()
        );

        when(paymentRepository.findByStatus(PaymentStatus.FAILED)).thenReturn(failedPayments);

        paymentService.retryFailedPayments();

        verify(paymentRepository).findByStatus(PaymentStatus.FAILED);
        verify(paymentRepository, times(failedPayments.size())).save(any(PaymentDto.class));
    }
}