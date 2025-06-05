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
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentProducer paymentProducer;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void shouldProcessPayment() {
        // Mock the mapper
        PaymentEntity mockEntity = new PaymentEntity();
        PaymentDto mockDto = PaymentDto.builder().id("TXN-12345").build();

        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(mockEntity);
        when(paymentMapper.toDto(any(PaymentEntity.class))).thenReturn(mockDto);

        PaymentRequestDto request = PaymentRequestDto.builder()
                .billId("BILL-1")
                .amount(new BigDecimal("100.00"))
                .build();

        PaymentDto result = paymentService.processPayment(request);

        assertNotNull(result);
        verify(paymentRepository).save(any(PaymentEntity.class));
        verify(paymentProducer).sendPaymentEvent(any());
    }

    @Test
    void shouldGetPaymentsByPolicy() {
        // Create mock entities
        PaymentEntity entity1 = new PaymentEntity();
        PaymentEntity entity2 = new PaymentEntity();

        PaymentDto dto1 = PaymentDto.builder().id("TXN-1").build();
        PaymentDto dto2 = PaymentDto.builder().id("TXN-2").build();

        when(paymentRepository.findByPolicyId("POLICY-123"))
                .thenReturn(List.of(entity1, entity2));
        when(paymentMapper.toDto(entity1)).thenReturn(dto1);
        when(paymentMapper.toDto(entity2)).thenReturn(dto2);

        List<PaymentDto> result = paymentService.getPaymentsByPolicy("POLICY-123");

        assertEquals(2, result.size()); // Should now pass
    }

    @Test
    void shouldGetPaymentById() {
        PaymentEntity mockEntity = new PaymentEntity();
        PaymentDto mockDto = PaymentDto.builder().id("TXN-12345").build();

        when(paymentRepository.findById("TXN-12345"))
                .thenReturn(Optional.of(mockEntity));
        when(paymentMapper.toDto(mockEntity)).thenReturn(mockDto);

        Optional<PaymentDto> result = paymentService.getPaymentById("TXN-12345");

        assertTrue(result.isPresent()); // Should now pass
    }

    @Test
    void shouldRetryFailedPayments() {
        PaymentEntity failedPayment = new PaymentEntity();
        failedPayment.setStatus(PaymentStatus.FAILED);

        when(paymentRepository.findByStatus(PaymentStatus.FAILED))
                .thenReturn(List.of(failedPayment));
        when(paymentRepository.save(any())).thenReturn(failedPayment);

        paymentService.retryFailedPayments();

        verify(paymentRepository).findByStatus(PaymentStatus.FAILED);
        verify(paymentRepository).save(any());
    }

    @Test
    void shouldHandleFailedPayment() {
        PaymentEntity mockEntity = new PaymentEntity();
        mockEntity.setStatus(PaymentStatus.FAILED);

        when(paymentRepository.findById("TXN-12345"))
                .thenReturn(Optional.of(mockEntity));
        when(paymentRepository.save(any())).thenReturn(mockEntity);

        // Test the retryFailedPayment method instead
        paymentService.retryFailedPayment("TXN-12345");

        verify(paymentRepository).findById("TXN-12345");
        verify(paymentRepository).save(any());
    }
}