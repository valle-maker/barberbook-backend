package com.udea.barberbook.barberbook_backend.security;

import java.time.Instant;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenBlocklistService {

    private final RevokedTokenRepository revokedTokenRepository;

    public void revoke(String jti, Instant expiresAt) {
        revokedTokenRepository.save(new RevokedToken(jti, expiresAt));
    }

    public boolean isRevoked(String jti) {
        return revokedTokenRepository.existsById(jti);
    }
}
