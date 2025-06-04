package com.insurance.billing.entity;

import com.insurance.shared.enums.CustomerTier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "grace_period_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GracePeriodConfig {
    @Id
    private String id;
    
    @Column(name = "policy_type")
    private String policyType;
    
    @Column(name = "payment_frequency")
    @Enumerated(EnumType.STRING)
    private PaymentFrequency frequency;
    
    @Column(name = "grace_period_days")
    private Integer gracePeriodDays;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private CustomerTier customerTier;
    
    public enum PaymentFrequency {
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