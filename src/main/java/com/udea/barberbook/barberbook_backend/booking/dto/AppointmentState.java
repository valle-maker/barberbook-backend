package com.udea.barberbook.barberbook_backend.booking.dto;

// What the client sees. Stored status is only CONFIRMADA/CANCELADA; COMPLETADA is derived: a
// confirmed appointment whose time has passed (the system doesn't track attendance).
public enum AppointmentState {
    CONFIRMADA,
    COMPLETADA,
    CANCELADA
}
