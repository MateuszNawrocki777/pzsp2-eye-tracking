package org.pzsp2.eye_tracking.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pzsp2.eye_tracking.admin.dto.AdminUserResponse;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserAccountRepository;
import org.pzsp2.eye_tracking.user.UserRole;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

  @Mock private UserAccountRepository userAccountRepository;

  @InjectMocks private AdminUserService service;

  @Test
  void getAllOtherUsers_filtersOutCurrentUser() {
    UUID myId = UUID.randomUUID();
    UUID otherId = UUID.randomUUID();

    UserAccount me = new UserAccount(myId, "me@test.com", "pass", UserRole.ADMIN);

    UserAccount other = new UserAccount(otherId, "other@test.com", "pass", UserRole.USER);

    given(userAccountRepository.findAll()).willReturn(List.of(me, other));

    List<AdminUserResponse> result = service.getAllOtherUsers(myId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).userId()).isEqualTo(otherId);
    assertThat(result.get(0).email()).isEqualTo("other@test.com");
  }

  @Test
  void getAllOtherUsers_returnsAll_whenCurrentUserIdIsNull() {
    UUID id = UUID.randomUUID();
    UserAccount u1 = new UserAccount(id, "u1@test.com", "pw", UserRole.USER);

    given(userAccountRepository.findAll()).willReturn(List.of(u1));

    List<AdminUserResponse> result = service.getAllOtherUsers(null);

    assertThat(result).hasSize(1);
  }

  @Test
  void updateRole_success() {
    UUID userId = UUID.randomUUID();
    UserAccount user = new UserAccount(userId, "user@test.com", "pw", UserRole.USER);

    given(userAccountRepository.findById(userId)).willReturn(Optional.of(user));

    AdminUserResponse response = service.updateRole(userId, UserRole.ADMIN);

    assertThat(response.role()).isEqualTo(UserRole.ADMIN);
    assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
  }

  @Test
  void updateRole_throwsBadRequest_whenIdNull() {
    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.updateRole(null, UserRole.ADMIN));

    assertThat(ex.getStatusCode()).isEqualTo(BAD_REQUEST);
    assertThat(ex.getReason()).isEqualTo("User id is required");
  }

  @Test
  void updateRole_throwsNotFound_whenUserMissing() {
    UUID userId = UUID.randomUUID();
    given(userAccountRepository.findById(userId)).willReturn(Optional.empty());

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> service.updateRole(userId, UserRole.ADMIN));

    assertThat(ex.getStatusCode()).isEqualTo(NOT_FOUND);
  }

  @Test
  void updateBanned_success() {
    UUID userId = UUID.randomUUID();
    UserAccount user = new UserAccount(userId, "user@test.com", "pw", UserRole.USER);
    user.setBanned(false);

    given(userAccountRepository.findById(userId)).willReturn(Optional.of(user));

    AdminUserResponse response = service.updateBanned(userId, true);

    assertThat(response.banned()).isTrue();
    assertThat(user.isBanned()).isTrue();
  }

  @Test
  void updateBanned_throwsNotFound() {
    UUID userId = UUID.randomUUID();
    given(userAccountRepository.findById(userId)).willReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> service.updateBanned(userId, true));
  }
}
