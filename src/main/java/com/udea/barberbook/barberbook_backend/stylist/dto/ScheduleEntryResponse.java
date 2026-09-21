package com.udea.barberbook.barberbook_backend.stylist.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ScheduleEntryResponse(
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    LocalTime breakStart,
    LocalTime breakEnd
) {}
