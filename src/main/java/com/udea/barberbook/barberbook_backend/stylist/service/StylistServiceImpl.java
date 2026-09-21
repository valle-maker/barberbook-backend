package com.udea.barberbook.barberbook_backend.stylist.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udea.barberbook.barberbook_backend.common.email.EmailService;
import com.udea.barberbook.barberbook_backend.common.exception.EmailAlreadyExistsException;
import com.udea.barberbook.barberbook_backend.common.exception.InvalidScheduleException;
import com.udea.barberbook.barberbook_backend.stylist.domain.Stylist;
import com.udea.barberbook.barberbook_backend.stylist.domain.WeeklySchedule;
import com.udea.barberbook.barberbook_backend.stylist.dto.CreateStylistRequest;
import com.udea.barberbook.barberbook_backend.stylist.dto.ScheduleEntryRequest;
import com.udea.barberbook.barberbook_backend.stylist.dto.ScheduleEntryResponse;
import com.udea.barberbook.barberbook_backend.stylist.dto.StylistResponse;
import com.udea.barberbook.barberbook_backend.stylist.repository.StylistRepository;
import com.udea.barberbook.barberbook_backend.user.domain.Role;
import com.udea.barberbook.barberbook_backend.user.domain.User;
import com.udea.barberbook.barberbook_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StylistServiceImpl implements StylistService {

    private final UserRepository userRepository;
    private final StylistRepository stylistRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional
    public StylistResponse createStylist(CreateStylistRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Este correo ya se encuentra registrado.");
        }

        validateSchedule(request.schedule());

        String temporaryPassword = generateTemporaryPassword();

        User user = User.builder()
            .fullName(request.fullName().trim())
            .phone(request.phone().trim())
            .email(email)
            .passwordHash(passwordEncoder.encode(temporaryPassword))
            .role(Role.ESTILISTA)
            .enabled(true)
            .failedLoginAttempts(0)
            .build();
        user = userRepository.save(user);

        Stylist stylist = Stylist.builder()
            .user(user)
            .specialty(request.specialty().trim())
            .active(true)
            .build();

        List<WeeklySchedule> scheduleEntities = request.schedule().stream()
            .map(entry -> WeeklySchedule.builder()
                .stylist(stylist)
                .dayOfWeek(entry.dayOfWeek())
                .startTime(entry.startTime())
                .endTime(entry.endTime())
                .breakStart(entry.breakStart())
                .breakEnd(entry.breakEnd())
                .build())
            .toList();
        stylist.setSchedule(scheduleEntities);

        Stylist saved = stylistRepository.save(stylist);

        emailService.sendTemporaryCredentials(user.getEmail(), user.getFullName(), temporaryPassword);

        return toResponse(saved);
    }

    private void validateSchedule(List<ScheduleEntryRequest> schedule) {
        for (ScheduleEntryRequest entry : schedule) {
            if (!entry.startTime().isBefore(entry.endTime())) {
                throw new InvalidScheduleException(
                    "La hora de inicio debe ser antes que la hora de fin (" + entry.dayOfWeek() + ").");
            }

            boolean hasBreakStart = entry.breakStart() != null;
            boolean hasBreakEnd = entry.breakEnd() != null;

            if (hasBreakStart != hasBreakEnd) {
                throw new InvalidScheduleException(
                    "El descanso debe tener hora de inicio y de fin (" + entry.dayOfWeek() + ").");
            }

            if (hasBreakStart) {
                if (!entry.breakStart().isBefore(entry.breakEnd())) {
                    throw new InvalidScheduleException(
                        "El descanso debe iniciar antes de terminar (" + entry.dayOfWeek() + ").");
                }
                if (entry.breakStart().isBefore(entry.startTime()) || entry.breakEnd().isAfter(entry.endTime())) {
                    throw new InvalidScheduleException(
                        "El descanso debe estar dentro del horario laboral (" + entry.dayOfWeek() + ").");
                }
            }
        }
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "!A1";
    }

    private StylistResponse toResponse(Stylist stylist) {
        List<ScheduleEntryResponse> scheduleResponse = stylist.getSchedule().stream()
            .map(entry -> new ScheduleEntryResponse(
                entry.getDayOfWeek(), entry.getStartTime(), entry.getEndTime(),
                entry.getBreakStart(), entry.getBreakEnd()))
            .toList();

        User user = stylist.getUser();
        return new StylistResponse(
            stylist.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getPhone(),
            stylist.getSpecialty(),
            user.getRole().name(),
            scheduleResponse,
            "Estilista registrado. Se enviaron las credenciales temporales por correo."
        );
    }
}
