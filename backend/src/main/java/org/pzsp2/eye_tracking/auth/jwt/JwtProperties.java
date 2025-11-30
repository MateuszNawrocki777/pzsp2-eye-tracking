package org.pzsp2.eye_tracking.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        long expirationMinutes) {
    public Duration expirationDuration() {
        return Duration.ofMinutes(expirationMinutes);
    }
}
