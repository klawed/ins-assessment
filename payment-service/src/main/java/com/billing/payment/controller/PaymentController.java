package com.billing.payment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@Slf4j
public class PaymentController {

    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        log.info("Hello endpoint called at {}", LocalDateTime.now());
        
        return ResponseEntity.ok(Map.of(
            "service", "payment-service",
            "message", "Hello from Payment Service!",
            "timestamp", LocalDateTime.now(),
            "status", "UP"
        ));
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, Object> paymentRequest) {
        log.info("Processing payment request: {}", paymentRequest);
        
        String policyId = (String) paymentRequest.get("policyId");
        BigDecimal amount = new BigDecimal(paymentRequest.get("amount").toString());
        
        // Simulate payment processing with different outcomes
        Map<String, Object> response = simulatePaymentProcessing(policyId, amount);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getPaymentHistory(
            @RequestParam(required = false) String policyId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        log.info("Getting payment history for policyId: {}, status: {}", policyId, status);
        List<Map<String, Object>> history = getMockPaymentHistory(policyId, status);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{paymentId}/status")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String paymentId) {
        log.info("Getting payment status for ID: {}", paymentId);
        
        Map<String, Object> payment = findMockPaymentByTransactionId(paymentId);
        
        if (payment != null) {
            return ResponseEntity.ok(payment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{paymentId}/retry")
    public ResponseEntity<Map<String, Object>> retryPayment(@PathVariable String paymentId) {
        log.info("Retrying payment for ID: {}", paymentId);
        
        Map<String, Object> retryResponse = Map.of(
            "originalTransactionId", paymentId,
            "newTransactionId", "TXN-" + LocalDateTime.now().getYear() + "-" + 
                String.format("%06d", (int)(Math.random() * 1000000)),
            "status", "PROCESSING",
            "retryAttempt", 1,
            "timestamp", LocalDateTime.now().toString(),
            "estimatedCompletionTime", LocalDateTime.now().plusMinutes(5).toString()
        );
        
        return ResponseEntity.ok(retryResponse);
    }

    @GetMapping("/delinquent")
    public ResponseEntity<Map<String, Object>> getDelinquentPolicies(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "1") int minDaysOverdue,
            @RequestParam(required = false) String customerId) {
        
        log.info("Getting delinquent policies with minDaysOverdue: {}, customerId: {}", minDaysOverdue, customerId);
        
        List<Map<String, Object>> delinquentPolicies = getMockDelinquentPolicies(customerId, minDaysOverdue);
        
        Map<String, Object> response = Map.of(
            "totalCount", delinquentPolicies.size(),
            "delinquentPolicies", delinquentPolicies
        );
        
        return ResponseEntity.ok(response);
    }

    // Mock data generation methods
    private Map<String, Object> simulatePaymentProcessing(String policyId, BigDecimal amount) {
        String transactionId = "TXN-" + LocalDateTime.now().getYear() + "-" + 
            String.format("%06d", (int)(Math.random() * 1000000));
        
        // Simulate different outcomes based on policy ID for predictable testing
        boolean shouldFail = policyId.equals("POLICY-FAIL") || Math.random() < 0.2; // 20% failure rate
        
        if (shouldFail) {
            return Map.of(
                "transactionId", transactionId,
                "status", "FAILED",
                "amount", amount,
                "policyId", policyId,
                "paymentMethod", "visa_****1234",
                "timestamp", LocalDateTime.now().toString(),
                "errorCode", "INSUFFICIENT_FUNDS",
                "message", "Payment declined due to insufficient funds",
                "retrySchedule", Map.of(
                    "nextRetryDate", LocalDateTime.now().plusDays(3).toString(),
                    "maxRetries", 3,
                    "currentRetryCount", 0
                )
            );
        } else {
            return Map.of(
                "transactionId", transactionId,
                "status", "SUCCESS",
                "amount", amount,
                "policyId", policyId,
                "paymentMethod", "visa_****1234",
                "timestamp", LocalDateTime.now().toString(),
                "confirmationCode", "CONF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "processingFee", BigDecimal.ZERO,
                "message", "Payment processed successfully"
            );
        }
    }

    private List<Map<String, Object>> getMockPaymentHistory(String policyId, String status) {
        return Arrays.asList(
            Map.of(
                "transactionId", "TXN-2024-001235",
                "policyId", "POLICY-123",
                "amount", new BigDecimal("171.00"),
                "status", "FAILED",
                "paymentMethod", "visa_****1234",
                "timestamp", LocalDateTime.now().minusDays(1).toString(),
                "errorCode", "INSUFFICIENT_FUNDS",
                "retryCount", 0
            ),
            Map.of(
                "transactionId", "TXN-2024-001189",
                "policyId", "POLICY-456",
                "amount", new BigDecimal("89.00"),
                "status", "SUCCESS",
                "paymentMethod", "visa_****1234",
                "timestamp", LocalDateTime.now().minusDays(15).toString(),
                "confirmationCode", "CONF-ABC123",
                "retryCount", 0
            ),
            Map.of(
                "transactionId", "TXN-2024-001156",
                "policyId", "POLICY-123",
                "amount", new BigDecimal("156.00"),
                "status", "SUCCESS",
                "paymentMethod", "visa_****1234",
                "timestamp", LocalDateTime.now().minusDays(45).toString(),
                "confirmationCode", "CONF-DEF456",
                "retryCount", 0
            )
        );
    }

    private Map<String, Object> findMockPaymentByTransactionId(String transactionId) {
        List<Map<String, Object>> allPayments = getMockPaymentHistory(null, null);
        
        return allPayments.stream()
                .filter(p -> p.get("transactionId").equals(transactionId))
                .findFirst()
                .orElse(null);
    }

    private List<Map<String, Object>> getMockDelinquentPolicies(String customerId, int minDaysOverdue) {
        return Arrays.asList(
            Map.of(
                "policyId", "POLICY-123",
                "customerId", "CUST-001",
                "customerName", "John Doe",
                "policyType", "AUTO_INSURANCE",
                "premiumAmount", new BigDecimal("156.00"),
                "dueDate", LocalDateTime.now().minusDays(3).toLocalDate().toString(),
                "daysOverdue", 3,
                "lateFee", new BigDecimal("15.00"),
                "totalAmountDue", new BigDecimal("171.00"),
                "status", "OVERDUE",
                "gracePeriodExpires", LocalDateTime.now().plusDays(7).toLocalDate().toString(),
                "lastPaymentDate", LocalDateTime.now().minusMonths(1).toLocalDate().toString(),
                "contactInfo", Map.of(
                    "email", "john.doe@example.com",
                    "phone", "+1-555-123-4567"
                )
            ),
            Map.of(
                "policyId", "POLICY-111",
                "customerId", "CUST-002",
                "customerName", "Jane Smith",
                "policyType", "AUTO_INSURANCE",
                "premiumAmount", new BigDecimal("175.00"),
                "dueDate", LocalDateTime.now().minusDays(7).toLocalDate().toString(),
                "daysOverdue", 7,
                "lateFee", new BigDecimal("15.00"),
                "totalAmountDue", new BigDecimal("190.00"),
                "status", "GRACE_PERIOD",
                "gracePeriodExpires", LocalDateTime.now().plusDays(3).toLocalDate().toString(),
                "lastPaymentDate", LocalDateTime.now().minusMonths(2).toLocalDate().toString(),
                "contactInfo", Map.of(
                    "email", "jane.smith@example.com",
                    "phone", "+1-555-987-6543"
                )
            )
        );
    }
}
