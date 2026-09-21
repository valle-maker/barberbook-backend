package com.udea.barberbook.barberbook_backend.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import com.udea.barberbook.barberbook_backend.auth.dto.AuthResponse;
import com.udea.barberbook.barberbook_backend.auth.dto.GoogleLoginRequest;
import com.udea.barberbook.barberbook_backend.auth.dto.LoginRequest;
import com.udea.barberbook.barberbook_backend.auth.dto.RegisterRequest;
import com.udea.barberbook.barberbook_backend.auth.dto.RegisterResponse;
import com.udea.barberbook.barberbook_backend.auth.service.AuthService;
import com.udea.barberbook.barberbook_backend.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String jti = (String) request.getAttribute(JwtAuthenticationFilter.JTI_ATTRIBUTE);
        Instant expiresAt = (Instant) request.getAttribute(JwtAuthenticationFilter.EXPIRATION_ATTRIBUTE);
        authService.logout(jti, expiresAt);
        return ResponseEntity.noContent().build();
    }
}
