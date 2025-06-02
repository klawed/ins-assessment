package com.billing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_retries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRetry {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String paymentId;
    
    @Column(nullable = false)
    private String billingId;
    
    @Column(nullable = false)
    private Integer retryAttempt;
    
    @Column(nullable = false)
    private LocalDateTime scheduledAt;
    
    private LocalDateTime attemptedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RetryStatus status;
    
    private String failureReason;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum RetryStatus {
        SCHEDULED, IN_PROGRESS, SUCCESS, FAILED, SKIPPED, EXHAUSTED
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