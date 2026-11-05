package com.nexus.notification.controller;

import com.nexus.notification.service.NotificationDeduplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationDeduplicationService deduplicationService;

    public NotificationController(NotificationDeduplicationService deduplicationService) {
        this.deduplicationService = deduplicationService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "service", "notification-service",
                "status", "UP",
                "cachedDeduplicationKeys", deduplicationService.getCachedEventCount()
        ));
    }
}
