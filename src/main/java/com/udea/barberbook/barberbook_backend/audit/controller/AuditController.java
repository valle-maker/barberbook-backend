package com.udea.barberbook.barberbook_backend.audit.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.udea.barberbook.barberbook_backend.audit.dto.AuditLogResponse;
import com.udea.barberbook.barberbook_backend.audit.service.AuditService;

import lombok.RequiredArgsConstructor;

// Read-only view of the audit trail for administrators (/api/v1/admin/** is ADMIN-only).
@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> latest(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(auditService.latest(limit));
    }
}
