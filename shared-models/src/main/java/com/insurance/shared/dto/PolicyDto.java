package com.insurance.shared.dto;

import com.insurance.shared.enums.PaymentFrequency;
import com.insurance.shared.enums.PolicyStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDto {
    @Builder.Default
    private String id = null;  // This is the main ID field

    @NotBlank(message = "Policy number is required")
    private String policyNumber;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Policy type is required")
    private String policyType;

    @NotNull(message = "Policy status is required")
    private PolicyStatus status;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;

    @NotNull(message = "Premium amount is required")
    @Positive(message = "Premium amount must be positive")
    private BigDecimal premiumAmount;

    @NotNull(message = "Payment frequency is required")
    private PaymentFrequency frequency;

    @NotNull(message = "Next due date is required")
    private LocalDate nextDueDate;

    @NotNull(message = "Grace period days is required")
    @PositiveOrZero(message = "Grace period days must be zero or positive")
    private Integer gracePeriodDays;

    // Add this field for the premium schedule
    private PremiumScheduleDto premiumSchedule;
}
