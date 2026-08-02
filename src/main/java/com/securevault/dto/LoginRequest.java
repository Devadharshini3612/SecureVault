package com.securevault.dto;

import jakarta.validation.constraints.*;

/**
 * LoginRequest DTO (Data Transfer Object)
 * 
 * This class is used to receive login credentials from the API request.
 * It accepts email and plaintext password which will be verified against
 * the BCrypt hash stored in the database.
 * 
 * Bean Validation annotations ensure proper input format:
 * - @NotBlank: Field cannot be null, empty, or whitespace-only
 * - @Email: Validates proper email format
 */
public class LoginRequest {
    
    /**
     * User's email address
     * Must be a valid email format
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    /**
     * User's password
     * Must not be empty
     */
    @NotBlank(message = "Password is required")
    private String password;

    // Constructors
    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
