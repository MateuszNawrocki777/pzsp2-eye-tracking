package org.pzsp2.eye_tracking.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pzsp2.eye_tracking.auth.crypto.PasswordService;
import org.pzsp2.eye_tracking.auth.dto.LoginRequest;
import org.pzsp2.eye_tracking.auth.dto.RegisterRequest;
import org.pzsp2.eye_tracking.auth.jwt.JwtService;
import org.pzsp2.eye_tracking.auth.jwt.JwtToken;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserAccountRepository;
import org.pzsp2.eye_tracking.user.UserRole;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class) class AuthServiceTest {

    @Mock private UserAccountRepository userAccountRepository;
    @Mock private PasswordService passwordService;
    @Mock private JwtService jwtService;

    private AuthService authService;

    @BeforeEach void setUp() {
        authService = new AuthService(userAccountRepository, passwordService, jwtService);
    }

    @Test void register_success() {
        RegisterRequest request = new RegisterRequest("test@example.com", "pass");
        given(userAccountRepository.existsByEmailIgnoreCase("test@example.com")).willReturn(false);
        given(passwordService.hashPassword("pass")).willReturn("hash");

        UserAccount savedUser = new UserAccount(UUID.randomUUID(), "test@example.com", "hash",
                        UserRole.USER);
        given(userAccountRepository.save(any(UserAccount.class))).willReturn(savedUser);

        JwtToken token = new JwtToken("jwt", Instant.now());
        given(jwtService.generateToken(savedUser)).willReturn(token);

        var response = authService.register(request);

        assertThat(response.email()).isEqualTo("test@example.com");
        verify(userAccountRepository).save(any(UserAccount.class));
    }

    @Test void register_conflict_emailExists() {
        RegisterRequest request = new RegisterRequest("test@example.com", "pass");
        given(userAccountRepository.existsByEmailIgnoreCase("test@example.com")).willReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                        () -> authService.register(request));

        assertThat(ex.getStatusCode()).isEqualTo(CONFLICT);
    }

    @Test void login_success() {
        LoginRequest req = new LoginRequest("u@t.com", "pass");
        UserAccount account = new UserAccount(UUID.randomUUID(), "u@t.com", "hash", UserRole.USER);
        account.setBanned(false);

        given(userAccountRepository.findByEmailIgnoreCase("u@t.com"))
                        .willReturn(Optional.of(account));
        given(passwordService.matches("pass", "hash")).willReturn(true);
        given(jwtService.generateToken(account)).willReturn(new JwtToken("jwt", Instant.now()));

        var res = authService.login(req);

        assertThat(res.token()).isEqualTo("jwt");
    }

    @Test void login_unauthorized_userNotFound() {
        LoginRequest req = new LoginRequest("missing@t.com", "pass");
        given(userAccountRepository.findByEmailIgnoreCase("missing@t.com"))
                        .willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                        () -> authService.login(req));
        assertThat(ex.getStatusCode()).isEqualTo(UNAUTHORIZED);
    }

    @Test void login_unauthorized_badPassword() {
        LoginRequest req = new LoginRequest("u@t.com", "wrong");
        UserAccount account = new UserAccount(UUID.randomUUID(), "u@t.com", "hash", UserRole.USER);

        given(userAccountRepository.findByEmailIgnoreCase("u@t.com"))
                        .willReturn(Optional.of(account));
        given(passwordService.matches("wrong", "hash")).willReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                        () -> authService.login(req));
        assertThat(ex.getStatusCode()).isEqualTo(UNAUTHORIZED);
    }

    @Test void login_forbidden_bannedUser() {
        LoginRequest req = new LoginRequest("banned@t.com", "pass");
        UserAccount account = new UserAccount(UUID.randomUUID(), "banned@t.com", "hash",
                        UserRole.USER);
        account.setBanned(true);

        given(userAccountRepository.findByEmailIgnoreCase("banned@t.com"))
                        .willReturn(Optional.of(account));
        given(passwordService.matches("pass", "hash")).willReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                        () -> authService.login(req));
        assertThat(ex.getStatusCode()).isEqualTo(FORBIDDEN);
        assertThat(ex.getReason()).isEqualTo("Account is banned");
    }
}
