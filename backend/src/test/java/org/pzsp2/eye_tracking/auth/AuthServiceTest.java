package org.pzsp2.eye_tracking.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pzsp2.eye_tracking.auth.crypto.PasswordService;
import org.pzsp2.eye_tracking.auth.dto.LoginRequest;
import org.pzsp2.eye_tracking.auth.dto.RegisterRequest;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserAccountRepository;
import org.pzsp2.eye_tracking.user.UserRole;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldPersistNewUser() {
        RegisterRequest request = new RegisterRequest("test@example.com", "StrongPass1", UserRole.USER);
        given(userAccountRepository.existsByEmailIgnoreCase("test@example.com")).willReturn(false);
        given(passwordService.hashPassword("StrongPass1")).willReturn("hash");

        UserAccount persisted = new UserAccount(UUID.randomUUID(), "test@example.com", "hash",
                UserRole.USER);
        Instant createdAt = Instant.now();
        ReflectionTestUtils.setField(persisted, "createdAt", createdAt);
        given(userAccountRepository.save(any(UserAccount.class))).willReturn(persisted);

        var response = authService.register(request);

        assertEquals(persisted.getUserId(), response.userId());
        assertEquals("test@example.com", response.email());
        assertEquals(UserRole.USER, response.role());
        assertEquals(createdAt, response.createdAt());

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        assertEquals("hash", captor.getValue().getPasswordHash());
    }

    @Test
    void registerShouldFailForDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("test@example.com", "StrongPass1", UserRole.USER);
        given(userAccountRepository.existsByEmailIgnoreCase("test@example.com")).willReturn(true);

        assertThrows(ResponseStatusException.class, () -> authService.register(request));
    }

    @Test
    void loginShouldReturnUserDetailsWhenPasswordMatches() {
        LoginRequest request = new LoginRequest("test@example.com", "StrongPass1");
        UserAccount account = new UserAccount(UUID.randomUUID(), "test@example.com", "storedHash",
                UserRole.ADMIN);
        given(userAccountRepository.findByEmailIgnoreCase("test@example.com")).willReturn(Optional.of(account));
        given(passwordService.matches("StrongPass1", "storedHash")).willReturn(true);

        var response = authService.login(request);

        assertEquals(account.getUserId(), response.userId());
        assertEquals(UserRole.ADMIN, response.role());
        assertNotNull(response.loggedInAt());
        assertEquals("Login successful", response.message());
    }

    @Test
    void loginShouldFailForInvalidPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "WrongPass");
        UserAccount account = new UserAccount(UUID.randomUUID(), "test@example.com", "storedHash",
                UserRole.ADMIN);
        given(userAccountRepository.findByEmailIgnoreCase("test@example.com")).willReturn(Optional.of(account));
        given(passwordService.matches("WrongPass", "storedHash")).willReturn(false);

        assertThrows(ResponseStatusException.class, () -> authService.login(request));
    }
}
