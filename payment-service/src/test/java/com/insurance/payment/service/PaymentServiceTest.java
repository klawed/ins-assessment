package com.insurance.payment.service;

import com.insurance.payment.repository.PaymentRepository;
import com.insurance.payment.entity.PaymentEntity;
import com.insurance.payment.mapper.PaymentMapper;
import com.insurance.shared.dto.PaymentDto;
import com.insurance.shared.dto.PaymentRequestDto;
import com.insurance.shared.enums.PaymentMethod;
import com.insurance.shared.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

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
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setId("PAYMENT-1");
        paymentEntity.setPolicyId("POLICY-123");
        paymentEntity.setAmount(new BigDecimal("200.00"));
        paymentEntity.setStatus(PaymentStatus.SUCCESS);
        paymentEntity.setTimestamp(LocalDateTime.now());
        paymentEntity.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        when(paymentMapper.toDto(any(PaymentEntity.class))).thenReturn(PaymentDto.builder()
                .id("PAYMENT-1")
                .policyId("POLICY-123")
                .amount(new BigDecimal("200.00"))
                .status(PaymentStatus.SUCCESS)
                .paymentMethod("CREDIT_CARD")
                .build());

        PaymentDto result = paymentService.processPayment(request);

        assertNotNull(result);
        assertEquals("PAYMENT-1", result.getId());
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        verify(paymentRepository).save(any(PaymentEntity.class));
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