package com.billing.policy.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {
    @Id
    private String id;
    
    @Column(nullable = false, unique = true)
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
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal premiumAmount;
    
    @Column(nullable = false)
    private String frequency;
    
    private Integer gracePeriodDays;
    
    private LocalDate nextDueDate;
    
    public enum PolicyStatus {
        ACTIVE, INACTIVE, CANCELLED, LAPSED, PENDING, OVERDUE, GRACE_PERIOD
    }
}