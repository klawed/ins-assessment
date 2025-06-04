package com.insurance.policy.entity;

import com.insurance.shared.enums.PaymentFrequency;
import com.insurance.shared.enums.PolicyStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "policies")
@Data
@SuperBuilder
@NoArgsConstructor
public class PolicyEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String policyNumber;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String policyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private BigDecimal premiumAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentFrequency frequency;

    private Integer gracePeriodDays;

    private LocalDate nextDueDate;

    @Column(updatable = false)
    private LocalDate createdAt;

    private LocalDate updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }
}