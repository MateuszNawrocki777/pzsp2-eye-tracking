package org.pzsp2.eye_tracking.auth.dto;

import org.pzsp2.eye_tracking.user.UserRole;

import java.time.Instant;
import java.util.UUID;

public record RegisterResponse(
                UUID userId,
                String email,
                UserRole role,
                Instant createdAt,
                String token,
                Instant expiresAt) {
}
