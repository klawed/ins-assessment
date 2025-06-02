package com.insurance.payment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.insurance.payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

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
        
        Map<String, Object> result = paymentService.processPayment(paymentRequest);
        return ResponseEntity.ok(result);
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
        
        if (policyId != null) {
            List<Map<String, Object>> history = paymentService.getPaymentHistory(policyId);
            return ResponseEntity.ok(history);
        } else {
            // For demo purposes, return empty list when no policyId specified
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/payments/{transactionId}/status")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String transactionId) {
        try {
            Map<String, Object> status = paymentService.getPaymentStatus(transactionId);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{paymentId}/retry")
    public ResponseEntity<Map<String, Object>> retryPayment(@PathVariable String paymentId) {
        log.info("Retrying payment for ID: {}", paymentId);
        
        Map<String, Object> retryResult = paymentService.retryPayment(paymentId);
        
        if (retryResult.containsKey("error")) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(retryResult);
    }

    @GetMapping("/failed")
    public ResponseEntity<List<Map<String, Object>>> getFailedPayments() {
        log.info("Getting failed payments");
        
        List<Map<String, Object>> failedPayments = paymentService.getFailedPayments();
        return ResponseEntity.ok(failedPayments);
    }
    
    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<Map<String, Object>>> getPaymentHistoryForPolicy(@PathVariable String policyId) {
        log.info("Getting payment history for policy: {}", policyId);
        
        List<Map<String, Object>> history = paymentService.getPaymentHistory(policyId);
        return ResponseEntity.ok(history);
    }
    
    @PostMapping("/{transactionId}/refund")
    public ResponseEntity<Map<String, Object>> initiateRefund(
            @PathVariable String transactionId,
            @RequestBody(required = false) Map<String, Object> refundRequest) {
        log.info("Initiating refund for transaction: {}", transactionId);
        
        BigDecimal amount = null;
        if (refundRequest != null && refundRequest.containsKey("amount")) {
            amount = new BigDecimal(refundRequest.get("amount").toString());
        }
        
        Map<String, Object> refundResult = paymentService.initiateRefund(transactionId, amount);
        
        if (refundResult.containsKey("error")) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(refundResult);
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPaymentStatistics() {
        log.info("Getting payment statistics");
        
        Map<String, Object> stats = paymentService.getPaymentStatistics();
        return ResponseEntity.ok(stats);
    }
    
    @PutMapping("/{transactionId}/status")
    public ResponseEntity<Map<String, Object>> updatePaymentStatus(
            @PathVariable String transactionId,
            @RequestBody Map<String, Object> statusUpdate) {
        log.info("Updating payment status for transaction {} to {}", transactionId, statusUpdate);
        
        String status = (String) statusUpdate.get("status");
        paymentService.updatePaymentStatus(transactionId, status);
        
        return ResponseEntity.ok(Map.of(
            "transactionId", transactionId,
            "status", "updated",
            "timestamp", LocalDateTime.now()
        ));
    }

    @GetMapping("/delinquent")
    public ResponseEntity<Map<String, Object>> getDelinquentPolicies(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "1") int minDaysOverdue,
            @RequestParam(required = false) String customerId) {
        
        log.info("Getting delinquent policies with minDaysOverdue: {}, customerId: {}", minDaysOverdue, customerId);
        
        // This endpoint delegates to billing service in a real implementation
        // For now, return mock data structure
        return ResponseEntity.ok(Map.of(
            "totalCount", 0,
            "delinquentPolicies", List.of(),
            "message", "This endpoint should delegate to billing service"
        ));
    }
}