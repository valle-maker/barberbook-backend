package com.udea.barberbook.barberbook_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.udea.barberbook.barberbook_backend.user.domain.Role;
import com.udea.barberbook.barberbook_backend.user.domain.User;
import com.udea.barberbook.barberbook_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Creates the first ADMIN on an empty database (e.g. a fresh cloud deploy) from
// SEED_ADMIN_EMAIL / SEED_ADMIN_PASSWORD. Does nothing if either is unset or the
// email already exists, so there are no default credentials in the code.
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.email}")
    private String email;

    @Value("${app.seed.admin.password}")
    private String password;

    @Value("${app.seed.admin.name}")
    private String name;

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            return;
        }

        userRepository.save(User.builder()
            .fullName(name)
            .phone("0000000000")
            .email(normalizedEmail)
            .passwordHash(passwordEncoder.encode(password))
            .role(Role.ADMIN)
            .enabled(true)
            .build());

        log.info("Admin inicial creado: {}", normalizedEmail);
    }
}
