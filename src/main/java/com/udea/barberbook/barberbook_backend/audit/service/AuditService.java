package com.udea.barberbook.barberbook_backend.audit.service;

import java.util.List;

import com.udea.barberbook.barberbook_backend.audit.dto.AuditLogResponse;

public interface AuditService {

    // Records an authenticated user's attempt to reach something their role doesn't allow.
    // Never throws: a failure to write the audit row must not change the 403 the caller gets.
    void recordAccessDenied(String userEmail, String userRole, String httpMethod, String path, String ip);

    // Newest first; limit is clamped to 1..200.
    List<AuditLogResponse> latest(int limit);
}
