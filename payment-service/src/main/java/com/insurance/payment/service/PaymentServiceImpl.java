package com.insurance.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of PaymentService providing payment processing,
 * retry logic, and transaction state management.
 */
@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    
    // Simulated in-memory storage for demonstration
    private final Map<String, Map<String, Object>> transactions = new HashMap<>();
    private final Random random = new Random();
    
    @Override
    public Map<String, Object> processPayment(Map<String, Object> paymentRequest) {
        String transactionId = generateTransactionId();
        log.info("Processing payment with transaction ID: {}", transactionId);
        
        // Simulate payment processing with random success/failure
        boolean isSuccessful = random.nextDouble() > 0.3; // 70% success rate
        
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("transactionId", transactionId);
        transaction.put("policyId", paymentRequest.get("policyId"));
        transaction.put("amount", paymentRequest.get("amount"));
        transaction.put("paymentMethod", paymentRequest.get("paymentMethod"));
        transaction.put("status", isSuccessful ? "COMPLETED" : "FAILED");
        transaction.put("attemptedAt", LocalDateTime.now());
        transaction.put("retryAttempt", 0);
        
        if (isSuccessful) {
            transaction.put("completedAt", LocalDateTime.now());
            transaction.put("message", "Payment processed successfully");
        } else {
            transaction.put("failureReason", "Insufficient funds");
            transaction.put("message", "Payment failed - will retry automatically");
            transaction.put("nextRetryAt", calculateNextRetryTime(1));
        }
        
        // Store transaction
        transactions.put(transactionId, transaction);
        
        return transaction;
    }
    
    @Override
    public Map<String, Object> retryPayment(String transactionId) {
        log.info("Retrying payment for transaction ID: {}", transactionId);
        
        Map<String, Object> transaction = transactions.get(transactionId);
        if (transaction == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Transaction not found");
            error.put("transactionId", transactionId);
            return error;
        }
        
        int currentAttempt = (Integer) transaction.get("retryAttempt");
        int nextAttempt = currentAttempt + 1;
        
        // Simulate retry with increasing success rate
        boolean isSuccessful = random.nextDouble() > (0.5 - (nextAttempt * 0.1));
        
        transaction.put("retryAttempt", nextAttempt);
        transaction.put("attemptedAt", LocalDateTime.now());
        
        if (isSuccessful) {
            transaction.put("status", "COMPLETED");
            transaction.put("completedAt", LocalDateTime.now());
            transaction.put("message", "Payment completed on retry " + nextAttempt);
        } else {
            transaction.put("status", "FAILED");
            transaction.put("failureReason", "Payment gateway timeout");
            transaction.put("message", "Payment failed on retry " + nextAttempt);
            if (nextAttempt < 5) { // Max 5 retries
                transaction.put("nextRetryAt", calculateNextRetryTime(nextAttempt + 1));
            } else {
                transaction.put("message", "Payment failed - maximum retries exceeded");
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("transactionId", transactionId);
        result.put("retryAttempt", nextAttempt);
        result.put("status", transaction.get("status"));
        result.put("message", transaction.get("message"));
        if (transaction.containsKey("nextRetryAt")) {
            result.put("nextRetryAt", transaction.get("nextRetryAt"));
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getPaymentHistory(String policyId) {
        log.info("Getting payment history for policy ID: {}", policyId);
        
        List<Map<String, Object>> history = new ArrayList<>();
        
        for (Map<String, Object> transaction : transactions.values()) {
            if (policyId.equals(transaction.get("policyId"))) {
                history.add(new HashMap<>(transaction));
            }
        }
        
        // Sort by attempted date, most recent first
        history.sort((t1, t2) -> {
            LocalDateTime date1 = (LocalDateTime) t1.get("attemptedAt");
            LocalDateTime date2 = (LocalDateTime) t2.get("attemptedAt");
            return date2.compareTo(date1);
        });
        
        return history;
    }
    
    @Override
    public Optional<Map<String, Object>> getPaymentTransaction(String transactionId) {
        log.info("Getting payment transaction: {}", transactionId);
        
        Map<String, Object> transaction = transactions.get(transactionId);
        return Optional.ofNullable(transaction != null ? new HashMap<>(transaction) : null);
    }
    
    @Override
    public List<Map<String, Object>> getFailedPayments() {
        log.info("Getting failed payments");
        
        List<Map<String, Object>> failedPayments = new ArrayList<>();
        
        for (Map<String, Object> transaction : transactions.values()) {
            if ("FAILED".equals(transaction.get("status"))) {
                failedPayments.add(new HashMap<>(transaction));
            }
        }
        
        return failedPayments;
    }
    
    @Override
    public LocalDateTime scheduleRetry(String transactionId, int retryAttempt) {
        log.info("Scheduling retry for transaction {} attempt {}", transactionId, retryAttempt);
        
        LocalDateTime nextRetryTime = calculateNextRetryTime(retryAttempt);
        
        Map<String, Object> transaction = transactions.get(transactionId);
        if (transaction != null) {
            transaction.put("nextRetryAt", nextRetryTime);
            transaction.put("retryScheduled", true);
        }
        
        return nextRetryTime;
    }
    
    @Override
    public LocalDateTime calculateNextRetryTime(int retryAttempt) {
        // Exponential backoff: 2^attempt minutes
        long delayMinutes = (long) Math.pow(2, retryAttempt);
        return LocalDateTime.now().plusMinutes(delayMinutes);
    }
    
    @Override
    public void updatePaymentStatus(String transactionId, String status) {
        log.info("Updating payment status for transaction {} to {}", transactionId, status);
        
        Map<String, Object> transaction = transactions.get(transactionId);
        if (transaction != null) {
            transaction.put("status", status);
            transaction.put("updatedAt", LocalDateTime.now());
        }
    }
    
    @Override
    public Map<String, Object> initiateRefund(String transactionId, BigDecimal amount) {
        log.info("Initiating refund for transaction {} amount {}", transactionId, amount);
        
        Map<String, Object> transaction = transactions.get(transactionId);
        if (transaction == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Transaction not found");
            error.put("transactionId", transactionId);
            return error;
        }
        
        BigDecimal originalAmount = (BigDecimal) transaction.get("amount");
        BigDecimal refundAmount = amount != null ? amount : originalAmount;
        
        Map<String, Object> refund = new HashMap<>();
        refund.put("refundId", "REF-" + generateTransactionId());
        refund.put("originalTransactionId", transactionId);
        refund.put("refundAmount", refundAmount);
        refund.put("status", "PROCESSED");
        refund.put("processedAt", LocalDateTime.now());
        refund.put("message", "Refund processed successfully");
        
        return refund;
    }
    
    @Override
    public Map<String, Object> getPaymentStatistics() {
        log.info("Getting payment statistics");
        
        int totalTransactions = transactions.size();
        long completedCount = transactions.values().stream()
            .mapToLong(t -> "COMPLETED".equals(t.get("status")) ? 1 : 0)
            .sum();
        long failedCount = transactions.values().stream()
            .mapToLong(t -> "FAILED".equals(t.get("status")) ? 1 : 0)
            .sum();
        
        BigDecimal totalProcessed = transactions.values().stream()
            .filter(t -> "COMPLETED".equals(t.get("status")))
            .map(t -> (BigDecimal) t.get("amount"))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTransactions", totalTransactions);
        stats.put("completedTransactions", completedCount);
        stats.put("failedTransactions", failedCount);
        stats.put("successRate", totalTransactions > 0 ? (double) completedCount / totalTransactions : 0.0);
        stats.put("totalAmountProcessed", totalProcessed);
        stats.put("generatedAt", LocalDateTime.now());
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getPaymentStatus(String transactionId) {
        Map<String, Object> transaction = transactions.get(transactionId);
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction not found: " + transactionId);
        }
        return Map.of(
            "transactionId", transactionId,
            "status", transaction.get("status"),
            "amount", transaction.get("amount"),
            "policyId", transaction.get("policyId"),
            "billId", transaction.get("billId"),
            "attemptedAt", transaction.get("attemptedAt")
        );
    }
    
    @Override
    public void deletePayment(String transactionId) {
        log.info("Deleting payment transaction: {}", transactionId);
        if (transactions.remove(transactionId) == null) {
            throw new IllegalArgumentException("Transaction not found: " + transactionId);
        }
    }
    
    @Override
    public boolean validatePaymentRequest(Map<String, Object> paymentRequest) {
        log.info("Validating payment request: {}", paymentRequest);
        if (!paymentRequest.containsKey("policyId") || !paymentRequest.containsKey("amount") || !paymentRequest.containsKey("paymentMethod")) {
            return false;
        }
        BigDecimal amount = (BigDecimal) paymentRequest.get("amount");
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    @Override
public Map<String, Object> getDelinquentPolicies(int limit, int offset, int minDaysOverdue, String customerId) {
    log.info("Fetching delinquent policies with minDaysOverdue={}, limit={}, offset={}, customerId={}",
            minDaysOverdue, limit, offset, customerId);

    // Filter transactions to find delinquent policies
    List<String> delinquentPolicies = transactions.values().stream()
        .filter(t -> "FAILED".equals(t.get("status")) &&
                     t.containsKey("attemptedAt") &&
                     t.get("attemptedAt") instanceof LocalDateTime &&
                     ((LocalDateTime) t.get("attemptedAt")).isBefore(LocalDateTime.now().minusDays(minDaysOverdue)) &&
                     (customerId == null || customerId.equals(t.get("customerId"))))
        .map(t -> (String) t.get("policyId"))
        .distinct()
        .skip(offset)
        .limit(limit)
        .toList();

    long totalCount = transactions.values().stream()
        .filter(t -> "FAILED".equals(t.get("status")) &&
                     t.containsKey("attemptedAt") &&
                     t.get("attemptedAt") instanceof LocalDateTime &&
                     ((LocalDateTime) t.get("attemptedAt")).isBefore(LocalDateTime.now().minusDays(minDaysOverdue)) &&
                     (customerId == null || customerId.equals(t.get("customerId"))))
        .map(t -> t.get("policyId"))
        .distinct()
        .count();

    return Map.of(
        "totalCount", totalCount,
        "delinquentPolicies", delinquentPolicies
    );
}
    
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
    }
}