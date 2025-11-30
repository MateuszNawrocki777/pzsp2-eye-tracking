package org.pzsp2.eye_tracking.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

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
}
