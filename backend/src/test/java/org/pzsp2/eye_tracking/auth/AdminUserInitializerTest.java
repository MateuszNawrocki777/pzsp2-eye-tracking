package org.pzsp2.eye_tracking.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pzsp2.eye_tracking.auth.crypto.PasswordService;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserAccountRepository;
import org.pzsp2.eye_tracking.user.UserRole;
import org.springframework.boot.ApplicationArguments;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AdminUserInitializerTest {

    @Mock
    private AdminUserProperties adminUserProperties;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private AdminUserInitializer initializer;

    @Test
    void shouldCreateAdminWhenMissing() throws Exception {
        given(adminUserProperties.email()).willReturn("admin@example.com");
        given(adminUserProperties.password()).willReturn("StrongPass123!");
        given(userAccountRepository.findByEmailIgnoreCase("admin@example.com")).willReturn(Optional.empty());
        given(passwordService.hashPassword("StrongPass123!")).willReturn("hashed");

        initializer.run(mock(ApplicationArguments.class));

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        UserAccount saved = captor.getValue();
        assertEquals(UserRole.ADMIN, saved.getRole());
        assertEquals("admin@example.com", saved.getEmail());
        assertEquals("hashed", saved.getPasswordHash());
    }

    @Test
    void shouldUpdatePasswordWhenAdminExistsWithDifferentSecret() throws Exception {
        given(adminUserProperties.email()).willReturn("admin@example.com");
        given(adminUserProperties.password()).willReturn("StrongPass123!");

        UserAccount existing = new UserAccount(UUID.randomUUID(), "admin@example.com", "old-hash", UserRole.ADMIN);

        given(userAccountRepository.findByEmailIgnoreCase("admin@example.com")).willReturn(Optional.of(existing));
        given(passwordService.matches("StrongPass123!", "old-hash")).willReturn(false);
        given(passwordService.hashPassword("StrongPass123!")).willReturn("new-hash");

        initializer.run(mock(ApplicationArguments.class));

        assertEquals("new-hash", existing.getPasswordHash());
        verify(userAccountRepository).save(existing);
    }
}
