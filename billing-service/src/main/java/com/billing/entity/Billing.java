package com.billing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "billings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Billing {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String policyId;
    
    @Column(nullable = false)
    private String customerId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal premiumAmount;
    
    @Column(nullable = false)
    private LocalDate dueDate;
    
    @Column(nullable = false)
    private LocalDate billingPeriodStart;
    
    @Column(nullable = false)
    private LocalDate billingPeriodEnd;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) 
    private BillingFrequency frequency;
    
    private LocalDate gracePeriodEnd;
    private Integer retryCount;
    private LocalDate nextRetryDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum BillingStatus {
        PENDING, PAID, OVERDUE, GRACE_PERIOD, DELINQUENT, CANCELLED
    }
    
    public enum BillingFrequency {
        MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}