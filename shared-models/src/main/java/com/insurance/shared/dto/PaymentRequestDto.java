package com.insurance.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    private String billId; // Unique identifier for the bill
    private String policyId; // Unique identifier for the policy
    private BigDecimal amount; // Payment amount
    private String paymentMethod; // Payment method (e.g., CREDIT_CARD, BANK_TRANSFER)
}