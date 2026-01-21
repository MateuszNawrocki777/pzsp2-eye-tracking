package org.pzsp2.eye_tracking.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user") public class UserAccount {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false) private UUID userId;

    @Column(name = "email", nullable = false, unique = true) private String email;

    @Column(name = "password_hash", nullable = false, length = 100) private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32) private UserRole role;

    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    @Column(name = "is_banned", nullable = false) private boolean banned;

    protected UserAccount() {
        // for JPA
    }

    public UserAccount(UUID userId, String email, String passwordHash, UserRole role) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.banned = false;
    }

    @PrePersist public void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
