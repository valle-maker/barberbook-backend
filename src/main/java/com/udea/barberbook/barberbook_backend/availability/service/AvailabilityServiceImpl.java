package com.udea.barberbook.barberbook_backend.availability.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udea.barberbook.barberbook_backend.availability.dto.AvailabilityResponse;
import com.udea.barberbook.barberbook_backend.availability.dto.TimeSlotResponse;
import com.udea.barberbook.barberbook_backend.booking.domain.Appointment;
import com.udea.barberbook.barberbook_backend.booking.domain.AppointmentStatus;
import com.udea.barberbook.barberbook_backend.booking.repository.AppointmentRepository;
import com.udea.barberbook.barberbook_backend.catalog.domain.BarberService;
import com.udea.barberbook.barberbook_backend.catalog.domain.ServiceStatus;
import com.udea.barberbook.barberbook_backend.catalog.repository.BarberServiceRepository;
import com.udea.barberbook.barberbook_backend.common.exception.InvalidAvailabilityQueryException;
import com.udea.barberbook.barberbook_backend.common.exception.ServiceNotFoundException;
import com.udea.barberbook.barberbook_backend.common.exception.StylistNotFoundException;
import com.udea.barberbook.barberbook_backend.stylist.domain.Stylist;
import com.udea.barberbook.barberbook_backend.stylist.domain.WeeklySchedule;
import com.udea.barberbook.barberbook_backend.stylist.repository.StylistRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final String ANY_STYLIST = "any";
    private static final String NO_SLOTS_MESSAGE =
        "No hay turnos disponibles para esta fecha. Intenta con otro barbero o con otro día.";

    private final BarberServiceRepository serviceRepository;
    private final StylistRepository stylistRepository;
    private final AppointmentRepository appointmentRepository;
    private final Clock clock;

    @Value("${app.business.close-time}")
    private String closeTime;

    @Value("${app.availability.slot-step-minutes}")
    private int slotStepMinutes;

    @Value("${app.availability.min-advance-minutes}")
    private int minAdvanceMinutes;

    @Override
    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(String stylistParam, UUID serviceId, LocalDate date) {
        BarberService service = serviceRepository.findById(serviceId)
            .filter(s -> s.getStatus() == ServiceStatus.ACTIVO)
            .orElseThrow(() -> new ServiceNotFoundException("No se encontró el servicio solicitado."));

        List<Stylist> candidates = resolveCandidates(stylistParam, service);

        TreeMap<LocalTime, List<UUID>> freeSlots = new TreeMap<>();
        LocalDateTime now = LocalDateTime.now(clock);
        if (!date.isBefore(now.toLocalDate()) && !candidates.isEmpty()) {
            computeSlots(candidates, service, date, now, freeSlots);
        }

        List<TimeSlotResponse> slots = freeSlots.entrySet().stream()
            .map(e -> new TimeSlotResponse(
                e.getKey().toString(),
                e.getValue().stream().sorted().toList()))
            .toList();

        return new AvailabilityResponse(
            date,
            stylistParam.toLowerCase(),
            serviceId,
            service.getDurationMinutes(),
            slots,
            slots.isEmpty() ? NO_SLOTS_MESSAGE : null
        );
    }

    private List<Stylist> resolveCandidates(String stylistParam, BarberService service) {
        if (ANY_STYLIST.equalsIgnoreCase(stylistParam)) {
            return stylistRepository.findByActiveTrueAndServices_Id(service.getId());
        }

        UUID stylistId;
        try {
            stylistId = UUID.fromString(stylistParam);
        } catch (IllegalArgumentException e) {
            throw new InvalidAvailabilityQueryException("Valor inválido para el parámetro 'stylistId'.");
        }

        Stylist stylist = stylistRepository.findById(stylistId)
            .orElseThrow(() -> new StylistNotFoundException("No se encontró el estilista solicitado."));
        boolean offersService = stylist.isActive()
            && stylist.getServices().stream().anyMatch(s -> s.getId().equals(service.getId()));
        if (!offersService) {
            throw new InvalidAvailabilityQueryException("El estilista seleccionado no ofrece este servicio.");
        }
        return List.of(stylist);
    }

    private void computeSlots(
        List<Stylist> stylists, BarberService service, LocalDate date, LocalDateTime now,
        TreeMap<LocalTime, List<UUID>> freeSlots
    ) {
        int duration = service.getDurationMinutes();
        int closeMinute = LocalTime.parse(closeTime).toSecondOfDay() / 60;
        LocalDateTime earliestStart = now.plusMinutes(minAdvanceMinutes);

        List<UUID> stylistIds = stylists.stream().map(Stylist::getId).toList();
        Map<UUID, List<Appointment>> busyByStylist = appointmentRepository
            .findOverlapping(stylistIds, AppointmentStatus.CONFIRMADA, date.atStartOfDay(), date.plusDays(1).atStartOfDay())
            .stream()
            .collect(Collectors.groupingBy(a -> a.getStylist().getId(), HashMap::new, Collectors.toList()));

        for (Stylist stylist : stylists) {
            List<Appointment> busy = busyByStylist.getOrDefault(stylist.getId(), new ArrayList<>());

            for (WeeklySchedule shift : stylist.getSchedule()) {
                if (shift.getDayOfWeek() != date.getDayOfWeek()) {
                    continue;
                }

                // The service must end by the local closing time and by the end of the shift.
                int shiftEnd = Math.min(shift.getEndTime().toSecondOfDay() / 60, closeMinute);
                int shiftStart = shift.getStartTime().toSecondOfDay() / 60;
                int firstStart = ((shiftStart + slotStepMinutes - 1) / slotStepMinutes) * slotStepMinutes;

                for (int start = firstStart; start + duration <= shiftEnd; start += slotStepMinutes) {
                    LocalDateTime slotStart = date.atTime(start / 60, start % 60);
                    LocalDateTime slotEnd = slotStart.plusMinutes(duration);

                    if (slotStart.isBefore(earliestStart)) {
                        continue;
                    }
                    if (overlapsBreak(shift, start, start + duration)) {
                        continue;
                    }
                    if (busy.stream().anyMatch(a -> slotStart.isBefore(a.getEndAt()) && slotEnd.isAfter(a.getStartAt()))) {
                        continue;
                    }
                    freeSlots.computeIfAbsent(slotStart.toLocalTime(), k -> new ArrayList<>()).add(stylist.getId());
                }
            }
        }
    }

    private boolean overlapsBreak(WeeklySchedule shift, int startMinute, int endMinute) {
        if (shift.getBreakStart() == null || shift.getBreakEnd() == null) {
            return false;
        }
        int breakStart = shift.getBreakStart().toSecondOfDay() / 60;
        int breakEnd = shift.getBreakEnd().toSecondOfDay() / 60;
        return startMinute < breakEnd && endMinute > breakStart;
    }
}
