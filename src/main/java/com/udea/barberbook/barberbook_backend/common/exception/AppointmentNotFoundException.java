package com.udea.barberbook.barberbook_backend.common.exception;

// Administrator side only (HU-20): the id matches no appointment. Clients never see this one: for
// them a missing and a foreign appointment are the same generic 403 (HS-02).
public class AppointmentNotFoundException extends RuntimeException {

    public AppointmentNotFoundException() {
        super("No encontramos la cita indicada.");
    }
}
