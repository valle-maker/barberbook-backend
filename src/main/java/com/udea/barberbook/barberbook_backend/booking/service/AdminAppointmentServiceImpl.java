package com.udea.barberbook.barberbook_backend.booking.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.udea.barberbook.barberbook_backend.availability.service.AvailabilityService;
import com.udea.barberbook.barberbook_backend.booking.domain.Appointment;
import com.udea.barberbook.barberbook_backend.booking.domain.AppointmentStatus;
import com.udea.barberbook.barberbook_backend.booking.dto.AdminAppointmentResponse;
import com.udea.barberbook.barberbook_backend.booking.dto.AppointmentState;
import com.udea.barberbook.barberbook_backend.booking.dto.AvailableStylistResponse;
import com.udea.barberbook.barberbook_backend.booking.repository.AppointmentRepository;
import com.udea.barberbook.barberbook_backend.common.email.EmailService;
import com.udea.barberbook.barberbook_backend.common.exception.AppointmentChangeNotAllowedException;
import com.udea.barberbook.barberbook_backend.common.exception.AppointmentNotFoundException;
import com.udea.barberbook.barberbook_backend.common.exception.InvalidRequestException;
import com.udea.barberbook.barberbook_backend.notification.domain.NotificationType;
import com.udea.barberbook.barberbook_backend.notification.service.NotificationService;
import com.udea.barberbook.barberbook_backend.stylist.domain.Stylist;
import com.udea.barberbook.barberbook_backend.stylist.repository.StylistRepository;
import com.udea.barberbook.barberbook_backend.user.domain.User;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAppointmentServiceImpl implements AdminAppointmentService {

    private static final int DEFAULT_WINDOW_DAYS = 60;
    private static final DateTimeFormatter DAY_FORMAT =
        DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-CO"));
    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final AppointmentRepository appointmentRepository;
    private final StylistRepository stylistRepository;
    private final AvailabilityService availabilityService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final EntityManager entityManager;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<AdminAppointmentResponse> list(LocalDate date, UUID stylistId) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime from = date != null ? date.atStartOfDay() : now;
        LocalDateTime to = date != null ? date.plusDays(1).atStartOfDay() : now.plusDays(DEFAULT_WINDOW_DAYS);
        return appointmentRepository.findActiveStartingBetween(AppointmentStatus.CONFIRMADA, now, from, to).stream()
            .filter(a -> stylistId == null || a.getStylist().getId().equals(stylistId))
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableStylistResponse> availableStylists(UUID appointmentId) {
        Appointment appointment = requireActive(find(appointmentId));
        return stylistRepository.findByActiveTrueAndServices_Id(appointment.getService().getId()).stream()
            .filter(s -> !s.getId().equals(appointment.getStylist().getId()))
            .filter(s -> availabilityService.isStylistFreeForReassignment(
                s, appointment.getService(), appointment.getStartAt()))
            .map(s -> new AvailableStylistResponse(s.getId(), s.getUser().getFullName(), s.getSpecialty()))
            .toList();
    }

    @Override
    @Transactional
    public AdminAppointmentResponse cancelForContingency(UUID appointmentId, String reason) {
        Appointment appointment = lockActive(appointmentId, List.of());

        appointment.setStatus(AppointmentStatus.CANCELADA);
        appointmentRepository.save(appointment);

        User client = appointment.getClient();
        if (client != null) {
            String when = whenText(appointment.getStartAt());
            String serviceName = appointment.getService().getName();
            String stylistName = appointment.getStylist().getUser().getFullName();
            notificationService.create(
                client, NotificationType.CITA_CANCELADA_POR_LOCAL, "Tuvimos que cancelar tu cita",
                "Por un imprevisto cancelamos tu cita de " + serviceName + " del " + when + " ("
                    + appointment.getCode() + "). Te pedimos disculpas: puedes reagendar cuando quieras.");
            afterCommit(() -> emailService.sendAppointmentCancelledByShop(
                client.getEmail(), client.getFullName(), appointment.getCode(), serviceName, stylistName, when,
                reason));
        }
        return toResponse(appointment);
    }

    @Override
    @Transactional
    public AdminAppointmentResponse reassign(UUID appointmentId, UUID newStylistId) {
        Appointment appointment = lockActive(appointmentId, List.of(newStylistId));

        Stylist previous = appointment.getStylist();
        if (previous.getId().equals(newStylistId)) {
            throw new InvalidRequestException("La cita ya está asignada a ese estilista.");
        }
        Stylist next = stylistRepository.findById(newStylistId)
            .filter(Stylist::isActive)
            .filter(s -> s.getServices().stream().anyMatch(sv -> sv.getId().equals(appointment.getService().getId())))
            .orElseThrow(() -> new AppointmentChangeNotAllowedException(
                "Ese estilista no está disponible para este servicio.", "ESTILISTA_NO_DISPONIBLE"));
        if (!availabilityService.isStylistFreeForReassignment(next, appointment.getService(), appointment.getStartAt())) {
            throw new AppointmentChangeNotAllowedException(
                "Ese estilista no está libre en ese horario. Elige otro.", "ESTILISTA_NO_DISPONIBLE");
        }

        String previousName = previous.getUser().getFullName();
        appointment.setStylist(next);
        appointmentRepository.save(appointment);

        User client = appointment.getClient();
        if (client != null) {
            String when = whenText(appointment.getStartAt());
            String serviceName = appointment.getService().getName();
            String nextName = next.getUser().getFullName();
            notificationService.create(
                client, NotificationType.CITA_REASIGNADA, "Cambió el estilista de tu cita",
                "Tu cita de " + serviceName + " del " + when + " (" + appointment.getCode() + ") ahora será con "
                    + nextName + ". La hora no cambia.");
            afterCommit(() -> emailService.sendAppointmentReassigned(
                client.getEmail(), client.getFullName(), appointment.getCode(), serviceName, previousName,
                nextName, when));
        }
        return toResponse(appointment);
    }

    private Appointment find(UUID id) {
        return appointmentRepository.findById(id).orElseThrow(AppointmentNotFoundException::new);
    }

    // Locks the stylist rows involved (the same lock the booking flow uses, always in id order),
    // re-reads the appointment and checks it is still active.
    private Appointment lockActive(UUID appointmentId, List<UUID> extraStylistIds) {
        Appointment found = find(appointmentId);
        List<UUID> ids = new java.util.ArrayList<>(extraStylistIds);
        ids.add(found.getStylist().getId());
        stylistRepository.lockAllByIdOrdered(ids);
        entityManager.refresh(found);
        return requireActive(found);
    }

    private Appointment requireActive(Appointment a) {
        if (a.getStatus() != AppointmentStatus.CONFIRMADA || !a.getEndAt().isAfter(LocalDateTime.now(clock))) {
            throw new AppointmentChangeNotAllowedException(
                "Esta cita ya no está activa, por lo que no se puede modificar.", "CITA_NO_ACTIVA");
        }
        return a;
    }

    private AdminAppointmentResponse toResponse(Appointment a) {
        User client = a.getClient();
        AppointmentState state = a.getStatus() == AppointmentStatus.CANCELADA
            ? AppointmentState.CANCELADA
            : a.getEndAt().isAfter(LocalDateTime.now(clock)) ? AppointmentState.CONFIRMADA : AppointmentState.COMPLETADA;
        return new AdminAppointmentResponse(
            a.getId(),
            a.getCode(),
            state,
            client != null ? client.getFullName() : "Sin cliente",
            client != null ? client.getEmail() : null,
            client != null ? client.getPhone() : null,
            a.getService().getId(),
            a.getService().getName(),
            a.getService().getDurationMinutes(),
            a.getPrice() != null ? a.getPrice() : a.getService().getPrice(),
            a.getStylist().getId(),
            a.getStylist().getUser().getFullName(),
            a.getStartAt().toLocalDate(),
            a.getStartAt().format(HOUR_FORMAT),
            a.getEndAt().format(HOUR_FORMAT)
        );
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
