package com.udea.barberbook.barberbook_backend.stylist.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateStylistRequest(

    @NotBlank(message = "El nombre es obligatorio")
    String fullName,

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 9, message = "Ingresa un número válido (mínimo 9 dígitos)")
    String phone,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo electrónico inválido")
    String email,

    @NotBlank(message = "La especialidad es obligatoria")
    String specialty,

    @NotEmpty(message = "Debes configurar al menos un día de horario")
    @Valid
    List<ScheduleEntryRequest> schedule,

    // Optional: services the stylist can perform. Can also be set later
    // through PUT /api/v1/admin/stylists/{id}/services.
    List<UUID> serviceIds,

    // Optional: profile picture URL (http/https).
    @Size(max = 500, message = "La URL de la foto no puede superar los 500 caracteres.")
    @Pattern(regexp = "^(https?://\\S+)?$", message = "La foto debe ser una URL http o https.")
    String photoUrl,

    // Optional: average rating 0.0-5.0 (placeholder until there is a reviews feature).
    @DecimalMin(value = "0.0", message = "La calificación debe estar entre 0 y 5.")
    @DecimalMax(value = "5.0", message = "La calificación debe estar entre 0 y 5.")
    @Digits(integer = 1, fraction = 1, message = "La calificación admite un solo decimal.")
    BigDecimal rating
) {}
