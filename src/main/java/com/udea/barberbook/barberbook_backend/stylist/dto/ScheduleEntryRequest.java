package com.udea.barberbook.barberbook_backend.stylist.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record ScheduleEntryRequest(

    @NotNull(message = "El día es obligatorio")
    DayOfWeek dayOfWeek,

    @NotNull(message = "La hora de inicio es obligatoria")
    LocalTime startTime,

    @NotNull(message = "La hora de fin es obligatoria")
    LocalTime endTime,

    LocalTime breakStart,

    LocalTime breakEnd
) {}
