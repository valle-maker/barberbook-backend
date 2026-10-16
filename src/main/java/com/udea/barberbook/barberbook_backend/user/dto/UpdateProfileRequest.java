package com.udea.barberbook.barberbook_backend.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// null or "" removes the profile photo.
public record UpdateProfileRequest(

    @Size(max = 500, message = "La URL de la foto no puede superar los 500 caracteres.")
    @Pattern(regexp = "^(https?://\\S+)?$", message = "La foto debe ser una URL http o https.")
    String photoUrl
) {}
