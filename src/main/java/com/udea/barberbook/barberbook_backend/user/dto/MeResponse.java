package com.udea.barberbook.barberbook_backend.user.dto;

import java.util.UUID;

// Data for the home header ("¡Buenos días! Juan" + photo). The greeting by time of day is
// composed by the front from firstName.
public record MeResponse(
    UUID id,
    String fullName,
    String firstName,
    String email,
    String phone,
    String role,
    String photoUrl
) {}
