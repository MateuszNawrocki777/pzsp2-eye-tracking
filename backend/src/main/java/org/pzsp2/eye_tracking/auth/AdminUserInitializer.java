package org.pzsp2.eye_tracking.auth;

import java.util.UUID;
import org.pzsp2.eye_tracking.auth.crypto.PasswordService;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserAccountRepository;
import org.pzsp2.eye_tracking.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class AdminUserInitializer implements ApplicationRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdminUserInitializer.class);

  private final AdminUserProperties adminUserProperties;
  private final UserAccountRepository userAccountRepository;
  private final PasswordService passwordService;

  AdminUserInitializer(
      AdminUserProperties adminUserProperties,
      UserAccountRepository userAccountRepository,
      PasswordService passwordService) {
    this.adminUserProperties = adminUserProperties;
    this.userAccountRepository = userAccountRepository;
    this.passwordService = passwordService;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    String email = adminUserProperties.email().toLowerCase();

    userAccountRepository
        .findByEmailIgnoreCase(email)
        .ifPresentOrElse(
            existing -> {
              if (existing.getRole() != UserRole.ADMIN) {
                LOGGER.warn(
                    "Configured admin email {} already exists with role {}. Skipping auto-setup.",
                    email,
                    existing.getRole());
                return;
              }

              if (!passwordService.matches(
                  adminUserProperties.password(), existing.getPasswordHash())) {
                existing.updatePasswordHash(
                    passwordService.hashPassword(adminUserProperties.password()));
                userAccountRepository.save(existing);
                LOGGER.info("Updated password for configured admin user {}", email);
              }
            },
            () -> {
              String hash = passwordService.hashPassword(adminUserProperties.password());
              UserAccount admin = new UserAccount(UUID.randomUUID(), email, hash, UserRole.ADMIN);
              userAccountRepository.save(admin);
              LOGGER.info("Created default admin user {}", email);
            });
  }
}
