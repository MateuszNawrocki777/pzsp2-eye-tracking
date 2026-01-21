package org.pzsp2.eye_tracking.auth.dto;

import java.time.Instant;
import java.util.UUID;
import org.pzsp2.eye_tracking.user.UserRole;

public record RegisterResponse(UUID userId, String email, UserRole role, Instant createdAt,
                String token, Instant expiresAt) {
}
