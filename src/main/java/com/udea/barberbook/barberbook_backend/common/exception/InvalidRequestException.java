package com.udea.barberbook.barberbook_backend.common.exception;

// A request whose parameters are well-formed JSON/HTTP but not acceptable (answered with 400).
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
