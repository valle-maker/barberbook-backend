package com.udea.barberbook.barberbook_backend.audit.dto;

import java.time.Instant;
import java.util.UUID;

import com.udea.barberbook.barberbook_backend.audit.domain.AuditEventType;

public record AuditLogResponse(
    UUID id,
    Instant createdAt,
    AuditEventType eventType,
    String userEmail,
    String userRole,
    String httpMethod,
    String path,
    String ip,
    String detail
) {}
