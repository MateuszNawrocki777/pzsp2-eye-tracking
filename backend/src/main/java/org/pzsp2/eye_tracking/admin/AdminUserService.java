package org.pzsp2.eye_tracking.admin;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.List;
import java.util.UUID;
import org.pzsp2.eye_tracking.admin.dto.AdminUserResponse;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserAccountRepository;
import org.pzsp2.eye_tracking.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service public class AdminUserService {

    private final UserAccountRepository userAccountRepository;

    public AdminUserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true) public List<AdminUserResponse> getAllOtherUsers(
                    UUID currentUserId) {
        return userAccountRepository.findAll().stream()
                        .filter(account -> currentUserId == null
                                        || !account.getUserId().equals(currentUserId))
                        .map(this::toResponse).toList();
    }

    @Transactional public AdminUserResponse updateRole(UUID userId, UserRole role) {
        UserAccount account = loadUser(userId);
        account.setRole(role);
        return toResponse(account);
    }

    @Transactional public AdminUserResponse updateBanned(UUID userId, boolean banned) {
        UserAccount account = loadUser(userId);
        account.setBanned(banned);
        return toResponse(account);
    }

    private UserAccount loadUser(UUID userId) {
        if (userId == null) {
            throw new ResponseStatusException(BAD_REQUEST, "User id is required");
        }
        return userAccountRepository.findById(userId).orElseThrow(
                        () -> new ResponseStatusException(NOT_FOUND, "User not found"));
    }

    private AdminUserResponse toResponse(UserAccount account) {
        return new AdminUserResponse(account.getUserId(), account.getEmail(), account.getRole(),
                        account.isBanned(), account.getCreatedAt());
    }
}
