package com.insurance.notification.service;

import com.insurance.shared.dto.NotificationDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;
/**
 * Service interface for notification management including event-driven notifications,
 * communication channels, and notification preferences.
 */
public interface NotificationService {
    
    /**
     * Send a notification to a customer
     * @param notificationRequest Map containing notification details
     * @return Map containing send result
     */
    Map<String, Object> sendNotification(Map<String, Object> notificationRequest);
    
    /**
     * Send payment reminder notification
     * @param policyId The policy identifier
     * @param customerId The customer identifier
     * @param dueDate The payment due date
     * @return Map containing notification result
     */
    Map<String, Object> sendPaymentReminder(String policyId, String customerId, LocalDateTime dueDate);
    
    /**
     * Send payment confirmation notification
     * @param transactionId The transaction identifier
     * @param customerId The customer identifier
     * @return Map containing notification result
     */
    Map<String, Object> sendPaymentConfirmation(String transactionId, String customerId);
    
    /**
     * Send overdue payment notification
     * @param policyId The policy identifier
     * @param customerId The customer identifier
     * @param daysOverdue Number of days overdue
     * @return Map containing notification result
     */
    Map<String, Object> sendOverdueNotification(String policyId, String customerId, int daysOverdue);
    
    /**
     * Get notification history for a customer
     * @param customerId The customer identifier
     * @return List of notification records
     */
    List<Map<String, Object>> getNotificationHistory(String customerId);
    
    /**
     * Get notification details by ID
     * @param notificationId The notification identifier
     * @return Optional containing notification details if found
     */
    Optional<Map<String, Object>> getNotification(String notificationId);
    
    /**
     * Update notification preferences for a customer
     * @param customerId The customer identifier
     * @param preferences Map containing preference settings
     * @return Map containing update result
     */
    Map<String, Object> updateNotificationPreferences(String customerId, Map<String, Object> preferences);
    
    /**
     * Get notification preferences for a customer
     * @param customerId The customer identifier
     * @return Map containing customer preferences
     */
    Map<String, Object> getNotificationPreferences(String customerId);
    
    /**
     * Schedule a notification to be sent later
     * @param notificationRequest Map containing notification details
     * @param sendAt Scheduled send time
     * @return Map containing scheduling result
     */
    Map<String, Object> scheduleNotification(Map<String, Object> notificationRequest, LocalDateTime sendAt);
    
    /**
     * Process event-driven notifications from other services
     * @param event Map containing event data
     * @return Map containing processing result
     */
    Map<String, Object> processEvent(Map<String, Object> event);
    
    /**
     * Get notification statistics for reporting
     * @return Map containing notification statistics
     */
    Map<String, Object> getNotificationStatistics();

    // Sends a notification
    void sendNotification(NotificationDto notification);

    // Retrieves notifications for a specific recipient
    List<NotificationDto> getNotificationsByRecipient(String recipient);

    // Retrieves all notifications
    List<NotificationDto> getAllNotifications();

    // Deletes a notification by its ID
    void deleteNotification(String notificationId);
}