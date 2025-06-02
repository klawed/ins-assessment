package com.insurance.notification.unit.service;

import com.insurance.notification.service.NotificationServiceImpl;
import com.insurance.shared.dto.NotificationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(); // Use the actual implementation
    }

    @Test
    void shouldSendNotification() {
        NotificationDto notification = NotificationDto.builder()
                .id("NOTIF-1")
                .recipient("user@example.com")
                .message("Your payment is due.")
                .timestamp(LocalDateTime.now())
                .status("SENT")
                .type("EMAIL")
                .build();

        notificationService.sendNotification(notification);

        Optional<Map<String, Object>> result = notificationService.getNotification("NOTIF-1");

        assertTrue(result.isPresent());
        assertEquals("NOTIF-1", result.get().get("id"));
        assertEquals("user@example.com", result.get().get("recipient"));
    }

    @Test
    void shouldGetNotificationsByRecipient() {
        NotificationDto notification1 = NotificationDto.builder()
                .id("NOTIF-1")
                .recipient("user@example.com")
                .message("Message 1")
                .timestamp(LocalDateTime.now())
                .status("SENT")
                .type("EMAIL")
                .build();

        NotificationDto notification2 = NotificationDto.builder()
                .id("NOTIF-2")
                .recipient("user@example.com")
                .message("Message 2")
                .timestamp(LocalDateTime.now())
                .status("SENT")
                .type("EMAIL")
                .build();

        notificationService.sendNotification(notification1);
        notificationService.sendNotification(notification2);

        List<NotificationDto> result = notificationService.getNotificationsByRecipient("user@example.com");

        assertEquals(2, result.size());
        assertEquals("NOTIF-1", result.get(0).getId());
        assertEquals("NOTIF-2", result.get(1).getId());
    }

    @Test
    void shouldGetAllNotifications() {
        NotificationDto notification1 = NotificationDto.builder()
                .id("NOTIF-1")
                .recipient("user@example.com")
                .message("Message 1")
                .timestamp(LocalDateTime.now())
                .status("SENT")
                .type("EMAIL")
                .build();

        NotificationDto notification2 = NotificationDto.builder()
                .id("NOTIF-2")
                .recipient("user2@example.com")
                .message("Message 2")
                .timestamp(LocalDateTime.now())
                .status("SENT")
                .type("EMAIL")
                .build();

        notificationService.sendNotification(notification1);
        notificationService.sendNotification(notification2);

        List<NotificationDto> result = notificationService.getAllNotifications();

        assertEquals(2, result.size());
        assertEquals("NOTIF-1", result.get(0).getId());
        assertEquals("NOTIF-2", result.get(1).getId());
    }

    @Test
    void shouldDeleteNotification() {
        NotificationDto notification = NotificationDto.builder()
                .id("NOTIF-1")
                .recipient("user@example.com")
                .message("Your payment is due.")
                .timestamp(LocalDateTime.now())
                .status("SENT")
                .type("EMAIL")
                .build();

        notificationService.sendNotification(notification);

        notificationService.deleteNotification("NOTIF-1");

        Optional<Map<String, Object>> result = notificationService.getNotification("NOTIF-1");

        assertFalse(result.isPresent());
    }
}