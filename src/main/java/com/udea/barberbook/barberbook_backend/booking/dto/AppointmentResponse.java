package com.udea.barberbook.barberbook_backend.booking.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.udea.barberbook.barberbook_backend.booking.domain.AppointmentStatus;

public record AppointmentResponse(
    UUID id,
    String code,
    AppointmentStatus status,
    UUID serviceId,
    String serviceName,
    int durationMinutes,
    long price,
    UUID stylistId,
    String stylistName,
    LocalDate date,
    String startTime,
    String endTime,
    String message
) {}
