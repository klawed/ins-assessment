package com.insurance.billing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "billing_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingEvent {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String billingId;
    
    @Column(nullable = false)
    private String policyId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;
    
    @Column(nullable = false)
    private LocalDateTime occurredAt;
    
    @Column(columnDefinition = "TEXT")
    private String payload;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    public enum EventType {
        BILLING_CREATED, PAYMENT_DUE, PAYMENT_SUCCESS, PAYMENT_FAILED,
        RETRY_SCHEDULED, GRACE_PERIOD_STARTED, DELINQUENT, REMINDER_SENT
    }
    
    @PrePersist
    protected void onCreate() {
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
    }
}