package org.pzsp2.eye_tracking.admin;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.pzsp2.eye_tracking.admin.dto.AdminUserResponse;
import org.pzsp2.eye_tracking.admin.dto.UpdateUserBannedRequest;
import org.pzsp2.eye_tracking.admin.dto.UpdateUserRoleRequest;
import org.pzsp2.eye_tracking.auth.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users") public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping public List<AdminUserResponse> listOtherUsers(
                    @AuthenticationPrincipal AuthenticatedUser adminUser) {
        UUID currentUserId = adminUser != null ? adminUser.userId() : null;
        return adminUserService.getAllOtherUsers(currentUserId);
    }

    @PostMapping("/{userId}/role")
    @ResponseStatus(HttpStatus.OK) public AdminUserResponse updateRole(@PathVariable UUID userId,
                    @Valid @RequestBody UpdateUserRoleRequest request) {
        return adminUserService.updateRole(userId, request.role());
    }

    @PostMapping("/{userId}/banned")
    @ResponseStatus(HttpStatus.OK) public AdminUserResponse updateBanned(@PathVariable UUID userId,
                    @Valid @RequestBody UpdateUserBannedRequest request) {
        return adminUserService.updateBanned(userId, request.banned());
    }
}
