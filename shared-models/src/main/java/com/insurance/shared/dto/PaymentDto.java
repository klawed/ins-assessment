package com.insurance.shared.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PaymentDto {
    private String id;
    private String billingId;
    private BigDecimal amount;
    private String status;
}