package com.securevault.dto;

import com.securevault.enums.Category;
import jakarta.validation.constraints.*;

/**
 * UpdateCredentialRequest DTO
 *
 * Data Transfer Object for updating an existing credential.
 * Users can update the service name, username, and/or password.
 *
 * JSON Request Example:
 * {
 *   "serviceName": "Gmail Personal",
 *   "username": "john.doe@gmail.com",
 *   "password": "NewSecurePassword456!",
 *   "category": "PERSONAL"
 * }
 *
 * All fields are optional - only provide the fields you want to update.
 * The password will be re-encrypted using AES-256 if provided.
 * 
 * Validation ensures that if fields are provided, they meet minimum quality standards.
 */
public class UpdateCredentialRequest {

    /**
     * Updated service name (optional)
     * If provided, must be between 1 and 100 characters
     */
    @Size(min = 1, max = 100, message = "Service name must be between 1 and 100 characters")
    private String serviceName;

    /**
     * Updated username (optional)
     * If provided, must be between 1 and 100 characters
     */
    @Size(min = 1, max = 100, message = "Username must be between 1 and 100 characters")
    private String username;

    /**
     * Updated password (optional)
     * 
     * If provided, the old encrypted password will be replaced
     * with a newly encrypted version of this password.
     * Must be between 1 and 200 characters if provided
     */
    @Size(min = 1, max = 200, message = "Password must be between 1 and 200 characters")
    private String password;

    /**
     * Updated category (optional)
     */
    private Category category;

    // ========== Constructors ==========

    public UpdateCredentialRequest() {
    }

    public UpdateCredentialRequest(String serviceName, String username, String password) {
        this.serviceName = serviceName;
        this.username = username;
        this.password = password;
    }

    public UpdateCredentialRequest(String serviceName, String username, String password, Category category) {
        this.serviceName = serviceName;
        this.username = username;
        this.password = password;
        this.category = category;
    }

    // ========== Getters and Setters ==========

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
