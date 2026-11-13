package com.udea.barberbook.barberbook_backend.booking.service;

import java.util.List;
import java.util.UUID;

import com.udea.barberbook.barberbook_backend.booking.dto.MyAppointmentResponse;

public interface MyAppointmentService {

    enum Scope { UPCOMING, HISTORY }

    // The client's own appointments (identified by email): upcoming soonest first, or history
    // (finished or cancelled) most recent first.
    List<MyAppointmentResponse> list(String clientEmail, Scope scope);

    // One appointment. Throws AppointmentAccessDeniedException when it isn't the client's own or
    // doesn't exist (HS-02: the two cases are indistinguishable).
    MyAppointmentResponse get(String clientEmail, UUID appointmentId);
}
