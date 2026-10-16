package com.udea.barberbook.barberbook_backend.booking.service;

import com.udea.barberbook.barberbook_backend.booking.dto.AppointmentResponse;
import com.udea.barberbook.barberbook_backend.booking.dto.CreateAppointmentRequest;

public interface BookingService {

    // Books the slot for the authenticated client (identified by email) or fails with 409
    // if it is no longer free.
    AppointmentResponse confirm(String clientEmail, CreateAppointmentRequest request);
}
