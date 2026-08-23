package com.securevault.dto;

import java.time.LocalDateTime;

/**
 * UserResponse DTO
 *
 * Data Transfer Object for returning user information in responses.
 * Used for registration success and user profile information.
 * 
 * SECURITY NOTE: 
 * - Never includes password hash or other sensitive data
 * - Only returns safe user information
 *
 * JSON Response Example:
 * {
 *   "userId": 5,
 *   "name": "John Doe",
 *   "email": "john@example.com",
 *   "registeredAt": "2026-07-15T10:30:00"
 * }
 */
public class UserResponse {

    /**
     * User's unique identifier
     */
    private Long userId;

    /**
     * User's full name
     */
    private String name;

    /**
     * User's email address
     */
    private String email;

    /**
     * When the user registered (optional field)
     */
    private LocalDateTime registeredAt;

    // ========== Constructors ==========

    public UserResponse() {
    }

    public UserResponse(Long userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public UserResponse(Long userId, String name, String email, LocalDateTime registeredAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.registeredAt = registeredAt;
    }

    // ========== Getters and Setters ==========

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}