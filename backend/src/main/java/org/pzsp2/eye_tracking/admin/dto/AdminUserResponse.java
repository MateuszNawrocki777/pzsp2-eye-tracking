package org.pzsp2.eye_tracking.admin.dto;

import java.time.Instant;
import java.util.UUID;
import org.pzsp2.eye_tracking.user.UserRole;

public record AdminUserResponse(
    UUID userId, String email, UserRole role, boolean banned, Instant createdAt) {}
