package com.udea.barberbook.barberbook_backend.notification.dto;

import java.util.List;

public record NotificationListResponse(long unreadCount, List<NotificationResponse> notifications) {}
