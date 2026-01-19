package org.pzsp2.eye_tracking.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pzsp2.eye_tracking.auth.dto.LoginRequest;
import org.pzsp2.eye_tracking.auth.dto.LoginResponse;
import org.pzsp2.eye_tracking.auth.dto.RegisterRequest;
import org.pzsp2.eye_tracking.auth.dto.RegisterResponse;
import org.pzsp2.eye_tracking.user.UserRole;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    @Test
    void register_callsService() {
        RegisterRequest req = new RegisterRequest("a@b.c", "pw");
        RegisterResponse mockResp = new RegisterResponse(UUID.randomUUID(), "a@b.c", UserRole.USER, Instant.now(), "t", Instant.now());

        when(authService.register(req)).thenReturn(mockResp);

        RegisterResponse result = controller.register(req);
        assertThat(result).isEqualTo(mockResp);
        verify(authService).register(req);
    }

    @Test
    void login_callsService() {
        LoginRequest req = new LoginRequest("a@b.c", "pw");
        LoginResponse mockResp = new LoginResponse(UUID.randomUUID(), UserRole.USER, "t", Instant.now());

        when(authService.login(req)).thenReturn(mockResp);

        LoginResponse result = controller.login(req);
        assertThat(result).isEqualTo(mockResp);
        verify(authService).login(req);
    }
}