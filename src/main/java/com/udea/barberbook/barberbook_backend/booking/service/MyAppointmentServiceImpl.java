package com.udea.barberbook.barberbook_backend.booking.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udea.barberbook.barberbook_backend.booking.domain.Appointment;
import com.udea.barberbook.barberbook_backend.booking.domain.AppointmentStatus;
import com.udea.barberbook.barberbook_backend.booking.dto.AppointmentState;
import com.udea.barberbook.barberbook_backend.booking.dto.MyAppointmentResponse;
import com.udea.barberbook.barberbook_backend.booking.repository.AppointmentRepository;
import com.udea.barberbook.barberbook_backend.common.exception.AppointmentAccessDeniedException;
import com.udea.barberbook.barberbook_backend.user.domain.User;
import com.udea.barberbook.barberbook_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyAppointmentServiceImpl implements MyAppointmentService {

    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Value("${app.booking.min-notice-hours}")
    private int minNoticeHours;

    @Override
    @Transactional(readOnly = true)
    public List<MyAppointmentResponse> list(String clientEmail, Scope scope) {
        User client = findClient(clientEmail);
        LocalDateTime now = LocalDateTime.now(clock);
        List<Appointment> appointments = scope == Scope.UPCOMING
            ? appointmentRepository.findUpcoming(client.getId(), AppointmentStatus.CONFIRMADA, now)
            : appointmentRepository.findHistory(client.getId(), AppointmentStatus.CANCELADA, now);
        return appointments.stream().map(a -> toResponse(a, now)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MyAppointmentResponse get(String clientEmail, UUID appointmentId) {
        User client = findClient(clientEmail);
        // Ownership is checked in the same lookup, so a foreign appointment is never even mapped
        // to a response: it is treated exactly like one that doesn't exist.
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .filter(a -> a.getClient() != null && a.getClient().getId().equals(client.getId()))
            .orElseThrow(AppointmentAccessDeniedException::new);
        return toResponse(appointment, LocalDateTime.now(clock));
    }

    private User findClient(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));
    }

    private MyAppointmentResponse toResponse(Appointment a, LocalDateTime now) {
        AppointmentState state = stateOf(a, now);
        boolean canManage = state == AppointmentState.CONFIRMADA
            && !a.getStartAt().isBefore(now.plusHours(minNoticeHours));
        return new MyAppointmentResponse(
            a.getId(),
            a.getCode(),
            state,
            a.getService().getId(),
            a.getService().getName(),
            a.getService().getDurationMinutes(),
            a.getPrice() != null ? a.getPrice() : a.getService().getPrice(),
            a.getStylist().getId(),
            a.getStylist().getUser().getFullName(),
            a.getStartAt().toLocalDate(),
            a.getStartAt().format(HOUR_FORMAT),
            a.getEndAt().format(HOUR_FORMAT),
            canManage
        );
    }

    private AppointmentState stateOf(Appointment a, LocalDateTime now) {
        if (a.getStatus() == AppointmentStatus.CANCELADA) {
            return AppointmentState.CANCELADA;
        }
        return a.getEndAt().isAfter(now) ? AppointmentState.CONFIRMADA : AppointmentState.COMPLETADA;
    }
}
