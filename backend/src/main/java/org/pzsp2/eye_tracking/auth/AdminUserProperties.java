package org.pzsp2.eye_tracking.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security.admin")
public record AdminUserProperties(
        @Email @NotBlank String email,
        @NotBlank String password) {
}
