package org.pzsp2.eye_tracking.auth.jwt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(String secret, String issuer, long expirationMinutes) {
  public Duration expirationDuration() {
    return Duration.ofMinutes(expirationMinutes);
  }
}
