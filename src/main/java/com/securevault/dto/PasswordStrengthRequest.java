package com.securevault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * PasswordStrengthRequest DTO
 *
 * Request object for analyzing password strength.
 */
public class PasswordStrengthRequest {

    /**
     * The password to analyze
     */
    @NotBlank(message = "Password is required for strength analysis")
    @Size(min = 1, max = 200, message = "Password must be between 1 and 200 characters")
    private String password;

    // ========== Constructors ==========

    public PasswordStrengthRequest() {
    }

    public PasswordStrengthRequest(String password) {
        this.password = password;
    }

    // ========== Getters and Setters ==========

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
