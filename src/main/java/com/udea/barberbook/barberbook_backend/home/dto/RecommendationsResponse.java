package com.udea.barberbook.barberbook_backend.home.dto;

import java.util.List;

// eligible is false until the client has minBookings bookings; the front hides the
// "Recomendado para ti" section then.
public record RecommendationsResponse(
    boolean eligible,
    int totalBookings,
    int minBookings,
    List<RecommendationCard> recommendations
) {}
