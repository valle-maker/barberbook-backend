package com.udea.barberbook.barberbook_backend.booking.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.udea.barberbook.barberbook_backend.booking.dto.AppointmentResponse;
import com.udea.barberbook.barberbook_backend.booking.dto.CreateAppointmentRequest;
import com.udea.barberbook.barberbook_backend.booking.dto.MyAppointmentResponse;
import com.udea.barberbook.barberbook_backend.booking.dto.RescheduleAppointmentRequest;
import com.udea.barberbook.barberbook_backend.booking.service.BookingService;
import com.udea.barberbook.barberbook_backend.booking.service.ManageAppointmentService;
import com.udea.barberbook.barberbook_backend.booking.service.MyAppointmentService;
import com.udea.barberbook.barberbook_backend.common.exception.InvalidRequestException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final BookingService bookingService;
    private final MyAppointmentService myAppointmentService;
    private final ManageAppointmentService manageAppointmentService;

    // "Mis reservas" (HU-14): ?scope=upcoming (default) or ?scope=history. Only the caller's own.
    @GetMapping
    public ResponseEntity<List<MyAppointmentResponse>> mine(
        Authentication authentication, @RequestParam(defaultValue = "upcoming") String scope
    ) {
        MyAppointmentService.Scope parsed = switch (scope.toLowerCase()) {
            case "upcoming" -> MyAppointmentService.Scope.UPCOMING;
            case "history" -> MyAppointmentService.Scope.HISTORY;
            default -> throw new InvalidRequestException("El parámetro 'scope' debe ser 'upcoming' o 'history'.");
        };
        return ResponseEntity.ok(myAppointmentService.list(authentication.getName(), parsed));
    }

    // Detail of one of the caller's own appointments; anything else gets the generic 403 (HS-02).
    @GetMapping("/{id}")
    public ResponseEntity<MyAppointmentResponse> detail(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(myAppointmentService.get(authentication.getName(), id));
    }

    // HU-15: cancel one of the caller's own active appointments (frees the slot right away).
    @PostMapping("/{id}/cancel")
    public ResponseEntity<MyAppointmentResponse> cancel(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(manageAppointmentService.cancel(authentication.getName(), id));
    }

    // HU-15: move the appointment to another free slot of the same stylist (same appointment, no
    // new one is created).
    @PatchMapping("/{id}")
    public ResponseEntity<MyAppointmentResponse> reschedule(
        Authentication authentication, @PathVariable UUID id,
        @Valid @RequestBody RescheduleAppointmentRequest request
    ) {
        return ResponseEntity.ok(manageAppointmentService.reschedule(authentication.getName(), id, request));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> confirm(
        Authentication authentication, @Valid @RequestBody CreateAppointmentRequest request
    ) {
        // The JWT subject is the user's email (see JwtService).
        AppointmentResponse response = bookingService.confirm(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
