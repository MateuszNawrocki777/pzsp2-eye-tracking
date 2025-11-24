package org.pzsp2.eye_tracking.auth.crypto;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordService {

    private static final int SALT_BYTE_LENGTH = 16;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateSalt() {
        byte[] saltBytes = new byte[SALT_BYTE_LENGTH];
        secureRandom.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    public String hashPassword(String password, String saltBase64) {
        byte[] saltBytes = Base64.getDecoder().decode(saltBase64);
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] combined = new byte[saltBytes.length + passwordBytes.length];
        System.arraycopy(saltBytes, 0, combined, 0, saltBytes.length);
        System.arraycopy(passwordBytes, 0, combined, saltBytes.length, passwordBytes.length);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }
}
