package com.udea.barberbook.barberbook_backend.booking.service;

import java.util.UUID;

import com.udea.barberbook.barberbook_backend.booking.dto.MyAppointmentResponse;
import com.udea.barberbook.barberbook_backend.booking.dto.RescheduleAppointmentRequest;

// HU-15 (+ HS-02): the client's own active appointments can be cancelled or moved, with at least
// app.booking.min-notice-hours of notice. A foreign or nonexistent id throws
// AppointmentAccessDeniedException before anything is read or changed.
public interface ManageAppointmentService {

    // Marks the appointment CANCELADA; the slot is free again immediately.
    MyAppointmentResponse cancel(String clientEmail, UUID appointmentId);

    // Moves the same appointment (same code, stylist and service) to another free slot.
    MyAppointmentResponse reschedule(String clientEmail, UUID appointmentId, RescheduleAppointmentRequest request);
}
