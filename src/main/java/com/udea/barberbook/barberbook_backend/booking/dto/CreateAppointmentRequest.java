package com.udea.barberbook.barberbook_backend.booking.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateAppointmentRequest(

    @NotNull(message = "El servicio es obligatorio.")
    UUID serviceId,

    // A stylist id, or "any" to let the system assign whoever is free at that time.
    @NotBlank(message = "El estilista es obligatorio.")
    String stylistId,

    @NotNull(message = "La fecha es obligatoria.")
    LocalDate date,

    @NotBlank(message = "La hora es obligatoria.")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora debe tener el formato HH:mm.")
    String time
) {}
