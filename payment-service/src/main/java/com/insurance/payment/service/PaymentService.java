package com.insurance.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for payment processing including payment transactions,
 * retry logic, and transaction state management.
 */
public interface PaymentService {

    /**
     * Process a payment transaction
     * @param paymentRequest Map containing payment details
     * @return Map containing payment result
     */
    Map<String, Object> processPayment(Map<String, Object> paymentRequest);
    
    /**
     * Retry a failed payment transaction
     * @param transactionId The transaction identifier
     * @return Map containing retry result
     */
    Map<String, Object> retryPayment(String transactionId);
    
    /**
     * Get payment history for a specific policy
     * @param policyId The policy identifier
     * @return List of payment transactions
     */
    List<Map<String, Object>> getPaymentHistory(String policyId);
    
    /**
     * Get details of a specific payment transaction
     * @param transactionId The transaction identifier
     * @return Optional containing transaction details if found
     */
    Optional<Map<String, Object>> getPaymentTransaction(String transactionId);
    
    /**
     * Get all failed payments that need retry
     * @return List of failed payment transactions
     */
    List<Map<String, Object>> getFailedPayments();
    
    /**
     * Schedule a payment retry with exponential backoff
     * @param transactionId The transaction identifier
     * @param retryAttempt The current retry attempt number
     * @return Scheduled retry time
     */
    LocalDateTime scheduleRetry(String transactionId, int retryAttempt);
    
    /**
     * Calculate the next retry time using exponential backoff
     * @param retryAttempt The current retry attempt number
     * @return Next retry time
     */
    LocalDateTime calculateNextRetryTime(int retryAttempt);
    
    /**
     * Update payment status
     * @param transactionId The transaction identifier
     * @param status The new payment status
     */
    void updatePaymentStatus(String transactionId, String status);
    
    /**
     * Initiate refund for a completed payment
     * @param transactionId The transaction identifier
     * @param amount The refund amount (null for full refund)
     * @return Map containing refund result
     */
    Map<String, Object> initiateRefund(String transactionId, BigDecimal amount);
    
    /**
     * Get payment statistics for reporting
     * @return Map containing payment statistics
     */
    Map<String, Object> getPaymentStatistics();

    /**
     * Get the status of a specific payment transaction
     * @param transactionId The transaction identifier
     * @return Map containing transaction status details
     */
    Map<String, Object> getPaymentStatus(String transactionId);

    /**
     * Delete a specific payment transaction
     * @param transactionId The transaction identifier
     */
    void deletePayment(String transactionId);

    /**
     * Validate a payment request
     * @param paymentRequest Map containing payment details
     * @return true if the request is valid, false otherwise
     */
    boolean validatePaymentRequest(Map<String, Object> paymentRequest);
}