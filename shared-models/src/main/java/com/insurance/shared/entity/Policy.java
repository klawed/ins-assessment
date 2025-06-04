package com.insurance.shared.entity;

import com.insurance.shared.enums.PaymentFrequency;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "policies")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Policy {
    @Id
    private String id;
    private String policyNumber;
    private String customerId;
    private String policyType;
    private BigDecimal premiumAmount;
    private LocalDate nextDueDate;
    private Integer gracePeriodDays;

    public enum PolicyStatus {
        ACTIVE, PENDING, CANCELLED, OVERDUE, DELINQUENT
    }
    
    @Enumerated(EnumType.STRING)
    private PolicyStatus status;

    @Column(name = "payment_frequency")
    @Enumerated(EnumType.STRING)
    private PaymentFrequency paymentFrequency;
}