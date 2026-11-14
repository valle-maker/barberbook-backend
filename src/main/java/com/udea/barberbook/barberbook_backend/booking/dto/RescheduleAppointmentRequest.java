package com.udea.barberbook.barberbook_backend.booking.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// New date and time for an existing appointment; the stylist and the service never change.
public record RescheduleAppointmentRequest(

    @NotNull(message = "La fecha es obligatoria.")
    LocalDate date,

    @NotBlank(message = "La hora es obligatoria.")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora debe tener el formato HH:mm.")
    String time
) {}
