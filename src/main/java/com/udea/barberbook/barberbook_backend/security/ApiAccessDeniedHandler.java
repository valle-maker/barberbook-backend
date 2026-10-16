package com.udea.barberbook.barberbook_backend.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.udea.barberbook.barberbook_backend.audit.service.AuditService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// HS-01 / HA-01: an authenticated user reached something their role doesn't allow.
// The request never gets to the controller; we answer 403 with the agreed message and
// leave a record in the audit trail. (A missing/invalid token is a 401, handled by
// JwtAuthenticationEntryPoint, not here.)
@Component
@RequiredArgsConstructor
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    static final String MESSAGE = "Acceso Denegado: Permisos Insuficientes";

    private final AuditService auditService;

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : null;
        String role = auth == null ? null : auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
            .findFirst()
            .orElse(null);

        auditService.recordAccessDenied(email, role, request.getMethod(), request.getRequestURI(), request.getRemoteAddr());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"" + MESSAGE + "\"}");
    }
}
