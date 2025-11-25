package org.pzsp2.eye_tracking.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.pzsp2.eye_tracking.user.UserRole;

public record RegisterRequest(
        @Email(message = "Email must be valid") @NotBlank(message = "Email is required") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters") String password,

        @NotNull(message = "Role is required") UserRole role) {
}
