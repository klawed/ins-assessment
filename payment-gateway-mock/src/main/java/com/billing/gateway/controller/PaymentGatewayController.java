package com.billing.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/gateway")
@Slf4j
public class PaymentGatewayController {

    private final Random random = new Random();

    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        log.info("Hello endpoint called at {}", LocalDateTime.now());
        
        return ResponseEntity.ok(Map.of(
            "service", "payment-gateway-mock",
            "message", "Hello from Payment Gateway Mock!",
            "timestamp", LocalDateTime.now(),
            "status", "UP"
        ));
    }

    @PostMapping("/charge")
    public ResponseEntity<Map<String, Object>> processCharge(@RequestBody Map<String, Object> chargeRequest) {
        log.info("Processing charge request: {}", chargeRequest);
        
        // Simulate random success/failure for testing
        boolean success = random.nextBoolean();
        String transactionId = "TXN-" + System.currentTimeMillis();
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "transactionId", transactionId,
                "status", "SUCCESS",
                "message", "Payment processed successfully",
                "timestamp", LocalDateTime.now()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "transactionId", transactionId,
                "status", "FAILED",
                "error", "Insufficient funds",
                "message", "Payment processing failed",
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    @GetMapping("/status/{transactionId}")
    public ResponseEntity<Map<String, Object>> getTransactionStatus(@PathVariable String transactionId) {
        log.info("Getting transaction status for ID: {}", transactionId);
        
        // Mock transaction status
        return ResponseEntity.ok(Map.of(
            "transactionId", transactionId,
            "status", "SUCCESS",
            "message", "Transaction completed",
            "timestamp", LocalDateTime.now()
        ));
    }
}