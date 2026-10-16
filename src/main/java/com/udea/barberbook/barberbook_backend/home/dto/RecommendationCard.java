package com.udea.barberbook.barberbook_backend.home.dto;

import java.util.UUID;

public record RecommendationCard(
    UUID serviceId,
    String serviceName,
    int durationMinutes,
    long price,
    UUID stylistId,
    String stylistName,
    String label
) {}
