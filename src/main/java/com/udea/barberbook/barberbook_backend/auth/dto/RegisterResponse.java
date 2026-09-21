package com.udea.barberbook.barberbook_backend.auth.dto;

import java.util.UUID;

public record RegisterResponse(
    UUID id,
    String fullName,
    String email,
    String role,
    String message
) {}
