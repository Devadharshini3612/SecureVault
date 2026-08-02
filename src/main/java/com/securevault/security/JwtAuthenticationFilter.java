package com.securevault.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JwtAuthenticationFilter
 * 
 * This filter intercepts EVERY HTTP request and checks for a valid JWT token.
 * 
 * Flow:
 * 1. Extract JWT token from Authorization header
 * 2. Validate the token
 * 3. If valid, authenticate the user
 * 4. Continue with the request
 * 
 * Authorization Header Format:
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    /**
     * Filter method that runs for every HTTP request
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to continue processing
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Step 1: Extract Authorization header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Step 2: Check if Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No JWT token found, continue without authentication
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract JWT token (remove "Bearer " prefix)
        jwt = authHeader.substring(7);

        try {
            // Step 4: Extract username (email) from token
            userEmail = jwtService.extractUsername(jwt);

            // Step 5: If username is found and user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Step 6: Validate the token
                if (jwtService.validateToken(jwt, userEmail)) {

                    // Step 7: Create authentication object
                    // We use an empty list for authorities since we don't have roles yet
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail,
                            null,
                            new ArrayList<>()
                    );

                    // Step 8: Set additional details (IP address, session ID, etc.)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Step 9: Set the authentication in the security context
                    // This tells Spring Security that the user is authenticated
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token is invalid or expired, continue without authentication
            // Spring Security will reject the request if it requires authentication
            System.err.println("JWT validation error: " + e.getMessage());
        }

        // Step 10: Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
