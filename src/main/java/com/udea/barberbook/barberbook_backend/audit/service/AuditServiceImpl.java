package com.udea.barberbook.barberbook_backend.audit.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udea.barberbook.barberbook_backend.audit.domain.AuditEventType;
import com.udea.barberbook.barberbook_backend.audit.domain.AuditLog;
import com.udea.barberbook.barberbook_backend.audit.dto.AuditLogResponse;
import com.udea.barberbook.barberbook_backend.audit.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final Clock clock;

    @Override
    @Transactional
    public void recordAccessDenied(String userEmail, String userRole, String httpMethod, String path, String ip) {
        // Values come from the request: strip control characters (log injection) and cap lengths.
        String email = clean(userEmail, 255);
        String role = clean(userRole, 30);
        String method = clean(httpMethod, 10);
        String safePath = clean(path, 300);
        String safeIp = clean(ip, 64);

        // Always to the application log too, so the trail survives even if the DB write fails.
        log.warn("[AUDITORIA] ACCESO_DENEGADO usuario={} rol={} {} {} ip={}", email, role, method, safePath, safeIp);

        try {
            auditLogRepository.save(AuditLog.builder()
                .createdAt(Instant.now(clock))
                .eventType(AuditEventType.ACCESO_DENEGADO)
                .userEmail(email)
                .userRole(role)
                .httpMethod(method)
                .path(safePath)
                .ip(safeIp)
                .detail("Intento de acceso a un recurso sin permisos suficientes.")
                .build());
        } catch (RuntimeException e) {
            log.error("[AUDITORIA] No se pudo guardar el registro de auditoría: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> latest(int limit) {
        int size = Math.max(1, Math.min(limit, 200));
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, size)).stream()
            .map(a -> new AuditLogResponse(
                a.getId(), a.getCreatedAt(), a.getEventType(), a.getUserEmail(), a.getUserRole(),
                a.getHttpMethod(), a.getPath(), a.getIp(), a.getDetail()))
            .toList();
    }

    private static String clean(String value, int max) {
        if (value == null) {
            return null;
        }
        String cleaned = value.replaceAll("\\p{Cntrl}", "_");
        return cleaned.length() > max ? cleaned.substring(0, max) : cleaned;
    }
}
