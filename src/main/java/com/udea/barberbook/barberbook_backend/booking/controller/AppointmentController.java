package com.udea.barberbook.barberbook_backend.booking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.udea.barberbook.barberbook_backend.booking.dto.AppointmentResponse;
import com.udea.barberbook.barberbook_backend.booking.dto.CreateAppointmentRequest;
import com.udea.barberbook.barberbook_backend.booking.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> confirm(
        Authentication authentication, @Valid @RequestBody CreateAppointmentRequest request
    ) {
        // The JWT subject is the user's email (see JwtService).
        AppointmentResponse response = bookingService.confirm(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
