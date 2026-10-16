package com.udea.barberbook.barberbook_backend.availability.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.udea.barberbook.barberbook_backend.availability.dto.AvailabilityResponse;
import com.udea.barberbook.barberbook_backend.availability.dto.DayAvailabilityResponse;
import com.udea.barberbook.barberbook_backend.availability.service.AvailabilityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<AvailabilityResponse> get(
        @RequestParam String stylistId,
        @RequestParam UUID serviceId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(availabilityService.getAvailability(stylistId, serviceId, date));
    }

    // Calendar view: which days of the month have at least one free slot.
    // month is 1-12 (the front's JS months are 0-11: send month + 1).
    @GetMapping("/month")
    public ResponseEntity<List<DayAvailabilityResponse>> getMonth(
        @RequestParam String stylistId,
        @RequestParam UUID serviceId,
        @RequestParam int year,
        @RequestParam int month
    ) {
        return ResponseEntity.ok(availabilityService.getMonthAvailability(stylistId, serviceId, year, month));
    }
}
