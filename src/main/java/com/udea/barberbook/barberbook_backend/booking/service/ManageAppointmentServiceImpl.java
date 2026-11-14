package com.udea.barberbook.barberbook_backend.booking.service;

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
import com.udea.barberbook.barberbook_backend.booking.dto.MyAppointmentResponse;
import com.udea.barberbook.barberbook_backend.booking.dto.RescheduleAppointmentRequest;
import com.udea.barberbook.barberbook_backend.booking.repository.AppointmentRepository;
import com.udea.barberbook.barberbook_backend.common.email.EmailService;
import com.udea.barberbook.barberbook_backend.common.exception.AppointmentAccessDeniedException;
import com.udea.barberbook.barberbook_backend.common.exception.AppointmentChangeNotAllowedException;
import com.udea.barberbook.barberbook_backend.common.exception.InvalidRequestException;
import com.udea.barberbook.barberbook_backend.common.exception.SlotUnavailableException;
import com.udea.barberbook.barberbook_backend.notification.domain.NotificationType;
import com.udea.barberbook.barberbook_backend.notification.service.NotificationService;
import com.udea.barberbook.barberbook_backend.stylist.domain.Stylist;
import com.udea.barberbook.barberbook_backend.stylist.repository.StylistRepository;
import com.udea.barberbook.barberbook_backend.user.domain.User;
import com.udea.barberbook.barberbook_backend.user.repository.UserRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageAppointmentServiceImpl implements ManageAppointmentService {

    private static final DateTimeFormatter DAY_FORMAT =
        DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-CO"));
    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final AppointmentRepository appointmentRepository;
    private final StylistRepository stylistRepository;
    private final UserRepository userRepository;
    private final AvailabilityService availabilityService;
    private final MyAppointmentService myAppointmentService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final EntityManager entityManager;
    private final Clock clock;

    @Value("${app.booking.min-notice-hours}")
    private int minNoticeHours;

    @Override
    @Transactional
    public MyAppointmentResponse cancel(String clientEmail, UUID appointmentId) {
        User client = findClient(clientEmail);
        Appointment appointment = lockOwnActive(client, appointmentId);

        appointment.setStatus(AppointmentStatus.CANCELADA);
        appointmentRepository.save(appointment);

        String when = whenText(appointment.getStartAt());
        notificationService.create(
            client, NotificationType.CITA_CANCELADA, "Cita cancelada",
            "Cancelaste tu cita de " + appointment.getService().getName() + " del " + when
                + " (" + appointment.getCode() + ").");
        afterCommit(() -> emailService.sendAppointmentCancelled(
            client.getEmail(), client.getFullName(), appointment.getCode(),
            appointment.getService().getName(), appointment.getStylist().getUser().getFullName(), when));

        return myAppointmentService.get(clientEmail, appointmentId);
    }

    @Override
    @Transactional
    public MyAppointmentResponse reschedule(String clientEmail, UUID appointmentId, RescheduleAppointmentRequest request) {
        User client = findClient(clientEmail);
        Appointment appointment = lockOwnActive(client, appointmentId);

        LocalDateTime newStart = request.date().atTime(LocalTime.parse(request.time()));
        if (newStart.equals(appointment.getStartAt())) {
            throw new InvalidRequestException("Elige una fecha u hora distinta a la de tu cita actual.");
        }
        // The stylist row is already locked by lockOwnActive, so the free check below cannot race
        // with another booking for the same stylist.
        Stylist stylist = appointment.getStylist();
        if (!availabilityService.isStylistFreeAt(stylist, appointment.getService(), newStart)) {
            throw new SlotUnavailableException(BookingServiceImpl.SLOT_TAKEN_MESSAGE);
        }

        String oldWhen = whenText(appointment.getStartAt());
        appointment.setStartAt(newStart);
        appointment.setEndAt(newStart.plusMinutes(appointment.getService().getDurationMinutes()));
        appointmentRepository.save(appointment);

        String when = whenText(newStart);
        notificationService.create(
            client, NotificationType.CITA_REPROGRAMADA, "Cita reprogramada",
            "Tu cita de " + appointment.getService().getName() + " pasó del " + oldWhen + " al " + when
                + " (" + appointment.getCode() + ").");
        afterCommit(() -> emailService.sendAppointmentRescheduled(
            client.getEmail(), client.getFullName(), appointment.getCode(),
            appointment.getService().getName(), stylist.getUser().getFullName(), when));

        return myAppointmentService.get(clientEmail, appointmentId);
    }

    // Ownership first (HS-02): a foreign or nonexistent id gets the generic 403 and nothing about
    // the appointment is read further. Then the stylist row is locked (the same lock the booking
    // flow uses), the appointment is re-read, and the business rules run on the fresh row.
    private Appointment lockOwnActive(User client, UUID appointmentId) {
        Appointment found = appointmentRepository.findById(appointmentId)
            .filter(a -> a.getClient() != null && a.getClient().getId().equals(client.getId()))
            .orElseThrow(AppointmentAccessDeniedException::new);

        stylistRepository.lockAllByIdOrdered(List.of(found.getStylist().getId()));
        entityManager.refresh(found);

        LocalDateTime now = LocalDateTime.now(clock);
        if (found.getStatus() != AppointmentStatus.CONFIRMADA || !found.getEndAt().isAfter(now)) {
            throw new AppointmentChangeNotAllowedException(
                "Esta cita ya no está activa, por lo que no se puede modificar.", "CITA_NO_ACTIVA");
        }
        if (found.getStartAt().isBefore(now.plusHours(minNoticeHours))) {
            throw new AppointmentChangeNotAllowedException(
                "Las modificaciones solo están permitidas con al menos " + minNoticeHours
                    + " horas de anticipación. Por favor comunícate por teléfono.",
                "CAMBIO_FUERA_DE_PLAZO");
        }
        return found;
    }

    private User findClient(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));
    }

    private static String whenText(LocalDateTime start) {
        return start.format(DAY_FORMAT) + " a las " + start.format(HOUR_FORMAT);
    }

    // A mail failure never undoes the change, and nothing is sent if the transaction rolls back.
    private void afterCommit(Runnable send) {
        Runnable safe = () -> {
            try {
                send.run();
            } catch (RuntimeException e) {
                log.warn("No se pudo enviar el correo de la cita: {}", e.getMessage());
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    safe.run();
                }
            });
        } else {
            safe.run();
        }
    }
}
