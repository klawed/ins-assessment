package com.insurance.payment.service;

import com.insurance.payment.repository.PaymentRepository;
import com.insurance.payment.entity.PaymentEntity;
import com.insurance.payment.mapper.PaymentMapper;
import com.insurance.payment.stream.PaymentProducer;
import com.insurance.shared.dto.PaymentDto;
import com.insurance.shared.dto.PaymentRequestDto;
import com.insurance.shared.enums.PaymentMethod;
import com.insurance.shared.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProducer paymentProducer;  // ADD THIS MOCK!

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentMapper paymentMapper;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldProcessPayment() {
        // If your service doesn't actually use the returned Payment object,
        // just mock it to return anything
        when(paymentRepository.save(any())).thenReturn(null);

        PaymentRequestDto request = PaymentRequestDto.builder()
                .billId("BILL-1")
                .amount(new BigDecimal("100.00"))
                .build();

        // If this test is just verifying the method doesn't throw an exception
        // and that dependencies are called correctly:
        assertDoesNotThrow(() -> paymentService.processPayment(request));

        verify(paymentRepository).save(any());
        verify(paymentProducer).sendPaymentEvent(any());
    }
    @Test
    void shouldGetPaymentsByPolicy() {
        String policyId = "POLICY-123";

        List<PaymentEntity> mockPayments = List.of(
            new PaymentEntity("PAYMENT-1", policyId, new BigDecimal("200.00"), PaymentStatus.SUCCESS, LocalDateTime.now(), PaymentMethod.CREDIT_CARD),
            new PaymentEntity("PAYMENT-2", policyId, new BigDecimal("150.00"), PaymentStatus.SUCCESS, LocalDateTime.now(), PaymentMethod.CREDIT_CARD)
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

        PaymentEntity mockPaymentEntity = new PaymentEntity(
            paymentId,
            "POLICY-123",
            new BigDecimal("200.00"),
            PaymentStatus.SUCCESS,
            LocalDateTime.now(),
            PaymentMethod.CREDIT_CARD
        );

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(mockPaymentEntity));

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
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setId("PAYMENT-1");
        paymentEntity.setPolicyId("POLICY-123");
        paymentEntity.setAmount(new BigDecimal("200.00"));
        paymentEntity.setStatus(PaymentStatus.FAILED);
        paymentEntity.setTimestamp(LocalDateTime.now());
        paymentEntity.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);

        PaymentDto result = paymentService.processPayment(request);

        assertNotNull(result);
        assertEquals(PaymentStatus.FAILED, result.getStatus());
        verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @Test
    void shouldRetryFailedPayments() {
        List<PaymentEntity> failedPayments = List.of(
                new PaymentEntity("PAYMENT-1", "POLICY-123", new BigDecimal("200.00"), PaymentStatus.FAILED, LocalDateTime.now(), PaymentMethod.CREDIT_CARD),
                new PaymentEntity("PAYMENT-2", "POLICY-456", new BigDecimal("150.00"), PaymentStatus.FAILED, LocalDateTime.now(), PaymentMethod.CREDIT_CARD)
        );

        when(paymentRepository.findByStatus(PaymentStatus.FAILED)).thenReturn(failedPayments);

        paymentService.retryFailedPayments();

        verify(paymentRepository).findByStatus(PaymentStatus.FAILED);
        verify(paymentRepository, times(failedPayments.size())).save(any(PaymentEntity.class));
    }
}