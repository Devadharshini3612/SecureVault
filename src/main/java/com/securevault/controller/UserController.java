package com.securevault.controller;

import com.securevault.dto.ApiResponse;
import com.securevault.dto.LoginRequest;
import com.securevault.dto.LoginResponse;
import com.securevault.dto.RegisterRequest;
import com.securevault.dto.UserResponse;
import com.securevault.entity.User;
import com.securevault.exception.DuplicateEmailException;
import com.securevault.exception.InvalidCredentialsException;
import com.securevault.exception.UserNotFoundException;
import com.securevault.mapper.DtoEntityMapper;
import com.securevault.security.JwtService;
import com.securevault.service.UserService;
import com.securevault.service.SecurityMonitoringService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * UserController - Production-Ready Authentication API
 *
 * Fully refactored controller using:
 * - DTO pattern with validation annotations (@Valid)
 * - Standardized ApiResponse wrapper for all responses
 * - DtoEntityMapper for clean conversions
 * - Custom exceptions handled by GlobalExceptionHandler
 * - Consistent HTTP status codes and error messages
 *
 * Available endpoints:
 * - POST /api/auth/register - User registration with validation
 * - POST /api/auth/login - JWT-based authentication
 */
@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private DtoEntityMapper mapper;
    
    @Autowired
    private SecurityMonitoringService securityMonitoringService;

    /**
     * POST /api/auth/register
     * 
     * Register a new user with name, email, and password.
     * Password is automatically hashed with BCrypt.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        // Create User entity from RegisterRequest DTO
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword()); // Will be hashed in service

        // Delegate to the service layer
        String result = userService.registerUser(user);

        // Check the result and return the correct HTTP response
        if (result.equals("EMAIL_EXISTS")) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Email already registered"));
        }

        // Get the created user for response
        User createdUser = userService.findByEmail(request.getEmail());
        UserResponse userResponse = new UserResponse(
            createdUser.getUserId(),
            createdUser.getName(),
            createdUser.getEmail()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", userResponse));
    }

    /**
     * POST /api/auth/login
     * 
     * Login with email and password.
     * Returns JWT token on successful authentication.
     * Tracks all login attempts for security monitoring.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            // First verify credentials
            String result = userService.loginUser(request.getEmail(), request.getPassword());

            if ("SUCCESS".equals(result)) {
                // Get user details for JWT token
                User user = userService.findByEmail(request.getEmail());
                
                // Track successful login
                securityMonitoringService.trackSuccessfulLogin(request.getEmail(), httpRequest);
                
                // Generate JWT token
                String token = jwtService.generateToken(user.getEmail(), user.getUserId());
                
                // Create response with JWT token
                LoginResponse loginResponse = new LoginResponse(
                    token,
                    user.getEmail(),
                    user.getUserId(),
                    "Login successful"
                );
                
                return ResponseEntity.ok(
                    ApiResponse.success("Login successful", loginResponse)
                );
                
            } else {
                // Track failed login attempt
                String reason = "USER_NOT_FOUND".equals(result) ? "User not found" : 
                               "INVALID_PASSWORD".equals(result) ? "Invalid password" : 
                               "Unknown error";
                
                securityMonitoringService.trackFailedLogin(request.getEmail(), reason, httpRequest);
                
                if ("USER_NOT_FOUND".equals(result)) {
                    return ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("User not found"));
                } else if ("INVALID_PASSWORD".equals(result)) {
                    return ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body(ApiResponse.error("Invalid password"));
                } else {
                    return ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("An error occurred"));
                }
            }
            
        } catch (Exception e) {
            // Track failed login due to exception
            securityMonitoringService.trackFailedLogin(
                request.getEmail(), 
                "System error: " + e.getMessage(), 
                httpRequest
            );
            
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Login error: " + e.getMessage()));
        }
    }
    
    /**
     * POST /api/auth/logout
     * 
     * Logout endpoint - tracks logout event for security monitoring.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestParam String email,
            @RequestParam Long userId,
            HttpServletRequest httpRequest) {
        try {
            // Track logout event
            securityMonitoringService.trackLogout(userId, email, httpRequest);
            
            return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully", "Session ended")
            );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Logout error: " + e.getMessage()));
        }
    }
}