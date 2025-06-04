package com.insurance.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationServiceDto {
    private String id; // Unique identifier for the notification
    private String recipient; // Email, phone number, etc.
    private String message; // Notification message
    private String type; // Type of notification (e.g., EMAIL, SMS)
    private String status; // Status (e.g., SENT, FAILED)
    private LocalDateTime timestamp; // When the notification was created
    private String eventType; // Event type (e.g., PAYMENT_FAILED, PAYMENT_SUCCEEDED)
}