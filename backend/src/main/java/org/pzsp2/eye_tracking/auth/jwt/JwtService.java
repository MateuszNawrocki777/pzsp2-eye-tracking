package org.pzsp2.eye_tracking.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserRole;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties properties;
    private SecretKey secretKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public JwtToken generateToken(UserAccount account) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.expirationDuration());

        String token = Jwts.builder()
                .subject(account.getUserId().toString())
                .issuer(properties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim("email", account.getEmail())
                .claim("role", account.getRole().name())
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();

        return new JwtToken(token, expiresAt);
    }

    public Optional<JwtUserDetails> parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);
            String roleValue = claims.get("role", String.class);
            UserRole role = UserRole.valueOf(roleValue);
            Instant expiresAt = claims.getExpiration().toInstant();

            return Optional.of(new JwtUserDetails(userId, email, role, expiresAt));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
