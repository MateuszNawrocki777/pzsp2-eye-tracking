package org.pzsp2.eye_tracking.auth.jwt;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserAccountRepository;
import org.pzsp2.eye_tracking.user.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private JwtService jwtService;

  @Mock private UserAccountRepository userAccountRepository;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @InjectMocks private JwtAuthenticationFilter filter;

  @Test
  void doFilter_noAuthHeader_continuesChain() throws Exception {
    given(request.getHeader("Authorization")).willReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilter_badAuthHeaderFormat_continuesChain() throws Exception {
    given(request.getHeader("Authorization")).willReturn("Basic adsfadfas");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void doFilter_invalidToken_sends401() throws Exception {
    given(request.getHeader("Authorization")).willReturn("Bearer invalid_token");
    given(jwtService.parseToken("invalid_token")).willReturn(Optional.empty());

    filter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired token");
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void doFilter_validToken_missingUserId_sends401() throws Exception {
    JwtUserDetails details = new JwtUserDetails(null, "email", UserRole.USER, Instant.now());

    given(request.getHeader("Authorization")).willReturn("Bearer token_no_id");
    given(jwtService.parseToken("token_no_id")).willReturn(Optional.of(details));

    filter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid token payload");
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void doFilter_validToken_userNotFound_sends401() throws Exception {
    UUID userId = UUID.randomUUID();
    JwtUserDetails details = new JwtUserDetails(userId, "email", UserRole.USER, Instant.now());

    given(request.getHeader("Authorization")).willReturn("Bearer valid_token");
    given(jwtService.parseToken("valid_token")).willReturn(Optional.of(details));
    given(userAccountRepository.findById(userId)).willReturn(Optional.empty());

    filter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpStatus.UNAUTHORIZED.value(), "User not found");
  }

  @Test
  void doFilter_validToken_userBanned_sends403() throws Exception {
    UUID userId = UUID.randomUUID();
    JwtUserDetails details = new JwtUserDetails(userId, "email", UserRole.USER, Instant.now());
    UserAccount account = new UserAccount(userId, "email", "pass", UserRole.USER);
    account.setBanned(true);

    given(request.getHeader("Authorization")).willReturn("Bearer valid_token");
    given(jwtService.parseToken("valid_token")).willReturn(Optional.of(details));
    given(userAccountRepository.findById(userId)).willReturn(Optional.of(account));

    filter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpStatus.FORBIDDEN.value(), "Account is banned");
  }

  @Test
  void doFilter_success_setsAuthentication() throws Exception {
    UUID userId = UUID.randomUUID();
    JwtUserDetails details = new JwtUserDetails(userId, "email", UserRole.USER, Instant.now());
    UserAccount account = new UserAccount(userId, "email", "pass", UserRole.USER);

    given(request.getHeader("Authorization")).willReturn("Bearer valid_token");
    given(jwtService.parseToken("valid_token")).willReturn(Optional.of(details));
    given(userAccountRepository.findById(userId)).willReturn(Optional.of(account));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }
}
