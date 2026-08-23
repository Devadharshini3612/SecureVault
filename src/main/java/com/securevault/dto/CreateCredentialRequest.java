package com.securevault.dto;

import com.securevault.enums.Category;
import jakarta.validation.constraints.*;

/**
 * CreateCredentialRequest DTO
 *
 * Data Transfer Object for creating a new credential.
 * This is used when a user wants to store a new password in the vault.
 *
 * JSON Request Example:
 * {
 *   "serviceName": "Gmail",
 *   "username": "john@gmail.com",
 *   "password": "MySecretPassword123!",
 *   "category": "PERSONAL"
 * }
 *
 * The password will be encrypted using AES-256 before storage.
 */
public class CreateCredentialRequest {

    /**
     * The ID of the user who owns this credential
     * Note: This is typically set by the controller from JWT token, not from request body
     */
    private Long userId;

    /**
     * The name of the service (e.g., "Gmail", "Netflix", "GitHub")
     * Required field, 1-100 characters
     */
    @NotBlank(message = "Service name is required")
    @Size(min = 1, max = 100, message = "Service name must be between 1 and 100 characters")
    private String serviceName;

    /**
     * The username or email for the service
     * Required field, 1-100 characters
     */
    @NotBlank(message = "Username is required")
    @Size(min = 1, max = 100, message = "Username must be between 1 and 100 characters")
    private String username;

    /**
     * The plaintext password to be encrypted and stored
     * 
     * IMPORTANT: This is sent as plaintext in the JSON request,
     * but will be encrypted using AES-256 before saving to the database.
     * Must be at least 1 character, maximum 200 for practical purposes
     */
    @NotBlank(message = "Password is required")
    @Size(min = 1, max = 200, message = "Password must be between 1 and 200 characters")
    private String password;

    /**
     * Category for organizing credentials (optional)
     * If not provided, defaults to OTHER in the entity
     */
    private Category category;

    // ========== Constructors ==========

    public CreateCredentialRequest() {
    }

    public CreateCredentialRequest(Long userId, String serviceName, String username, String password) {
        this.userId = userId;
        this.serviceName = serviceName;
        this.username = username;
        this.password = password;
    }

    // ========== Getters and Setters ==========

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
