package com.securevault.dto;

/**
 * LoginResponse DTO
 *
 * Response returned when a user successfully logs in.
 * Contains the JWT token that the client must include in future requests.
 *
 * JSON Response Example:
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "email": "john@example.com",
 *   "userId": 3,
 *   "message": "Login successful"
 * }
 */
public class LoginResponse {

    /**
     * JWT token - must be included in Authorization header for protected endpoints
     */
    private String token;

    /**
     * User's email address
     */
    private String email;

    /**
     * User's ID
     */
    private Long userId;

    /**
     * Success message
     */
    private String message;

    // ========== Constructors ==========

    public LoginResponse() {
    }

    public LoginResponse(String token, String email, Long userId, String message) {
        this.token = token;
        this.email = email;
        this.userId = userId;
        this.message = message;
    }

    // ========== Getters and Setters ==========

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
