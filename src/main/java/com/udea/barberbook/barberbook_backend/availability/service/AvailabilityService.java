package com.udea.barberbook.barberbook_backend.availability.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.udea.barberbook.barberbook_backend.availability.dto.AvailabilityResponse;
import com.udea.barberbook.barberbook_backend.availability.dto.DayAvailabilityResponse;
import com.udea.barberbook.barberbook_backend.catalog.domain.BarberService;
import com.udea.barberbook.barberbook_backend.stylist.domain.Stylist;

public interface AvailabilityService {

    // stylistId is a stylist UUID or the literal "any" (any enabled stylist).
    AvailabilityResponse getAvailability(String stylistId, UUID serviceId, LocalDate date);

    // One entry per day of the month (month is 1-12): does the day have any free slot?
    List<DayAvailabilityResponse> getMonthAvailability(String stylistId, UUID serviceId, int year, int month);

    // Building blocks reused by the booking flow (HU-13). They must run inside a transaction.
    BarberService findActiveService(UUID serviceId);

    List<Stylist> resolveCandidates(String stylistId, BarberService service);

    boolean isStylistFreeAt(Stylist stylist, BarberService service, LocalDateTime slotStart);
}
