package com.udea.barberbook.barberbook_backend.booking.dto;

import jakarta.validation.constraints.Size;

// Optional explanation that goes into the apology email ("Motivo: ...").
public record AdminCancelRequest(
    @Size(max = 200, message = "El motivo no puede superar los 200 caracteres.")
    String reason
) {}
