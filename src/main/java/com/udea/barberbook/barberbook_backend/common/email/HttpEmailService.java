package com.udea.barberbook.barberbook_backend.common.email;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

// Sends real emails through a provider's HTTP API (EMAIL_PROVIDER=brevo or resend). HTTP is used
// instead of SMTP because Render's free plan blocks outbound SMTP ports. Sending is asynchronous:
// a slow or failing provider never delays an API response or undoes a confirmed booking.
// With EMAIL_PROVIDER unset (or "log") LoggingEmailService is used instead.
@Slf4j
@Service
@ConditionalOnExpression("!'${app.email.provider:log}'.equals('log')")
public class HttpEmailService implements EmailService {

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";
    private static final String RESEND_URL = "https://api.resend.com/emails";

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final JsonMapper json = JsonMapper.builder().build();

    @Value("${app.email.provider}")
    private String provider;

    @Value("${app.email.api-key:}")
    private String apiKey;

    @Value("${app.email.from:}")
    private String from;

    @Value("${app.email.from-name:BarberBook}")
    private String fromName;

    @Value("${app.email.api-url:}")
    private String apiUrlOverride;

    @PostConstruct
    void validate() {
        String p = provider.toLowerCase();
        if (!p.equals("brevo") && !p.equals("resend")) {
            throw new IllegalStateException("EMAIL_PROVIDER debe ser log, brevo o resend (recibido: " + provider + ")");
        }
        if (apiKey.isBlank() || from.isBlank()) {
            throw new IllegalStateException("Con EMAIL_PROVIDER=" + p + " hay que definir EMAIL_API_KEY y EMAIL_FROM");
        }
        log.info("Correo real activado con el proveedor {} (remitente {})", p, from);
    }

    @Override
    public void sendTemporaryCredentials(String toEmail, String fullName, String temporaryPassword) {
        String html = "<p>Hola " + esc(fullName) + ",</p>"
            + "<p>Se creó tu cuenta de estilista en BarberBook. Tu contraseña temporal es:</p>"
            + "<p><strong>" + esc(temporaryPassword) + "</strong></p>"
            + "<p>Inicia sesión con este correo y cámbiala cuando puedas.</p>";
        send(toEmail, fullName, "Bienvenido a BarberBook", html);
    }

    @Override
    public void sendAppointmentConfirmation(
        String toEmail, String fullName, String code, String serviceName, String stylistName,
        String whenText, long price, String address
    ) {
        String html = "<p>Hola " + esc(fullName) + ",</p>"
            + "<p>Tu cita quedó <strong>confirmada</strong>.</p>"
            + "<ul>"
            + "<li><strong>Código de reserva:</strong> " + esc(code) + "</li>"
            + "<li><strong>Servicio:</strong> " + esc(serviceName) + "</li>"
            + "<li><strong>Estilista:</strong> " + esc(stylistName) + "</li>"
            + "<li><strong>Fecha y hora:</strong> " + esc(whenText) + "</li>"
            + "<li><strong>Total a pagar en el local:</strong> $" + String.format("%,d", price).replace(',', '.') + "</li>"
            + "<li><strong>Dirección:</strong> " + esc(address) + "</li>"
            + "</ul>"
            + "<p>Te esperamos.</p>";
        send(toEmail, fullName, "Cita confirmada " + code, html);
    }

    private void send(String toEmail, String toName, String subject, String html) {
        try {
            boolean brevo = provider.equalsIgnoreCase("brevo");
            String url = !apiUrlOverride.isBlank() ? apiUrlOverride : brevo ? BREVO_URL : RESEND_URL;

            Map<String, Object> body = brevo
                ? Map.of(
                    "sender", Map.of("name", fromName, "email", from),
                    "to", List.of(Map.of("email", toEmail, "name", toName)),
                    "subject", subject,
                    "htmlContent", html)
                : Map.of(
                    "from", fromName + " <" + from + ">",
                    "to", List.of(toEmail),
                    "subject", subject,
                    "html", html);

            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
            if (brevo) {
                request.header("api-key", apiKey);
            } else {
                request.header("Authorization", "Bearer " + apiKey);
            }

            client.sendAsync(request.build(), HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, error) -> {
                    if (error != null) {
                        log.warn("No se pudo enviar el correo '{}' a {}: {}", subject, toEmail, error.getMessage());
                    } else if (response.statusCode() / 100 != 2) {
                        log.warn("El proveedor rechazó el correo '{}' a {}: HTTP {} {}",
                            subject, toEmail, response.statusCode(), response.body());
                    } else {
                        log.info("Correo '{}' enviado a {}", subject, toEmail);
                    }
                });
        } catch (RuntimeException e) {
            log.warn("No se pudo preparar el correo '{}' a {}: {}", subject, toEmail, e.getMessage());
        }
    }

    private static String esc(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }
}
