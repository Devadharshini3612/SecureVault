package com.securevault.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig
 * 
 * Configures Spring Security for the application.
 * 
 * Key configurations:
 * 1. Disable default login page (we use JWT, not sessions)
 * 2. Set stateless session management (no HTTP sessions)
 * 3. Permit public endpoints (/api/auth/register, /api/auth/login)
 * 4. Protect vault endpoints (/api/credentials/**, /api/password/**)
 * 5. Add JWT filter before authentication
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Configures the security filter chain
     * 
     * @param http the HttpSecurity object to configure
     * @return the SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (Cross-Site Request Forgery) protection
                // We don't need it for stateless REST APIs with JWT
                .csrf(csrf -> csrf.disable())

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC endpoints - anyone can access without JWT
                        .requestMatchers(
                                "/api/auth/register",      // User registration
                                "/api/auth/login",         // User login
                                "/api/password/**"         // Password generator utilities
                        ).permitAll()

                        // PROTECTED endpoints - require JWT authentication
                        .requestMatchers(
                                "/api/credentials/**"      // All vault/credential endpoints
                        ).authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Set session management to STATELESS
                // We don't use HTTP sessions, only JWT tokens
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Add JWT filter before the default authentication filter
                // This ensures JWT validation happens first
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
