package com.udea.barberbook.barberbook_backend.home.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udea.barberbook.barberbook_backend.booking.domain.Appointment;
import com.udea.barberbook.barberbook_backend.booking.domain.AppointmentStatus;
import com.udea.barberbook.barberbook_backend.booking.repository.AppointmentRepository;
import com.udea.barberbook.barberbook_backend.catalog.domain.BarberService;
import com.udea.barberbook.barberbook_backend.catalog.domain.ServiceStatus;
import com.udea.barberbook.barberbook_backend.catalog.dto.ServiceResponse;
import com.udea.barberbook.barberbook_backend.home.dto.RecommendationCard;
import com.udea.barberbook.barberbook_backend.home.dto.RecommendationsResponse;
import com.udea.barberbook.barberbook_backend.stylist.domain.Stylist;
import com.udea.barberbook.barberbook_backend.user.domain.User;
import com.udea.barberbook.barberbook_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private static final int MAX_POPULAR = 20;

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Value("${app.home.recommendation-min-bookings}")
    private int minBookings;

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> popularServices(int limit) {
        int size = Math.max(1, Math.min(limit, MAX_POPULAR));
        return appointmentRepository
            .findMostBooked(AppointmentStatus.CANCELADA, ServiceStatus.ACTIVO, PageRequest.of(0, size)).stream()
            .map(s -> new ServiceResponse(
                s.getId(), s.getName(), s.getCategory().getLabel(),
                s.getDurationMinutes(), s.getPrice(), s.getStatus()))
            .toList();
    }

    // "Recomendado para ti": once the client has at least minBookings (non-cancelled)
    // bookings, suggest (1) their habitual service with the stylist they usually pick for
    // it and (2) their usual stylist with the service they book most with them. Only pairs
    // that can be booked today (active service, active stylist who offers it) are suggested.
    // Rating-based labels ("Porque te gustó antes") need a reviews feature that doesn't exist yet.
    @Override
    @Transactional(readOnly = true)
    public RecommendationsResponse recommendations(String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));
        List<Appointment> mine = appointmentRepository.findByClientIdAndStatusNot(
            client.getId(), AppointmentStatus.CANCELADA);

        if (mine.size() < minBookings) {
            return new RecommendationsResponse(false, mine.size(), minBookings, List.of());
        }

        List<Appointment> bookable = mine.stream().filter(this::isBookable).toList();
        List<RecommendationCard> cards = new ArrayList<>();

        Optional<BarberService> favoriteService = mostFrequent(bookable, a -> a.getService());
        favoriteService.ifPresent(service -> {
            List<Appointment> withService = bookable.stream()
                .filter(a -> a.getService().getId().equals(service.getId())).toList();
            mostFrequent(withService, a -> a.getStylist()).ifPresent(stylist ->
                cards.add(card(service, stylist, "Tu favorito habitual")));
        });

        mostFrequent(bookable, a -> a.getStylist()).ifPresent(stylist -> {
            List<Appointment> withStylist = bookable.stream()
                .filter(a -> a.getStylist().getId().equals(stylist.getId())).toList();
            Optional<BarberService> service = mostFrequent(
                withStylist.stream().filter(a -> !alreadySuggested(cards, a)).toList(), a -> a.getService());
            service.ifPresent(s -> cards.add(card(s, stylist, "Con " + firstName(stylist) + ", tu estilista")));
        });

        return new RecommendationsResponse(true, mine.size(), minBookings, cards);
    }

    private boolean isBookable(Appointment a) {
        Stylist stylist = a.getStylist();
        BarberService service = a.getService();
        return service.getStatus() == ServiceStatus.ACTIVO
            && stylist.isActive()
            && stylist.getServices().stream().anyMatch(s -> s.getId().equals(service.getId()));
    }

    private boolean alreadySuggested(List<RecommendationCard> cards, Appointment a) {
        return cards.stream().anyMatch(c ->
            c.serviceId().equals(a.getService().getId()) && c.stylistId().equals(a.getStylist().getId()));
    }

    // Most repeated key among the appointments; ties broken by id so the result is stable.
    private <T> Optional<T> mostFrequent(List<Appointment> appointments, Function<Appointment, T> key) {
        Map<UUID, Long> counts = appointments.stream()
            .collect(Collectors.groupingBy(a -> idOf(key.apply(a)), Collectors.counting()));
        return appointments.stream()
            .map(key)
            .distinct()
            .max(Comparator.<T>comparingLong(t -> counts.get(idOf(t))).thenComparing(t -> idOf(t)));
    }

    private UUID idOf(Object entity) {
        return entity instanceof BarberService s ? s.getId() : ((Stylist) entity).getId();
    }

    private RecommendationCard card(BarberService service, Stylist stylist, String label) {
        return new RecommendationCard(
            service.getId(), service.getName(), service.getDurationMinutes(), service.getPrice(),
            stylist.getId(), stylist.getUser().getFullName(), label);
    }

    private String firstName(Stylist stylist) {
        return stylist.getUser().getFullName().trim().split("\\s+")[0];
    }
}
