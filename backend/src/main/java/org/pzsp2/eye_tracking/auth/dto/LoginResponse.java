package org.pzsp2.eye_tracking.auth.dto;

import org.pzsp2.eye_tracking.user.UserRole;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(
        UUID userId,
        UserRole role,
        Instant loggedInAt,
        String message) {
}
