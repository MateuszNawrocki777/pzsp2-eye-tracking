package org.pzsp2.eye_tracking.auth;

import org.pzsp2.eye_tracking.auth.crypto.PasswordService;
import org.pzsp2.eye_tracking.auth.dto.LoginRequest;
import org.pzsp2.eye_tracking.auth.dto.LoginResponse;
import org.pzsp2.eye_tracking.auth.dto.RegisterRequest;
import org.pzsp2.eye_tracking.auth.dto.RegisterResponse;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordService passwordService;

    public AuthService(UserAccountRepository userAccountRepository, PasswordService passwordService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordService = passwordService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(CONFLICT, "Email is already in use");
        }

        UUID userId = UUID.randomUUID();
        String salt = passwordService.generateSalt();
        String hash = passwordService.hashPassword(request.password(), salt);

        UserAccount userAccount = new UserAccount(
                userId,
                request.email().toLowerCase(),
                hash,
                salt,
                request.role());

        UserAccount saved = userAccountRepository.save(userAccount);
        return new RegisterResponse(saved.getUserId(), saved.getEmail(), saved.getRole(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserAccount account = userAccountRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid email or password"));

        String computedHash = passwordService.hashPassword(request.password(), account.getPasswordSalt());
        if (!computedHash.equals(account.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid email or password");
        }

        return new LoginResponse(account.getUserId(), account.getRole(), Instant.now(), "Login successful");
    }
}
