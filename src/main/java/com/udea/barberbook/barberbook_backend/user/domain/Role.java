package com.udea.barberbook.barberbook_backend.user.domain;

public enum Role {
    CLIENTE,
    ESTILISTA,
    ADMIN;

    // The frontend's session state uses these lowercase English values (see App.tsx UserRole)
    public String toFrontendValue() {
        return switch (this) {
            case CLIENTE -> "client";
            case ESTILISTA -> "stylist";
            case ADMIN -> "admin";
        };
    }
}
