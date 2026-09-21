package com.udea.barberbook.barberbook_backend.auth.service;

import java.time.Instant;

import com.udea.barberbook.barberbook_backend.auth.dto.AuthResponse;
import com.udea.barberbook.barberbook_backend.auth.dto.GoogleLoginRequest;
import com.udea.barberbook.barberbook_backend.auth.dto.LoginRequest;
import com.udea.barberbook.barberbook_backend.auth.dto.RegisterRequest;
import com.udea.barberbook.barberbook_backend.auth.dto.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse loginWithGoogle(GoogleLoginRequest request);

    void logout(String jti, Instant tokenExpiresAt);
}
