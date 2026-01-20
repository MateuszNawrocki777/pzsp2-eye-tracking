package org.pzsp2.eye_tracking.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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

  @Mock private AdminUserProperties adminUserProperties;

  @Mock private UserAccountRepository userAccountRepository;

  @Mock private PasswordService passwordService;

  @InjectMocks private AdminUserInitializer initializer;

  @Test
  void shouldCreateAdminWhenMissing() {
    given(adminUserProperties.email()).willReturn("admin@example.com");
    given(adminUserProperties.password()).willReturn("StrongPass123!");
    given(userAccountRepository.findByEmailIgnoreCase("admin@example.com"))
        .willReturn(Optional.empty());
    given(passwordService.hashPassword("StrongPass123!")).willReturn("hashed");

    initializer.run(mock(ApplicationArguments.class));

    ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
    verify(userAccountRepository).save(captor.capture());
    UserAccount saved = captor.getValue();
    assertEquals(UserRole.ADMIN, saved.getRole());
  }

  @Test
  void shouldUpdatePasswordWhenAdminExistsWithDifferentSecret() {
    given(adminUserProperties.email()).willReturn("admin@example.com");
    given(adminUserProperties.password()).willReturn("NewPass!");

    UserAccount existing =
        new UserAccount(UUID.randomUUID(), "admin@example.com", "old-hash", UserRole.ADMIN);

    given(userAccountRepository.findByEmailIgnoreCase("admin@example.com"))
        .willReturn(Optional.of(existing));
    given(passwordService.matches("NewPass!", "old-hash")).willReturn(false);
    given(passwordService.hashPassword("NewPass!")).willReturn("new-hash");

    initializer.run(mock(ApplicationArguments.class));

    assertEquals("new-hash", existing.getPasswordHash());
    verify(userAccountRepository).save(existing);
  }

  @Test
  void shouldDoNothingWhenPasswordMatches() {
    given(adminUserProperties.email()).willReturn("admin@example.com");
    given(adminUserProperties.password()).willReturn("SamePass!");

    UserAccount existing =
        new UserAccount(UUID.randomUUID(), "admin@example.com", "hash", UserRole.ADMIN);

    given(userAccountRepository.findByEmailIgnoreCase("admin@example.com"))
        .willReturn(Optional.of(existing));
    given(passwordService.matches("SamePass!", "hash")).willReturn(true);

    initializer.run(mock(ApplicationArguments.class));

    verify(userAccountRepository, never()).save(any());
  }

  @Test
  void shouldSkipSetupWhenUserExistsButNotAdmin() {
    given(adminUserProperties.email()).willReturn("user@example.com");

    UserAccount existing =
        new UserAccount(UUID.randomUUID(), "user@example.com", "hash", UserRole.USER);

    given(userAccountRepository.findByEmailIgnoreCase("user@example.com"))
        .willReturn(Optional.of(existing));

    initializer.run(mock(ApplicationArguments.class));

    verify(passwordService, never()).matches(any(), any());
    verify(userAccountRepository, never()).save(any());
  }
}
