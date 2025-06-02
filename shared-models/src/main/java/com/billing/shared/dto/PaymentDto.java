package com.billing.shared.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class PaymentDto {
    private String id;
    private String billingId;
    private BigDecimal amount;
    private String status;
}