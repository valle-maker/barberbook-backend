package com.udea.barberbook.barberbook_backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(

    @NotBlank(message = "El id_token es obligatorio")
    String idToken
) {}
