package com.securevault.dto;

import jakarta.validation.constraints.*;

/**
 * RegisterRequest DTO (Data Transfer Object)
 * 
 * This class is used to receive registration data from the API request.
 * It accepts the plaintext password from JSON and we'll hash it before
 * saving to the database.
 * 
 * Bean Validation annotations ensure data quality:
 * - @NotBlank: Field cannot be null, empty, or whitespace-only
 * - @Email: Validates proper email format
 * - @Size: Enforces minimum/maximum length constraints
 */
public class RegisterRequest {
    
    /**
     * User's full name
     * Must be between 2 and 50 characters
     */
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    /**
     * User's email address
     * Must be a valid email format and between 5 and 100 characters
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(min = 5, max = 100, message = "Email must be between 5 and 100 characters")
    private String email;

    /**
     * User's password
     * Must be at least 8 characters for security
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    // Constructors
    public RegisterRequest() {
    }

    public RegisterRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
