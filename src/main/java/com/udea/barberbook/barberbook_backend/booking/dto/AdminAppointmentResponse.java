package com.udea.barberbook.barberbook_backend.booking.dto;

import java.time.LocalDate;
import java.util.UUID;

// An appointment as the administrator sees it (HU-20): includes who booked it.
public record AdminAppointmentResponse(
    UUID id,
    String code,
    AppointmentState status,
    String clientName,
    String clientEmail,
    String clientPhone,
    UUID serviceId,
    String serviceName,
    int durationMinutes,
    long price,
    UUID stylistId,
    String stylistName,
    LocalDate date,
    String startTime,
    String endTime
) {}
