package com.udea.barberbook.barberbook_backend.common.email;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

// Stub: logs instead of sending a real email — no SMTP provider configured
// for this sprint (see the frontend integration doc). Swap for a real
// implementation of EmailService later without touching any caller.
@Slf4j
@Service
public class LoggingEmailService implements EmailService {

    @Override
    public void sendTemporaryCredentials(String toEmail, String fullName, String temporaryPassword) {
        log.info(
            "[EMAIL STUB] Para: {} | Asunto: Bienvenido a BarberBook | Hola {}, tu contraseña temporal es: {}",
            toEmail, fullName, temporaryPassword
        );
    }
}
