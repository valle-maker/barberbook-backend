package com.udea.barberbook.barberbook_backend.stylist.dto;

import java.util.List;
import java.util.UUID;

public record StylistResponse(
    UUID id,
    String fullName,
    String email,
    String phone,
    String specialty,
    String role,
    List<ScheduleEntryResponse> schedule,
    String message
) {}
