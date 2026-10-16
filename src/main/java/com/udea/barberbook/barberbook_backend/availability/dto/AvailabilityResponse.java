package com.udea.barberbook.barberbook_backend.availability.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// message is only present when slots is empty (no turns for that date).
public record AvailabilityResponse(
    LocalDate date,
    String stylistId,
    UUID serviceId,
    int durationMinutes,
    List<TimeSlotResponse> slots,
    String message
) {}
