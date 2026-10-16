package com.udea.barberbook.barberbook_backend.notification.service;

import java.util.UUID;

import com.udea.barberbook.barberbook_backend.notification.domain.NotificationType;
import com.udea.barberbook.barberbook_backend.notification.dto.NotificationListResponse;
import com.udea.barberbook.barberbook_backend.user.domain.User;

public interface NotificationService {

    // Called by other features (e.g. booking) inside their own transaction.
    void create(User user, NotificationType type, String title, String message);

    NotificationListResponse list(String email, int limit);

    void markRead(String email, UUID notificationId);

    void markAllRead(String email);
}
