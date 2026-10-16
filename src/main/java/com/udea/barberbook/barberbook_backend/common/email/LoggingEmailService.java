package com.udea.barberbook.barberbook_backend.common.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

// Stub: logs instead of sending a real email — no SMTP provider configured
// for this sprint (see the frontend integration doc). Swap for a real
// implementation of EmailService later without touching any caller.
@Slf4j
@Service
@ConditionalOnExpression("'${app.email.provider:log}'.equals('log')")
public class LoggingEmailService implements EmailService {

    @Override
    public void sendTemporaryCredentials(String toEmail, String fullName, String temporaryPassword) {
        log.info(
            "[EMAIL STUB] Para: {} | Asunto: Bienvenido a BarberBook | Hola {}, tu contraseña temporal es: {}",
            toEmail, fullName, temporaryPassword
        );
    }

    @Override
    public void sendAppointmentConfirmation(
        String toEmail, String fullName, String code, String serviceName, String stylistName,
        String whenText, long price, String address
    ) {
        log.info(
            "[EMAIL STUB] Para: {} | Asunto: Cita confirmada {} | Hola {}, tu cita de {} con {} es el {}. "
                + "Total a pagar en el local: ${}. Dirección: {}",
            toEmail, code, fullName, serviceName, stylistName, whenText, price, address
        );
    }
}
