package com.insurance.shared.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Data
@Builder
public class PremiumScheduleDto {
    @NotNull
    private String policyId;
    
    @Positive
    private BigDecimal premiumAmount;
    
    @NotNull
    private String billingFrequency;
    
    @NotNull
    private LocalDate nextDueDate;
    
    private int gracePeriodDays;
    private String status;
    private Integer daysOverdue;
    private BigDecimal lateFee;
    private BigDecimal totalAmountDue;
    private List<PaymentDto> schedule;

    @Data
    @Builder
    public static class PaymentDto {
        private LocalDate dueDate;
        private BigDecimal amount;
        private String status;
        private LocalDate paidDate;
    }
}