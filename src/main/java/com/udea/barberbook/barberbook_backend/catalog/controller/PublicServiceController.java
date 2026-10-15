package com.udea.barberbook.barberbook_backend.catalog.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.udea.barberbook.barberbook_backend.catalog.dto.ServiceResponse;
import com.udea.barberbook.barberbook_backend.catalog.service.CatalogService;

import lombok.RequiredArgsConstructor;

// Public catalog (no login): only ACTIVO services, as the home page is also
// visible to visitors.
@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class PublicServiceController {

    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> listActive() {
        return ResponseEntity.ok(catalogService.listActive());
    }
}
