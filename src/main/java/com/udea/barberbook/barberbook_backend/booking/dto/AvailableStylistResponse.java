package com.udea.barberbook.barberbook_backend.booking.dto;

import java.util.UUID;

// A stylist who could take over an appointment (HU-20): does the service and is free at that time.
public record AvailableStylistResponse(UUID id, String fullName, String specialty) {}
