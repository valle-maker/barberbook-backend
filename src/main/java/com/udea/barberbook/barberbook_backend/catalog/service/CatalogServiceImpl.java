package com.udea.barberbook.barberbook_backend.catalog.service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udea.barberbook.barberbook_backend.catalog.domain.BarberService;
import com.udea.barberbook.barberbook_backend.catalog.domain.ServiceCategory;
import com.udea.barberbook.barberbook_backend.catalog.domain.ServiceStatus;
import com.udea.barberbook.barberbook_backend.catalog.dto.ServiceRequest;
import com.udea.barberbook.barberbook_backend.catalog.dto.ServiceResponse;
import com.udea.barberbook.barberbook_backend.catalog.repository.BarberServiceRepository;
import com.udea.barberbook.barberbook_backend.common.exception.ServiceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final BarberServiceRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> listActive() {
        return repository.findByStatusOrderByNameAsc(ServiceStatus.ACTIVO).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> searchActive(String query) {
        if (query == null || query.isBlank()) {
            return listActive();
        }
        String[] words = normalize(query).split("\\s+");
        return repository.findByStatusOrderByNameAsc(ServiceStatus.ACTIVO).stream()
            .filter(s -> {
                String haystack = normalize(s.getName() + " " + s.getCategory().getLabel());
                return Arrays.stream(words).allMatch(haystack::contains);
            })
            .map(this::toResponse)
            .toList();
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .trim();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> listAll(ServiceStatus statusFilter) {
        List<BarberService> services = statusFilter == null
            ? repository.findAllByOrderByNameAsc()
            : repository.findByStatusOrderByNameAsc(statusFilter);
        return services.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ServiceResponse create(ServiceRequest request) {
        BarberService service = BarberService.builder()
            .name(request.name().trim())
            .category(ServiceCategory.fromLabel(request.category()))
            .durationMinutes(request.duration())
            .price(request.price().longValueExact())
            .status(ServiceStatus.ACTIVO)
            .build();
        return toResponse(repository.save(service));
    }

    // A price change only affects future bookings: appointments will store the
    // price they were booked at (HU-13), so nothing to touch here.
    @Override
    @Transactional
    public ServiceResponse update(UUID id, ServiceRequest request) {
        BarberService service = findOrThrow(id);
        service.setName(request.name().trim());
        service.setCategory(ServiceCategory.fromLabel(request.category()));
        service.setDurationMinutes(request.duration());
        service.setPrice(request.price().longValueExact());
        return toResponse(repository.save(service));
    }

    @Override
    @Transactional
    public ServiceResponse changeStatus(UUID id, ServiceStatus status) {
        BarberService service = findOrThrow(id);
        service.setStatus(status);
        return toResponse(repository.save(service));
    }

    private BarberService findOrThrow(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new ServiceNotFoundException("No se encontró el servicio solicitado."));
    }

    private ServiceResponse toResponse(BarberService service) {
        return new ServiceResponse(
            service.getId(),
            service.getName(),
            service.getCategory().getLabel(),
            service.getDurationMinutes(),
            service.getPrice(),
            service.getStatus()
        );
    }
}
