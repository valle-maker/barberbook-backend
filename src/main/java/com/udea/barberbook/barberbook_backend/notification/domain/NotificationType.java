package com.udea.barberbook.barberbook_backend.notification.domain;

public enum NotificationType {
    CITA_CONFIRMADA,
    CITA_CANCELADA,
    CITA_REPROGRAMADA,
    // HU-20: changes made by the shop (administrator), not by the client
    CITA_CANCELADA_POR_LOCAL,
    CITA_REASIGNADA
}
