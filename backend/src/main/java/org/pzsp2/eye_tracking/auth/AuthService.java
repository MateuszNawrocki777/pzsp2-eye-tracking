package org.pzsp2.eye_tracking.auth;

import org.pzsp2.eye_tracking.auth.crypto.PasswordService;
import org.pzsp2.eye_tracking.auth.dto.LoginRequest;
import org.pzsp2.eye_tracking.auth.dto.LoginResponse;
import org.pzsp2.eye_tracking.auth.dto.RegisterRequest;
import org.pzsp2.eye_tracking.auth.dto.RegisterResponse;
import org.pzsp2.eye_tracking.auth.jwt.JwtService;
import org.pzsp2.eye_tracking.auth.jwt.JwtToken;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserAccountRepository;
import org.pzsp2.eye_tracking.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthService(UserAccountRepository userAccountRepository,
            PasswordService passwordService,
            JwtService jwtService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(CONFLICT, "Email is already in use");
        }

        UUID userId = UUID.randomUUID();
        String hash = passwordService.hashPassword(request.password());

        UserAccount userAccount = new UserAccount(
                userId,
                request.email().toLowerCase(),
                hash,
                UserRole.USER);

        UserAccount saved = userAccountRepository.save(userAccount);
        JwtToken token = jwtService.generateToken(saved);

        return new RegisterResponse(
                saved.getUserId(),
                saved.getEmail(),
                saved.getRole(),
                saved.getCreatedAt(),
                token.token(),
                token.expiresAt());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserAccount account = userAccountRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid email or password"));

        if (!passwordService.matches(request.password(), account.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid email or password");
        }

        if (account.isBanned()) {
            throw new ResponseStatusException(FORBIDDEN, "Account is banned");
        }

        JwtToken token = jwtService.generateToken(account);

        return new LoginResponse(account.getUserId(), account.getRole(), token.token(), token.expiresAt());
    }
}
