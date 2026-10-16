package com.udea.barberbook.barberbook_backend.notification.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.udea.barberbook.barberbook_backend.notification.dto.NotificationListResponse;
import com.udea.barberbook.barberbook_backend.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

// The bell on the home screen: recent notices of the authenticated user, newest first.
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<NotificationListResponse> list(
        Authentication authentication, @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(notificationService.list(authentication.getName(), limit));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(Authentication authentication, @PathVariable UUID id) {
        notificationService.markRead(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(Authentication authentication) {
        notificationService.markAllRead(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
