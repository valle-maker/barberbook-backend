package com.udea.barberbook.barberbook_backend.security;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// One row per logged-out (revoked) JWT, keyed by its "jti" claim — HU-03.
// Rows past their expiresAt are harmless (the JWT would be rejected on expiry
// anyway) and can be purged later by a scheduled cleanup if the table grows.
@Entity
@Table(name = "revoked_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RevokedToken {

    @Id
    private String jti;

    @Column(nullable = false)
    private Instant expiresAt;
}
