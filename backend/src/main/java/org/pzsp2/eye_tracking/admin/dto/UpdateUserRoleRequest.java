package org.pzsp2.eye_tracking.admin.dto;

import jakarta.validation.constraints.NotNull;
import org.pzsp2.eye_tracking.user.UserRole;

public record UpdateUserRoleRequest(@NotNull UserRole role) {}
