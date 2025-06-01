package com.billing.shared.dto;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PolicyDto {

    @NotBlank(message = "Policy ID cannot be blank")
    private String policyId;

    @NotBlank(message = "Policy number cannot be blank")
    private String policyNumber;

    @NotBlank(message = "Customer ID cannot be blank")
    private String customerId;

    @NotBlank(message = "Policy type cannot be blank")
    private String policyType;

    @NotNull(message = "Policy status cannot be null")
    private PolicyStatus status;

    @NotNull(message = "Effective date cannot be null")
    private LocalDate effectiveDate;

    @NotNull(message = "Expiration date cannot be null")
    private LocalDate expirationDate;

    @Positive(message = "Premium amount must be positive")
    private BigDecimal premiumAmount;

    private String frequency; // MONTHLY, QUARTERLY, ANNUALLY

    private Integer gracePeriodDays;

    public enum PolicyStatus {
        ACTIVE,
        INACTIVE,
        CANCELLED,
        LAPSED,
        PENDING
    }
}
