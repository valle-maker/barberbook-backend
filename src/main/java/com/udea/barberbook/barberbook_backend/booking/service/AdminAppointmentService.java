package com.udea.barberbook.barberbook_backend.booking.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.udea.barberbook.barberbook_backend.booking.dto.AdminAppointmentResponse;
import com.udea.barberbook.barberbook_backend.booking.dto.AvailableStylistResponse;

// HU-20: the administrator manages other people's appointments to handle contingencies (a sick
// barber, a problem at the shop). Unlike the client side there is no minimum-notice rule here.
public interface AdminAppointmentService {

    // Active (confirmed, not finished) appointments, soonest first. With a date, only that day's;
    // without, the next 60 days. Optionally only one stylist's.
    List<AdminAppointmentResponse> list(LocalDate date, UUID stylistId);

    // Stylists that could take the appointment over at the same time (excluding the current one).
    List<AvailableStylistResponse> availableStylists(UUID appointmentId);

    // Cancels it, frees the slot, and apologises to the client by email and notification.
    AdminAppointmentResponse cancelForContingency(UUID appointmentId, String reason);

    // Moves it to another stylist keeping the same date and time, and tells the client.
    AdminAppointmentResponse reassign(UUID appointmentId, UUID newStylistId);
}
