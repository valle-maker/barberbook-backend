package com.udea.barberbook.barberbook_backend.common.exception;

// The appointment is the client's own but cannot be cancelled or rescheduled right now (HU-15):
// too close to its start (code CAMBIO_FUERA_DE_PLAZO) or no longer active (CITA_NO_ACTIVA).
public class AppointmentChangeNotAllowedException extends RuntimeException {

    private final String code;

    public AppointmentChangeNotAllowedException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
