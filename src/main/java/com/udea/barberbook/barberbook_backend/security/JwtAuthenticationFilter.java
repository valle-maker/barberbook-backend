package com.udea.barberbook.barberbook_backend.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String JTI_ATTRIBUTE = "jwt_jti";
    public static final String EXPIRATION_ATTRIBUTE = "jwt_exp";

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final TokenBlocklistService tokenBlocklistService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(AUTH_HEADER);
        log.warn("[JWT-DEBUG] {} {} — Authorization header present: {}, value: {}",
            request.getMethod(), request.getRequestURI(), header != null,
            header == null ? "null" : (header.length() > 25 ? header.substring(0, 25) + "..." : header));

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                Claims claims = jwtService.parseClaims(token);
                log.warn("[JWT-DEBUG] Token parsed OK — sub: {}, role: {}, jti: {}",
                    claims.getSubject(), claims.get("role", String.class), claims.getId());

                if (!tokenBlocklistService.isRevoked(claims.getId())) {
                    String role = claims.get("role", String.class);
                    var authentication = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    request.setAttribute(JTI_ATTRIBUTE, claims.getId());
                    request.setAttribute(EXPIRATION_ATTRIBUTE, claims.getExpiration().toInstant());
                } else {
                    log.warn("[JWT-DEBUG] Token REVOKED (jti={})", claims.getId());
                }
            } catch (JwtException | IllegalArgumentException ex) {
                log.warn("[JWT-DEBUG] Token REJECTED — {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
