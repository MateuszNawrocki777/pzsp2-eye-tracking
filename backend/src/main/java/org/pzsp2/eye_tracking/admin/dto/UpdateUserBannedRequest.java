package org.pzsp2.eye_tracking.admin.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserBannedRequest(@NotNull Boolean banned) {
}
