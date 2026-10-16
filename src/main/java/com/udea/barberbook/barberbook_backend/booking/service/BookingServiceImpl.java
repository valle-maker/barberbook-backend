package com.udea.barberbook.barberbook_backend.booking.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.udea.barberbook.barberbook_backend.availability.service.AvailabilityService;
import com.udea.barberbook.barberbook_backend.booking.domain.Appointment;
import com.udea.barberbook.barberbook_backend.booking.domain.AppointmentStatus;
import com.udea.barberbook.barberbook_backend.booking.dto.AppointmentResponse;
import com.udea.barberbook.barberbook_backend.booking.dto.CreateAppointmentRequest;
import com.udea.barberbook.barberbook_backend.booking.repository.AppointmentRepository;
import com.udea.barberbook.barberbook_backend.catalog.domain.BarberService;
import com.udea.barberbook.barberbook_backend.common.email.EmailService;
import com.udea.barberbook.barberbook_backend.common.exception.SlotUnavailableException;
import com.udea.barberbook.barberbook_backend.notification.domain.NotificationType;
import com.udea.barberbook.barberbook_backend.notification.service.NotificationService;
import com.udea.barberbook.barberbook_backend.stylist.domain.Stylist;
import com.udea.barberbook.barberbook_backend.stylist.repository.StylistRepository;
import com.udea.barberbook.barberbook_backend.user.domain.User;
import com.udea.barberbook.barberbook_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    static final String SLOT_TAKEN_MESSAGE =
        "Lo sentimos, este turno acaba de ser ocupado. Por favor selecciona otro horario.";

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O/1/I
    private static final int CODE_LENGTH = 6;
    private static final DateTimeFormatter DAY_FORMAT =
        DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-CO"));
    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final AvailabilityService availabilityService;
    private final AppointmentRepository appointmentRepository;
    private final StylistRepository stylistRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.business.address}")
    private String businessAddress;

    @Override
    @Transactional
    public AppointmentResponse confirm(String clientEmail, CreateAppointmentRequest request) {
        User client = userRepository.findByEmail(clientEmail)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        BarberService service = availabilityService.findActiveService(request.serviceId());
        List<Stylist> candidates = availabilityService.resolveCandidates(request.stylistId(), service);
        if (candidates.isEmpty()) {
            throw new SlotUnavailableException(SLOT_TAKEN_MESSAGE);
        }

        LocalDateTime start = request.date().atTime(LocalTime.parse(request.time()));
        LocalDateTime end = start.plusMinutes(service.getDurationMinutes());

        // Serialize concurrent bookings: whoever gets the stylist row lock first books; the
        // next one re-checks availability after the commit and finds the slot taken.
        List<UUID> ids = candidates.stream().map(Stylist::getId).toList();
        Stylist chosen = stylistRepository.lockAllByIdOrdered(ids).stream()
            .filter(stylist -> availabilityService.isStylistFreeAt(stylist, service, start))
            .findFirst()
            .orElseThrow(() -> new SlotUnavailableException(SLOT_TAKEN_MESSAGE));

        Appointment appointment = appointmentRepository.save(Appointment.builder()
            .stylist(chosen)
            .service(service)
            .client(client)
            .startAt(start)
            .endAt(end)
            .status(AppointmentStatus.CONFIRMADA)
            .code(generateUniqueCode())
            .price(service.getPrice())
            .createdAt(LocalDateTime.now(clock))
            .build());

        String when = start.format(DAY_FORMAT) + " a las " + start.format(HOUR_FORMAT);
        notificationService.create(
            client, NotificationType.CITA_CONFIRMADA, "Cita confirmada",
            "Tu cita de " + service.getName() + " con " + chosen.getUser().getFullName()
                + " es el " + when + " (" + appointment.getCode() + ").");
        sendConfirmationAfterCommit(client, appointment, chosen, service, when);

        return new AppointmentResponse(
            appointment.getId(),
            appointment.getCode(),
            appointment.getStatus(),
            service.getId(),
            service.getName(),
            service.getDurationMinutes(),
            service.getPrice(),
            chosen.getId(),
            chosen.getUser().getFullName(),
            start.toLocalDate(),
            start.format(HOUR_FORMAT),
            end.format(HOUR_FORMAT),
            "¡Cita confirmada con éxito! Te esperamos el " + when + "."
        );
    }

    // The email goes out only if the booking actually committed, and a mail failure never
    // undoes a confirmed appointment.
    private void sendConfirmationAfterCommit(
        User client, Appointment appointment, Stylist stylist, BarberService service, String when
    ) {
        Runnable send = () -> {
            try {
                emailService.sendAppointmentConfirmation(
                    client.getEmail(), client.getFullName(), appointment.getCode(), service.getName(),
                    stylist.getUser().getFullName(), when, service.getPrice(), businessAddress);
            } catch (RuntimeException e) {
                log.warn("No se pudo enviar el correo de la cita {}: {}", appointment.getCode(), e.getMessage());
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send.run();
                }
            });
        } else {
            send.run();
        }
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder("#BB-");
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            }
            String code = sb.toString();
            if (!appointmentRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("No se pudo generar un código de reserva único.");
    }
}
