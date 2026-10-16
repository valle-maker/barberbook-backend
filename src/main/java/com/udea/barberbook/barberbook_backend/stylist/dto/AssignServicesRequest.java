package com.udea.barberbook.barberbook_backend.stylist.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AssignServicesRequest(

    @NotNull(message = "La lista de servicios es obligatoria (puede ir vacía).")
    List<UUID> serviceIds
) {}
