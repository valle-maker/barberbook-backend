package com.udea.barberbook.barberbook_backend.catalog.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.udea.barberbook.barberbook_backend.catalog.domain.ServiceStatus;
import com.udea.barberbook.barberbook_backend.catalog.dto.ServiceRequest;
import com.udea.barberbook.barberbook_backend.catalog.dto.ServiceResponse;
import com.udea.barberbook.barberbook_backend.catalog.dto.ServiceStatusRequest;
import com.udea.barberbook.barberbook_backend.catalog.service.CatalogService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// ADMIN only — enforced by the /api/v1/admin/** rule in SecurityConfig.
@RestController
@RequestMapping("/api/v1/admin/services")
@RequiredArgsConstructor
public class AdminServiceController {

    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> list(@RequestParam(required = false) ServiceStatus status) {
        return ResponseEntity.ok(catalogService.listAll(status));
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> update(@PathVariable UUID id, @Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.ok(catalogService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceResponse> changeStatus(
        @PathVariable UUID id,
        @Valid @RequestBody ServiceStatusRequest request
    ) {
        return ResponseEntity.ok(catalogService.changeStatus(id, request.status()));
    }
}
