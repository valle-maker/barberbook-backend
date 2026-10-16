package com.udea.barberbook.barberbook_backend.notification.dto;

import java.time.Instant;
import java.util.UUID;

import com.udea.barberbook.barberbook_backend.notification.domain.NotificationType;

public record NotificationResponse(
    UUID id,
    NotificationType type,
    String title,
    String message,
    Instant createdAt,
    boolean read
) {}
