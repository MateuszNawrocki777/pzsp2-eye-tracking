package org.pzsp2.eye_tracking.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.pzsp2.eye_tracking.auth.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration @EnableWebSecurity @EnableMethodSecurity @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring Bean injection") public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${FRONTEND_URL:http://localhost:5173}") private String frontendUrl;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // TODO: change this later
        http.csrf(AbstractHttpConfigurer::disable).cors(Customizer.withDefaults())
                        .authorizeHttpRequests(auth -> auth
                                        .requestMatchers("/api/auth/**", "/v3/api-docs/**",
                                                        "/swagger-ui/**", "/swagger-ui.html")
                                        .permitAll().requestMatchers("/admin/**").hasRole("ADMIN")
                                        .anyRequest().permitAll())
                        .httpBasic(AbstractHttpConfigurer::disable)
                        .formLogin(AbstractHttpConfigurer::disable)
                        .addFilterBefore(jwtAuthenticationFilter,
                                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl)); // allow frontend
                                                               // from env
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // cookies/auth

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}