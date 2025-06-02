package com.billing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.billing.shared.enums.PaymentFrequency;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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
}