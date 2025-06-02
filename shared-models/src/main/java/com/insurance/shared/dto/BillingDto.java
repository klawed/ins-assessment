package com.insurance.shared.dto;

import com.insurance.shared.enums.BillingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingDto {
    private String id;
    private String policyId;
    private String customerId;
    private BigDecimal amount;
    private LocalDate dueDate;
    private BillingStatus status;
    private String paymentStatus;
    private LocalDateTime billingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; // Add this field
}