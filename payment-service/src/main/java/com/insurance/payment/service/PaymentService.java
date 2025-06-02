package com.insurance.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.insurance.shared.dto.PaymentDto;
import com.insurance.shared.dto.PaymentRequestDto;

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
    PaymentDto processPayment(PaymentRequestDto paymentRequest);
    
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

    /**
     * Get a list of delinquent policies based on overdue payments
     * @param limit The maximum number of results to return
     * @param offset The starting point for pagination
     * @param minDaysOverdue The minimum number of days a payment is overdue
     * @param customerId (Optional) Filter by customer ID
     * @return Map containing total count and a list of delinquent policies
     */
    Map<String, Object> getDelinquentPolicies(int limit, int offset, int minDaysOverdue, String customerId);

    /**
     * Get payments by policy identifier
     * @param policyId The policy identifier
     * @return List of payment DTOs
     */
    List<PaymentDto> getPaymentsByPolicy(String policyId);

    /**
     * Retry all failed payments
     */
    void retryFailedPayments();

    /**
     * Get a payment by its identifier
     * @param paymentId The payment identifier
     * @return Optional containing the payment DTO if found
     */
    Optional<PaymentDto> getPaymentById(String paymentId);

    /**
     * Retry a failed payment transaction
     * @param transactionId The transaction identifier
     */
    void retryFailedPayment(String transactionId);

    /**
     * Get payment history for a specific policy with pagination and status filter
     * @param policyId The policy identifier
     * @param status The payment status to filter by
     * @param limit The maximum number of results to return
     * @param offset The starting point for pagination
     * @return List of payment DTOs
     */
    List<PaymentDto> getPaymentHistory(String policyId, String status, int limit, int offset);
}