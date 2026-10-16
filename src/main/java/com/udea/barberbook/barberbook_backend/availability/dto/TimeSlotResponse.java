package com.udea.barberbook.barberbook_backend.availability.dto;

import java.util.List;
import java.util.UUID;

// time is "HH:mm". stylistIds lists who can take that slot: a single id when a
// specific stylist was requested, every free stylist for "any".
public record TimeSlotResponse(String time, List<UUID> stylistIds) {}
