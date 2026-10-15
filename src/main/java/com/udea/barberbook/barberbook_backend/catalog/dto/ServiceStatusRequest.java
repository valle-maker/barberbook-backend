package com.udea.barberbook.barberbook_backend.catalog.dto;

import com.udea.barberbook.barberbook_backend.catalog.domain.ServiceStatus;

import jakarta.validation.constraints.NotNull;

public record ServiceStatusRequest(

    @NotNull(message = "El estado es obligatorio (ACTIVO o INACTIVO).")
    ServiceStatus status
) {}
