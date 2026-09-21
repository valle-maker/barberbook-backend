package com.udea.barberbook.barberbook_backend.auth.dto;

public record AuthResponse(
    String token,
    String role,
    String fullName,
    String email
) {}
