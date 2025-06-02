package com.billing.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of NotificationService providing event-driven notifications,
 * communication management, and notification preferences.
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    // Simulated in-memory storage for demonstration
    private final Map<String, Map<String, Object>> notifications = new HashMap<>();
    private final Map<String, Map<String, Object>> customerPreferences = new HashMap<>();
    private final Random random = new Random();
    
    @Override
    public Map<String, Object> sendNotification(Map<String, Object> notificationRequest) {
        String notificationId = generateNotificationId();
        log.info("Sending notification with ID: {}", notificationId);
        
        String customerId = (String) notificationRequest.get("customerId");
        String type = (String) notificationRequest.get("type");
        String channel = (String) notificationRequest.get("channel");
        String message = (String) notificationRequest.get("message");
        
        // Check customer preferences
        Map<String, Object> preferences = getNotificationPreferences(customerId);
        boolean allowedChannel = isChannelAllowed(channel, preferences);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("notificationId", notificationId);
        notification.put("customerId", customerId);
        notification.put("type", type);
        notification.put("channel", channel);
        notification.put("message", message);
        notification.put("sentAt", LocalDateTime.now());
        notification.put("status", allowedChannel ? "SENT" : "BLOCKED");
        
        if (!allowedChannel) {
            notification.put("reason", "Customer has disabled " + channel + " notifications");
        }
        
        // Store notification
        notifications.put(notificationId, notification);
        
        return notification;
    }
    
    @Override
    public Map<String, Object> sendPaymentReminder(String policyId, String customerId, LocalDateTime dueDate) {
        log.info("Sending payment reminder for policy {} to customer {}", policyId, customerId);
        
        Map<String, Object> request = Map.of(
            "customerId", customerId,
            "type", "PAYMENT_REMINDER",
            "channel", "EMAIL",
            "message", "Your payment for policy " + policyId + " is due on " + dueDate.toLocalDate(),
            "policyId", policyId,
            "dueDate", dueDate
        );
        
        return sendNotification(request);
    }
    
    @Override
    public Map<String, Object> sendPaymentConfirmation(String transactionId, String customerId) {
        log.info("Sending payment confirmation for transaction {} to customer {}", transactionId, customerId);
        
        Map<String, Object> request = Map.of(
            "customerId", customerId,
            "type", "PAYMENT_CONFIRMATION",
            "channel", "EMAIL",
            "message", "Payment confirmed. Transaction ID: " + transactionId,
            "transactionId", transactionId
        );
        
        return sendNotification(request);
    }
    
    @Override
    public Map<String, Object> sendOverdueNotification(String policyId, String customerId, int daysOverdue) {
        log.info("Sending overdue notification for policy {} to customer {} ({} days overdue)", 
                policyId, customerId, daysOverdue);
        
        String urgency = daysOverdue > 30 ? "URGENT" : "STANDARD";
        String channel = daysOverdue > 30 ? "SMS" : "EMAIL";
        
        Map<String, Object> request = Map.of(
            "customerId", customerId,
            "type", "PAYMENT_OVERDUE",
            "channel", channel,
            "message", "Your payment for policy " + policyId + " is " + daysOverdue + " days overdue",
            "policyId", policyId,
            "daysOverdue", daysOverdue,
            "urgency", urgency
        );
        
        return sendNotification(request);
    }
    
    @Override
    public List<Map<String, Object>> getNotificationHistory(String customerId) {
        log.info("Getting notification history for customer: {}", customerId);
        
        List<Map<String, Object>> history = new ArrayList<>();
        
        for (Map<String, Object> notification : notifications.values()) {
            if (customerId.equals(notification.get("customerId"))) {
                history.add(new HashMap<>(notification));
            }
        }
        
        // Sort by sent date, most recent first
        history.sort((n1, n2) -> {
            LocalDateTime date1 = (LocalDateTime) n1.get("sentAt");
            LocalDateTime date2 = (LocalDateTime) n2.get("sentAt");
            return date2.compareTo(date1);
        });
        
        return history;
    }
    
    @Override
    public Optional<Map<String, Object>> getNotification(String notificationId) {
        log.info("Getting notification: {}", notificationId);
        
        Map<String, Object> notification = notifications.get(notificationId);
        return Optional.ofNullable(notification != null ? new HashMap<>(notification) : null);
    }
    
    @Override
    public Map<String, Object> updateNotificationPreferences(String customerId, Map<String, Object> preferences) {
        log.info("Updating notification preferences for customer: {}", customerId);
        
        customerPreferences.put(customerId, new HashMap<>(preferences));
        
        Map<String, Object> result = new HashMap<>();
        result.put("customerId", customerId);
        result.put("preferences", preferences);
        result.put("updatedAt", LocalDateTime.now());
        result.put("status", "UPDATED");
        
        return result;
    }
    
    @Override
    public Map<String, Object> getNotificationPreferences(String customerId) {
        log.info("Getting notification preferences for customer: {}", customerId);
        
        Map<String, Object> preferences = customerPreferences.get(customerId);
        
        if (preferences == null) {
            // Default preferences
            preferences = Map.of(
                "email", true,
                "sms", true,
                "push", false,
                "paymentReminders", true,
                "overdueNotifications", true,
                "confirmations", true
            );
        }
        
        return new HashMap<>(preferences);
    }
    
    @Override
    public Map<String, Object> scheduleNotification(Map<String, Object> notificationRequest, LocalDateTime sendAt) {
        String scheduleId = generateNotificationId();
        log.info("Scheduling notification {} to be sent at {}", scheduleId, sendAt);
        
        Map<String, Object> scheduledNotification = new HashMap<>(notificationRequest);
        scheduledNotification.put("scheduleId", scheduleId);
        scheduledNotification.put("scheduledFor", sendAt);
        scheduledNotification.put("status", "SCHEDULED");
        scheduledNotification.put("createdAt", LocalDateTime.now());
        
        // In a real implementation, this would be stored and processed by a scheduler
        return scheduledNotification;
    }
    
    @Override
    public Map<String, Object> processEvent(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        log.info("Processing event: {}", eventType);
        
        Map<String, Object> result = new HashMap<>();
        result.put("eventType", eventType);
        result.put("processedAt", LocalDateTime.now());
        result.put("status", "PROCESSED");
        
        // Process different event types
        switch (eventType) {
            case "PAYMENT_FAILED":
                String policyId = (String) event.get("policyId");
                String customerId = (String) event.get("customerId");
                sendOverdueNotification(policyId, customerId, 1);
                result.put("action", "Sent overdue notification");
                break;
                
            case "PAYMENT_SUCCEEDED":
                String transactionId = (String) event.get("transactionId");
                String customerIdSuccess = (String) event.get("customerId");
                sendPaymentConfirmation(transactionId, customerIdSuccess);
                result.put("action", "Sent payment confirmation");
                break;
                
            case "PAYMENT_DUE":
                String policyIdDue = (String) event.get("policyId");
                String customerIdDue = (String) event.get("customerId");
                LocalDateTime dueDate = (LocalDateTime) event.get("dueDate");
                sendPaymentReminder(policyIdDue, customerIdDue, dueDate);
                result.put("action", "Sent payment reminder");
                break;
                
            default:
                result.put("action", "Event type not recognized");
                log.warn("Unknown event type: {}", eventType);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getNotificationStatistics() {
        log.info("Getting notification statistics");
        
        int totalNotifications = notifications.size();
        long sentCount = notifications.values().stream()
            .mapToLong(n -> "SENT".equals(n.get("status")) ? 1 : 0)
            .sum();
        long blockedCount = notifications.values().stream()
            .mapToLong(n -> "BLOCKED".equals(n.get("status")) ? 1 : 0)
            .sum();
        
        // Count by channel
        Map<String, Long> channelStats = new HashMap<>();
        notifications.values().forEach(n -> {
            String channel = (String) n.get("channel");
            channelStats.merge(channel, 1L, Long::sum);
        });
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNotifications", totalNotifications);
        stats.put("sentNotifications", sentCount);
        stats.put("blockedNotifications", blockedCount);
        stats.put("deliveryRate", totalNotifications > 0 ? (double) sentCount / totalNotifications : 0.0);
        stats.put("channelBreakdown", channelStats);
        stats.put("generatedAt", LocalDateTime.now());
        
        return stats;
    }
    
    private boolean isChannelAllowed(String channel, Map<String, Object> preferences) {
        return preferences.getOrDefault(channel.toLowerCase(), true).equals(true);
    }
    
    private String generateNotificationId() {
        return "NOT-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
    }
}