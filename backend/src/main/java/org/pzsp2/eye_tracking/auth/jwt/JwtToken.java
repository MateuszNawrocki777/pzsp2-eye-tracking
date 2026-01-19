package org.pzsp2.eye_tracking.auth.jwt;

import java.time.Instant;

public record JwtToken(String token, Instant expiresAt) {}
