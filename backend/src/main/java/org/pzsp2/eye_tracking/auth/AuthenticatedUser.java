package org.pzsp2.eye_tracking.auth;

import java.util.UUID;
import org.pzsp2.eye_tracking.user.UserRole;

public record AuthenticatedUser(UUID userId, String email, UserRole role) {}
