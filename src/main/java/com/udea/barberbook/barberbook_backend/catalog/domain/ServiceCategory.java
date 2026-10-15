package com.udea.barberbook.barberbook_backend.catalog.domain;

import java.util.Arrays;

public enum ServiceCategory {
    CORTES("Cortes"),
    BARBA("Barba"),
    COMBOS("Combos"),
    TRATAMIENTOS("Tratamientos");

    private final String label;

    ServiceCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ServiceCategory fromLabel(String label) {
        return Arrays.stream(values())
            .filter(category -> category.label.equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Categoría desconocida: " + label));
    }
}
