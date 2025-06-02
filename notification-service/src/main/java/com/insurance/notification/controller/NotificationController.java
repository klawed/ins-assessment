package com.insurance.notification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        log.info("Hello endpoint called at {}", LocalDateTime.now());
        
        return ResponseEntity.ok(Map.of(
            "service", "notification-service",
            "message", "Hello from Notification Service!",
            "timestamp", LocalDateTime.now(),
            "status", "UP"
        ));
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendNotification(@RequestBody Map<String, Object> notificationRequest) {
        log.info("Sending notification: {}", notificationRequest);
        
        // TODO: Implement actual notification sending
        return ResponseEntity.ok(Map.of(
            "notificationId", "NOTIF-" + System.currentTimeMillis(),
            "status", "SENT",
            "message", "Notification send endpoint - implementation pending",
            "timestamp", LocalDateTime.now()
        ));
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<Map<String, Object>> getNotifications(@PathVariable String policyId) {
        log.info("Getting notifications for policy ID: {}", policyId);
        
        // TODO: Implement actual notification retrieval
        return ResponseEntity.ok(Map.of(
            "policyId", policyId,
            "notifications", List.of(),
            "count", 0,
            "message", "Notification retrieval endpoint - implementation pending"
        ));
    }
}