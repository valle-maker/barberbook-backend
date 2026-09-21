package com.udea.barberbook.barberbook_backend.stylist.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
    List<ScheduleEntryRequest> schedule
) {}
