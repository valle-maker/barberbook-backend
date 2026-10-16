package com.udea.barberbook.barberbook_backend.availability.service;

import java.time.LocalDate;
import java.util.UUID;

import com.udea.barberbook.barberbook_backend.availability.dto.AvailabilityResponse;

public interface AvailabilityService {

    // stylistId is a stylist UUID or the literal "any" (any enabled stylist).
    AvailabilityResponse getAvailability(String stylistId, UUID serviceId, LocalDate date);
}
