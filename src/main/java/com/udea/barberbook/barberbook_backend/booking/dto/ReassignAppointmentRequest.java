package com.udea.barberbook.barberbook_backend.booking.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ReassignAppointmentRequest(
    @NotNull(message = "El estilista es obligatorio.")
    UUID stylistId
) {}
