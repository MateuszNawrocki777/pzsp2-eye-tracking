package org.pzsp2.eye_tracking.auth.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserRole;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "very_long_secret_key_that_is_secure_enough_for_hs512_algorithm_testing_purposes";

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(secret, "test-issuer", 60);
        jwtService = new JwtService(props);
        ReflectionTestUtils.invokeMethod(jwtService, "init");
    }

    @Test
    void generateAndParseToken_success() {
        UserAccount user = new UserAccount(UUID.randomUUID(), "user@test.com", "pw", UserRole.USER);

        JwtToken jwt = jwtService.generateToken(user);
        assertThat(jwt.token()).isNotEmpty();

        Optional<JwtUserDetails> details = jwtService.parseToken(jwt.token());
        assertThat(details).isPresent();
        assertThat(details.get().userId()).isEqualTo(user.getUserId());
        assertThat(details.get().email()).isEqualTo("user@test.com");
        assertThat(details.get().role()).isEqualTo(UserRole.USER);
    }

    @Test
    void parseToken_invalidToken_returnsEmpty() {
        Optional<JwtUserDetails> details = jwtService.parseToken("invalid.token.string");
        assertThat(details).isEmpty();
    }

    @Test
    void parseToken_nullOrEmpty_returnsEmpty() {
        assertThat(jwtService.parseToken(null)).isEmpty();
        assertThat(jwtService.parseToken("")).isEmpty();
        assertThat(jwtService.parseToken("   ")).isEmpty();
    }

    @Test
    void parseToken_wrongSignature_returnsEmpty() {
        JwtProperties otherProps = new JwtProperties(secret + "_other", "test", 60);
        JwtService otherService = new JwtService(otherProps);
        ReflectionTestUtils.invokeMethod(otherService, "init");

        UserAccount user = new UserAccount(UUID.randomUUID(), "a@b.c", "pw", UserRole.USER);
        String token = otherService.generateToken(user).token();

        Optional<JwtUserDetails> details = jwtService.parseToken(token);
        assertThat(details).isEmpty();
    }
}