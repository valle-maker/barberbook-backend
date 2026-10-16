package com.udea.barberbook.barberbook_backend.common.exception;

import java.time.Instant;
import java.util.Map;

// code is an optional machine-readable reason (e.g. "SLOT_TAKEN") the front can switch on.
public record ApiError(
    Instant timestamp,
    int status,
    String error,
    String message,
    Map<String, String> fieldErrors,
    String code
) {
    public ApiError(int status, String error, String message) {
        this(Instant.now(), status, error, message, null, null);
    }

    public ApiError(int status, String error, String message, Map<String, String> fieldErrors) {
        this(Instant.now(), status, error, message, fieldErrors, null);
    }

    public ApiError(int status, String error, String message, String code) {
        this(Instant.now(), status, error, message, null, code);
    }
}
