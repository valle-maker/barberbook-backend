package com.udea.barberbook.barberbook_backend.home.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.udea.barberbook.barberbook_backend.catalog.dto.ServiceResponse;
import com.udea.barberbook.barberbook_backend.home.dto.RecommendationsResponse;
import com.udea.barberbook.barberbook_backend.home.service.HomeService;

import lombok.RequiredArgsConstructor;

// Data sections of the client home screen (HU-06). Stylists live in /api/v1/stylists,
// notifications in /api/v1/notifications, the header in /api/v1/users/me.
@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    // Public: the "Servicios populares" carousel.
    @GetMapping("/popular-services")
    public ResponseEntity<List<ServiceResponse>> popular(@RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(homeService.popularServices(limit));
    }

    // Clients only: "Recomendado para ti".
    @GetMapping("/recommendations")
    public ResponseEntity<RecommendationsResponse> recommendations(Authentication authentication) {
        return ResponseEntity.ok(homeService.recommendations(authentication.getName()));
    }
}
