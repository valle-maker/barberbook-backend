package com.udea.barberbook.barberbook_backend.common.exception;

// The appointment doesn't belong to the caller, or doesn't exist. Both cases are deliberately
// indistinguishable to the client (HS-02 / HA-02): same status, same generic message.
public class AppointmentAccessDeniedException extends RuntimeException {

    public static final String MESSAGE = "No tienes autorización para consultar esta información.";

    public AppointmentAccessDeniedException() {
        super(MESSAGE);
    }
}
