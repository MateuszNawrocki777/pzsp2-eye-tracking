package org.pzsp2.eye_tracking.auth.crypto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordService passwordService;

    @Test
    void hashPassword_delegatesToEncoder() {
        given(passwordEncoder.encode("raw-password")).willReturn("hashed-password");

        String result = passwordService.hashPassword("raw-password");

        assertThat(result).isEqualTo("hashed-password");
        verify(passwordEncoder).encode("raw-password");
    }

    @Test
    void matches_delegatesToEncoder() {
        given(passwordEncoder.matches("raw", "encoded")).willReturn(true);

        boolean result = passwordService.matches("raw", "encoded");

        assertThat(result).isTrue();
        verify(passwordEncoder).matches("raw", "encoded");
    }
}