package org.pzsp2.eye_tracking.auth.dto;

import java.time.Instant;
import java.util.UUID;
import org.pzsp2.eye_tracking.user.UserRole;

public record LoginResponse(UUID userId, UserRole role, String token, Instant expiresAt) {
}
