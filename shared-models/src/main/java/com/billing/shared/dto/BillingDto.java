package com.billing.shared.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class BillingDto {
    private String id;
    private String policyId;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String status;
}