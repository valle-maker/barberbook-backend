package com.udea.barberbook.barberbook_backend.stylist.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Both fields optional. A null field is left unchanged; photoUrl "" removes the photo.
public record UpdateStylistProfileRequest(

    @Size(max = 500, message = "La URL de la foto no puede superar los 500 caracteres.")
    @Pattern(regexp = "^(https?://\\S+)?$", message = "La foto debe ser una URL http o https.")
    String photoUrl,

    @DecimalMin(value = "0.0", message = "La calificación debe estar entre 0 y 5.")
    @DecimalMax(value = "5.0", message = "La calificación debe estar entre 0 y 5.")
    @Digits(integer = 1, fraction = 1, message = "La calificación admite un solo decimal.")
    BigDecimal rating
) {}
