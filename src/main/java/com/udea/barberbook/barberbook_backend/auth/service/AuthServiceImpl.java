package com.udea.barberbook.barberbook_backend.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.udea.barberbook.barberbook_backend.auth.dto.AuthResponse;
import com.udea.barberbook.barberbook_backend.auth.dto.GoogleLoginRequest;
import com.udea.barberbook.barberbook_backend.auth.dto.LoginRequest;
import com.udea.barberbook.barberbook_backend.auth.dto.RegisterRequest;
import com.udea.barberbook.barberbook_backend.auth.dto.RegisterResponse;
import com.udea.barberbook.barberbook_backend.auth.google.GoogleTokenInfo;
import com.udea.barberbook.barberbook_backend.auth.google.GoogleTokenVerifier;
import com.udea.barberbook.barberbook_backend.common.exception.AccountLockedException;
import com.udea.barberbook.barberbook_backend.common.exception.EmailAlreadyExistsException;
import com.udea.barberbook.barberbook_backend.common.exception.GoogleAuthNotConfiguredException;
import com.udea.barberbook.barberbook_backend.common.exception.InvalidCredentialsException;
import com.udea.barberbook.barberbook_backend.common.exception.InvalidGoogleTokenException;
import com.udea.barberbook.barberbook_backend.security.JwtService;
import com.udea.barberbook.barberbook_backend.security.TokenBlocklistService;
import com.udea.barberbook.barberbook_backend.user.domain.Role;
import com.udea.barberbook.barberbook_backend.user.domain.User;
import com.udea.barberbook.barberbook_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration ATTEMPT_WINDOW = Duration.ofMinutes(5);
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);
    private static final String GENERIC_LOGIN_ERROR = "Correo o contraseña incorrectos.";
    private static final String LOCKED_MESSAGE =
        "Demasiados intentos fallidos. Cuenta temporalmente bloqueada por seguridad.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final TokenBlocklistService tokenBlocklistService;

    @Value("${google.client-id}")
    private String googleClientId;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Este correo ya se encuentra registrado. Intenta iniciar sesión.");
        }

        User user = User.builder()
            .fullName(request.fullName().trim())
            .phone(request.phone().trim())
            .email(normalizedEmail)
            .passwordHash(passwordEncoder.encode(request.password()))
            .role(Role.CLIENTE)
            .enabled(true)
            .failedLoginAttempts(0)
            .build();

        User saved = userRepository.save(user);

        return new RegisterResponse(
            saved.getId(),
            saved.getFullName(),
            saved.getEmail(),
            saved.getRole().name(),
            "¡Registro exitoso! Ya puedes iniciar sesión"
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new InvalidCredentialsException(GENERIC_LOGIN_ERROR));

        Instant now = Instant.now();
        enforceNotLocked(user, now);
        resetAttemptWindowIfExpired(user, now);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw registerFailedAttempt(user, now);
        }

        user.setFailedLoginAttempts(0);
        user.setLastFailedAttemptAt(null);
        user.setLockedUntil(null);
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new GoogleAuthNotConfiguredException(
                "El inicio de sesión con Google no está configurado todavía en el servidor.");
        }

        GoogleTokenInfo info = googleTokenVerifier.verify(request.idToken());

        if (info == null || info.email() == null || !googleClientId.equals(info.audience())) {
            throw new InvalidGoogleTokenException("Token de Google inválido.");
        }

        String email = info.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                .fullName(info.name() != null && !info.name().isBlank() ? info.name() : email)
                .phone("")
                .email(email)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.CLIENTE)
                .googleId(info.subject())
                .enabled(true)
                .failedLoginAttempts(0)
                .build();
            return userRepository.save(newUser);
        });

        return buildAuthResponse(user);
    }

    private void enforceNotLocked(User user, Instant now) {
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new AccountLockedException(LOCKED_MESSAGE);
        }
        if (user.getLockedUntil() != null) {
            // lock window passed — clear it before evaluating this attempt
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
        }
    }

    private void resetAttemptWindowIfExpired(User user, Instant now) {
        if (user.getLastFailedAttemptAt() != null
            && Duration.between(user.getLastFailedAttemptAt(), now).compareTo(ATTEMPT_WINDOW) > 0) {
            user.setFailedLoginAttempts(0);
        }
    }

    private RuntimeException registerFailedAttempt(User user, Instant now) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        user.setLastFailedAttemptAt(now);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(now.plus(LOCK_DURATION));
            userRepository.save(user);
            return new AccountLockedException(LOCKED_MESSAGE);
        }

        userRepository.save(user);
        return new InvalidCredentialsException(
            GENERIC_LOGIN_ERROR + " Intento " + attempts + " de " + MAX_FAILED_ATTEMPTS + "."
        );
    }

    @Override
    public void logout(String jti, Instant tokenExpiresAt) {
        tokenBlocklistService.revoke(jti, tokenExpiresAt);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getRole().toFrontendValue(), user.getFullName(), user.getEmail());
    }
}
