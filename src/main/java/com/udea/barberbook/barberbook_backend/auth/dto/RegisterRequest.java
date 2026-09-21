package com.udea.barberbook.barberbook_backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

    @NotBlank(message = "El nombre es obligatorio")
    String fullName,

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 9, message = "Ingresa un número válido (mínimo 9 dígitos)")
    String phone,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo electrónico inválido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
        message = "La contraseña debe tener mínimo 8 caracteres e incluir letras, números y un símbolo"
    )
    String password
) {}
