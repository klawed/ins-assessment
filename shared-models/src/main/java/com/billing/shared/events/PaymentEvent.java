package com.billing.shared.events;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {

    private String eventId;
    private String policyId;
    private String paymentId;
    private PaymentEventType eventType;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime timestamp;
    private String reason;
    private Integer attemptNumber;

    public enum PaymentEventType {
        PAYMENT_ATTEMPTED,
        PAYMENT_SUCCEEDED,
        PAYMENT_FAILED,
        RETRY_SCHEDULED,
        RETRIES_EXHAUSTED
    }
}