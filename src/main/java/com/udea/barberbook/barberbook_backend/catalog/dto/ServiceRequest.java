package com.udea.barberbook.barberbook_backend.catalog.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ServiceRequest(

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 80, message = "El nombre no puede superar los 80 caracteres.")
    String name,

    @NotNull(message = "La categoría es obligatoria.")
    @Pattern(
        regexp = "Cortes|Barba|Combos|Tratamientos",
        message = "Selecciona una categoría válida: Cortes, Barba, Combos o Tratamientos."
    )
    String category,

    @NotNull(message = "La duración es obligatoria.")
    @Min(value = 10, message = "La duración debe estar entre 10 y 240 minutos.")
    @Max(value = 240, message = "La duración debe estar entre 10 y 240 minutos.")
    Integer duration,

    @NotNull(message = "El precio es obligatorio.")
    @Positive(message = "El precio debe ser mayor a $0.")
    @Digits(integer = 12, fraction = 0, message = "El precio debe ser un valor entero en pesos, sin decimales.")
    BigDecimal price
) {}
