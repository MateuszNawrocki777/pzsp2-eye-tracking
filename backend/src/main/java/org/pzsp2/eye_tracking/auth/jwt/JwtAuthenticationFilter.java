package org.pzsp2.eye_tracking.auth.jwt;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.pzsp2.eye_tracking.auth.AuthenticatedUser;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserAccountRepository userAccountRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserAccountRepository userAccountRepository) {
        this.jwtService = jwtService;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        JwtUserDetails details = jwtService.parseToken(token)
                .orElse(null);

        if (details == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired token");
            return;
        }

        UUID userId = details.userId();
        if (userId == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid token payload");
            return;
        }

        UserAccount account = userAccountRepository.findById(userId).orElse(null);
        if (account == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "User not found");
            return;
        }

        if (account.isBanned()) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "Account is banned");
            return;
        }

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(account.getUserId(), account.getEmail(),
                account.getRole());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                authenticatedUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
