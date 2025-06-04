package com.insurance.shared.dto;

import com.insurance.shared.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentDto {
    private String id;
    private String policyId;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime timestamp;
    private String paymentMethod;
}