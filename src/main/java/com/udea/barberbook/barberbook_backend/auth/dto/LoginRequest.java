package com.udea.barberbook.barberbook_backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo electrónico inválido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {}
