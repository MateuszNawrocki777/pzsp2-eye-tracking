package org.pzsp2.eye_tracking.admin.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.pzsp2.eye_tracking.user.UserRole;

class AdminDtoTest {

    @Test void testAdminUserResponse() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        AdminUserResponse response = new AdminUserResponse(id, "email@test.com", UserRole.USER,
                        false, now);

        assertThat(response.userId()).isEqualTo(id);
        assertThat(response.email()).isEqualTo("email@test.com");
        assertThat(response.role()).isEqualTo(UserRole.USER);
        assertThat(response.banned()).isFalse();
        assertThat(response.createdAt()).isEqualTo(now);
    }

    @Test void testUpdateUserBannedRequest() {
        UpdateUserBannedRequest req = new UpdateUserBannedRequest(true);
        assertThat(req.banned()).isTrue();
    }

    @Test void testUpdateUserRoleRequest() {
        UpdateUserRoleRequest req = new UpdateUserRoleRequest(UserRole.ADMIN);
        assertThat(req.role()).isEqualTo(UserRole.ADMIN);
    }
}
