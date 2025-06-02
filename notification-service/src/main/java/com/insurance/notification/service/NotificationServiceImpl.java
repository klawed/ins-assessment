package com.insurance.notification.service;

import com.insurance.shared.dto.NotificationDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of NotificationService providing event-driven notifications,
 * communication management, and notification preferences.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private final Map<String, Map<String, Object>> notifications = new HashMap<>();
    private final Map<String, Map<String, Object>> notificationPreferences = new HashMap<>();
    private final Map<String, Integer> retryCounters = new HashMap<>();
    private static final int MAX_RETRIES = 3;

    @Override
    public Map<String, Object> sendNotification(Map<String, Object> notificationRequest) {
        String notificationId = UUID.randomUUID().toString();
        notifications.put(notificationId, notificationRequest);
        return Map.of("status", "success", "message", "Notification sent", "notificationId", notificationId);
    }

    @Override
    public Map<String, Object> sendPaymentReminder(String policyId, String customerId, LocalDateTime dueDate) {
        Map<String, Object> reminder = Map.of(
                "policyId", policyId,
                "customerId", customerId,
                "dueDate", dueDate,
                "type", "PAYMENT_REMINDER"
        );
        return sendNotification(reminder);
    }

    @Override
    public Map<String, Object> sendPaymentConfirmation(String transactionId, String customerId) {
        Map<String, Object> confirmation = Map.of(
                "transactionId", transactionId,
                "customerId", customerId,
                "type", "PAYMENT_CONFIRMATION"
        );
        return sendNotification(confirmation);
    }

    @Override
    public Map<String, Object> sendOverdueNotification(String policyId, String customerId, int daysOverdue) {
        Map<String, Object> overdueNotification = Map.of(
                "policyId", policyId,
                "customerId", customerId,
                "daysOverdue", daysOverdue,
                "type", "OVERDUE_NOTIFICATION"
        );
        return sendNotification(overdueNotification);
    }

    @Override
    public List<Map<String, Object>> getNotificationHistory(String customerId) {
        List<Map<String, Object>> history = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : notifications.entrySet()) {
            Map<String, Object> notification = entry.getValue();
            if (customerId.equals(notification.get("customerId"))) {
                history.add(notification);
            }
        }
        return history;
    }

    @Override
    public Optional<Map<String, Object>> getNotification(String notificationId) {
        return Optional.ofNullable(notifications.get(notificationId));
    }

    @Override
    public Map<String, Object> updateNotificationPreferences(String customerId, Map<String, Object> preferences) {
        notificationPreferences.put(customerId, preferences);
        return Map.of("status", "success", "message", "Preferences updated");
    }

    @Override
    public Map<String, Object> getNotificationPreferences(String customerId) {
        return notificationPreferences.getOrDefault(customerId, Map.of());
    }

    @Override
    public Map<String, Object> scheduleNotification(Map<String, Object> notificationRequest, LocalDateTime sendAt) {
        Map<String, Object> scheduledNotification = new HashMap<>(notificationRequest);
        scheduledNotification.put("sendAt", sendAt);
        return sendNotification(scheduledNotification);
    }

    @Override
    public Map<String, Object> processEvent(Map<String, Object> event) {
        String eventType = (String) event.get("type");
        if ("PAYMENT_FAILED".equals(eventType)) {
            return retryNotification(event);
        } else if ("PAYMENT_SUCCEEDED".equals(eventType)) {
            return sendNotification(Map.of("type", "PAYMENT_SUCCEEDED", "details", event));
        }
        return Map.of("status", "ignored", "message", "Event type not handled");
    }

    @Override
    public Map<String, Object> getNotificationStatistics() {
        return Map.of(
                "totalNotifications", notifications.size(),
                "totalPreferences", notificationPreferences.size(),
                "totalRetries", retryCounters.size()
        );
    }

    @Override
    public void sendNotification(NotificationDto notification) {
        Map<String, Object> notificationMap = Map.of(
                "id", notification.getId(),
                "recipient", notification.getRecipient(),
                "message", notification.getMessage(),
                "timestamp", notification.getTimestamp(),
                "status", notification.getStatus(),
                "type", notification.getType()
        );
        notifications.put(notification.getId(), notificationMap);
    }

    @Override
    public List<NotificationDto> getNotificationsByRecipient(String recipient) {
        List<NotificationDto> result = new ArrayList<>();
        for (Map<String, Object> notification : notifications.values()) {
            if (recipient.equals(notification.get("recipient"))) {
                result.add(NotificationDto.builder()
                        .id((String) notification.get("id"))
                        .recipient((String) notification.get("recipient"))
                        .message((String) notification.get("message"))
                        .timestamp((LocalDateTime) notification.get("timestamp"))
                        .status((String) notification.get("status"))
                        .type((String) notification.get("type"))
                        .build());
            }
        }
        return result;
    }

    @Override
    public List<NotificationDto> getAllNotifications() {
        List<NotificationDto> result = new ArrayList<>();
        for (Map<String, Object> notification : notifications.values()) {
            result.add(NotificationDto.builder()
                    .id((String) notification.get("id"))
                    .recipient((String) notification.get("recipient"))
                    .message((String) notification.get("message"))
                    .timestamp((LocalDateTime) notification.get("timestamp"))
                    .status((String) notification.get("status"))
                    .type((String) notification.get("type"))
                    .build());
        }
        return result;
    }

    @Override
    public void deleteNotification(String notificationId) {
        notifications.remove(notificationId);
    }

    private Map<String, Object> retryNotification(Map<String, Object> event) {
        String notificationId = (String) event.get("notificationId");
        int retryCount = retryCounters.getOrDefault(notificationId, 0);
        if (retryCount >= MAX_RETRIES) {
            return Map.of("status", "failed", "message", "Max retries reached");
        }
        retryCounters.put(notificationId, retryCount + 1);
        return sendNotification(event);
    }
}