package org.pzsp2.eye_tracking.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.pzsp2.eye_tracking.user.UserRole;

class AuthDtoTest {

    @Test void testLoginRequest() {
        LoginRequest request = new LoginRequest("test@example.com", "password");

        assertThat(request.email()).isEqualTo("test@example.com");
        assertThat(request.password()).isEqualTo("password");
    }

    @Test void testRegisterRequest() {
        RegisterRequest request = new RegisterRequest("new@example.com", "secret");

        assertThat(request.email()).isEqualTo("new@example.com");
        assertThat(request.password()).isEqualTo("secret");
    }

    @Test void testLoginResponse() {
        UUID userId = UUID.randomUUID();
        Instant expiresAt = Instant.now();
        LoginResponse response = new LoginResponse(userId, UserRole.ADMIN, "token", expiresAt);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.role()).isEqualTo(UserRole.ADMIN);
        assertThat(response.token()).isEqualTo("token");
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
    }

    @Test void testRegisterResponse() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(3600);

        RegisterResponse response = new RegisterResponse(userId, "reg@example.com", UserRole.USER,
                        now, "jwt-token", expiresAt);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("reg@example.com");
        assertThat(response.role()).isEqualTo(UserRole.USER);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
    }
}
