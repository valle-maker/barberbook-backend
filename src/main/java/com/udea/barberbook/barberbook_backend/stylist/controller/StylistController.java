package com.udea.barberbook.barberbook_backend.stylist.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.udea.barberbook.barberbook_backend.stylist.dto.AssignServicesRequest;
import com.udea.barberbook.barberbook_backend.stylist.dto.CreateStylistRequest;
import com.udea.barberbook.barberbook_backend.stylist.dto.StylistResponse;
import com.udea.barberbook.barberbook_backend.stylist.service.StylistService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/stylists")
@RequiredArgsConstructor
public class StylistController {

    private final StylistService stylistService;

    @PostMapping
    public ResponseEntity<StylistResponse> create(@Valid @RequestBody CreateStylistRequest request) {
        StylistResponse response = stylistService.createStylist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/services")
    public ResponseEntity<StylistResponse> assignServices(
        @PathVariable UUID id, @Valid @RequestBody AssignServicesRequest request
    ) {
        return ResponseEntity.ok(stylistService.assignServices(id, request.serviceIds()));
    }
}
