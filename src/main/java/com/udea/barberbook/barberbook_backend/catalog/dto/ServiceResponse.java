package com.udea.barberbook.barberbook_backend.catalog.dto;

import java.util.UUID;

import com.udea.barberbook.barberbook_backend.catalog.domain.ServiceStatus;

public record ServiceResponse(
    UUID id,
    String name,
    String category,
    int duration,
    long price,
    ServiceStatus status
) {}
