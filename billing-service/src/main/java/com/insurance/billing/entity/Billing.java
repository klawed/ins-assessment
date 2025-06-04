package com.insurance.billing.entity;

import com.insurance.shared.enums.BillingStatus;
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
    
    private String policyId;
    private String customerId;
    private BigDecimal amount;
    private LocalDate dueDate;
    private Integer retryCount;
    
    @Enumerated(EnumType.STRING)
    private BillingStatus status;
    
    private LocalDateTime billingDate;
    private String paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate gracePeriodEnd;

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