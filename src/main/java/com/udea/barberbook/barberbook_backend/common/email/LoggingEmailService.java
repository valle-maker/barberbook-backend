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

    @Override
    public void sendAppointmentCancelled(
        String toEmail, String fullName, String code, String serviceName, String stylistName, String whenText
    ) {
        log.info(
            "[EMAIL STUB] Para: {} | Asunto: Cita cancelada {} | Hola {}, cancelaste tu cita de {} con {} del {}.",
            toEmail, code, fullName, serviceName, stylistName, whenText
        );
    }

    @Override
    public void sendAppointmentRescheduled(
        String toEmail, String fullName, String code, String serviceName, String stylistName, String whenText
    ) {
        log.info(
            "[EMAIL STUB] Para: {} | Asunto: Cita reprogramada {} | Hola {}, tu cita de {} con {} ahora es el {}.",
            toEmail, code, fullName, serviceName, stylistName, whenText
        );
    }

    @Override
    public void sendAppointmentCancelledByShop(
        String toEmail, String fullName, String code, String serviceName, String stylistName, String whenText,
        String reason
    ) {
        log.info(
            "[EMAIL STUB] Para: {} | Asunto: Tuvimos que cancelar tu cita {} | Hola {}, lamentamos cancelar tu cita "
                + "de {} con {} del {}. Motivo: {}. Te invitamos a reagendar.",
            toEmail, code, fullName, serviceName, stylistName, whenText, reason
        );
    }

    @Override
    public void sendAppointmentReassigned(
        String toEmail, String fullName, String code, String serviceName, String previousStylistName,
        String newStylistName, String whenText
    ) {
        log.info(
            "[EMAIL STUB] Para: {} | Asunto: Cambio de estilista en tu cita {} | Hola {}, tu cita de {} del {} "
                + "pasa de {} a {}.",
            toEmail, code, fullName, serviceName, whenText, previousStylistName, newStylistName
        );
    }
}
