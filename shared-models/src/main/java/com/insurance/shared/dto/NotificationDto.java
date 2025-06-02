package com.insurance.shared.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private String id;

    @NotBlank(message = "Recipient is required")
    @Email(message = "Recipient must be a valid email address")
    private String recipient;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;

    @NotBlank(message = "Status is required")
    private String status;

    @NotBlank(message = "Type is required")
    private String type;
}