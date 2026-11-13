package com.udea.barberbook.barberbook_backend.audit.domain;

public enum AuditEventType {
    // An authenticated user tried something their role doesn't allow (HS-01 / HA-01).
    ACCESO_DENEGADO,
    // A client tried to read or change an appointment that isn't theirs (HS-02 / HA-02).
    RESERVA_AJENA
}
