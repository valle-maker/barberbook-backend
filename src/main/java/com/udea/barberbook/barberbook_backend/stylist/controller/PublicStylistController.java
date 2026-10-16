package com.udea.barberbook.barberbook_backend.stylist.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.udea.barberbook.barberbook_backend.stylist.dto.PublicStylistResponse;
import com.udea.barberbook.barberbook_backend.stylist.service.StylistService;

import lombok.RequiredArgsConstructor;

// Stylist directory for the client app ("Nuestros estilistas" on the home screen and the
// stylist step of the booking wizard, filtered by ?serviceId=). No login needed, no private data.
@RestController
@RequestMapping("/api/v1/stylists")
@RequiredArgsConstructor
public class PublicStylistController {

    private final StylistService stylistService;

    @GetMapping
    public ResponseEntity<List<PublicStylistResponse>> list(@RequestParam(required = false) UUID serviceId) {
        return ResponseEntity.ok(stylistService.listActive(serviceId));
    }
}
