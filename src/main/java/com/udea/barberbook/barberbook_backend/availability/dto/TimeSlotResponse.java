package com.udea.barberbook.barberbook_backend.availability.dto;

import java.util.List;
import java.util.UUID;

// time is "HH:mm". stylistIds lists everyone who can take that slot: a single id
// when a specific stylist was requested, every free stylist for "any".
// stylistId is the stylist proposed for the slot (first of stylistIds); the
// front's mock contract carries one stylist per slot.
public record TimeSlotResponse(String time, UUID stylistId, List<UUID> stylistIds) {}
