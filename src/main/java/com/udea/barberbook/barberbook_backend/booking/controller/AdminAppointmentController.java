package com.udea.barberbook.barberbook_backend.booking.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.udea.barberbook.barberbook_backend.booking.dto.AdminAppointmentResponse;
import com.udea.barberbook.barberbook_backend.booking.dto.AdminCancelRequest;
import com.udea.barberbook.barberbook_backend.booking.dto.AvailableStylistResponse;
import com.udea.barberbook.barberbook_backend.booking.dto.ReassignAppointmentRequest;
import com.udea.barberbook.barberbook_backend.booking.service.AdminAppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// HU-20. Everything under /api/v1/admin/** is ADMIN-only (see SecurityConfig).
@RestController
@RequestMapping("/api/v1/admin/appointments")
@RequiredArgsConstructor
public class AdminAppointmentController {

    private final AdminAppointmentService adminAppointmentService;

    @GetMapping
    public ResponseEntity<List<AdminAppointmentResponse>> list(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(required = false) UUID stylistId
    ) {
        return ResponseEntity.ok(adminAppointmentService.list(date, stylistId));
    }

    @GetMapping("/{id}/available-stylists")
    public ResponseEntity<List<AvailableStylistResponse>> availableStylists(@PathVariable UUID id) {
        return ResponseEntity.ok(adminAppointmentService.availableStylists(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<AdminAppointmentResponse> cancel(
        @PathVariable UUID id, @Valid @RequestBody(required = false) AdminCancelRequest request
    ) {
        String reason = request == null ? null : request.reason();
        return ResponseEntity.ok(adminAppointmentService.cancelForContingency(id, reason));
    }

    @PatchMapping("/{id}/stylist")
    public ResponseEntity<AdminAppointmentResponse> reassign(
        @PathVariable UUID id, @Valid @RequestBody ReassignAppointmentRequest request
    ) {
        return ResponseEntity.ok(adminAppointmentService.reassign(id, request.stylistId()));
    }
}
