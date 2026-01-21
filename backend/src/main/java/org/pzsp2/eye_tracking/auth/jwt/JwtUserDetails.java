package org.pzsp2.eye_tracking.auth.jwt;

import java.time.Instant;
import java.util.UUID;
import org.pzsp2.eye_tracking.user.UserRole;

public record JwtUserDetails(UUID userId, String email, UserRole role, Instant expiresAt) {
}
