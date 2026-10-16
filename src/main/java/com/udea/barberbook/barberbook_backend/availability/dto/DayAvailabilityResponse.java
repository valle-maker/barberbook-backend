package com.udea.barberbook.barberbook_backend.availability.dto;

import java.time.LocalDate;

public record DayAvailabilityResponse(LocalDate date, boolean hasAvailability) {}
