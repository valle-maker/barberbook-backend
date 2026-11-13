package com.udea.barberbook.barberbook_backend.booking.dto;

import java.time.LocalDate;
import java.util.UUID;

// One appointment as shown in "Mis reservas" (list and detail). Times are local to the business
// time zone. canManage = still confirmed and far enough ahead to be cancelled or rescheduled
// (the notice rule of HU-15), so the UI knows whether to offer those options.
public record MyAppointmentResponse(
    UUID id,
    String code,
    AppointmentState status,
    UUID serviceId,
    String serviceName,
    int durationMinutes,
    long price,
    UUID stylistId,
    String stylistName,
    LocalDate date,
    String startTime,
    String endTime,
    boolean canManage
) {}
