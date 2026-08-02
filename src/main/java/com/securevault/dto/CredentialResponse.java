package com.securevault.dto;

import java.time.LocalDateTime;

/**
 * CredentialResponse DTO
 *
 * Data Transfer Object for returning credential information to the client.
 * This is used when reading or listing credentials.
 *
 * IMPORTANT SECURITY NOTE:
 * - The password field contains the DECRYPTED plaintext password
 * - It's decrypted from the database using AES-256 before being sent in the response
 * - Only the credential owner can retrieve and decrypt their passwords
 *
 * JSON Response Example:
 * {
 *   "credentialId": 1,
 *   "userId": 3,
 *   "serviceName": "Gmail",
 *   "username": "john@gmail.com",
 *   "password": "MySecretPassword123!",
 *   "createdAt": "2026-07-10T15:30:00",
 *   "updatedAt": "2026-07-10T15:30:00"
 * }
 */
public class CredentialResponse {

    /**
     * The credential ID
     */
    private Long credentialId;

    /**
     * The user ID who owns this credential
     */
    private Long userId;

    /**
     * The service name
     */
    private String serviceName;

    /**
     * The username for the service
     */
    private String username;

    /**
     * The DECRYPTED password
     * 
     * This field contains the plaintext password that was
     * decrypted from the encrypted_password column in the database.
     */
    private String password;

    /**
     * When the credential was created
     */
    private LocalDateTime createdAt;

    /**
     * When the credential was last updated
     */
    private LocalDateTime updatedAt;

    // ========== Constructors ==========

    public CredentialResponse() {
    }

    public CredentialResponse(Long credentialId, Long userId, String serviceName, 
                              String username, String password, 
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.credentialId = credentialId;
        this.userId = userId;
        this.serviceName = serviceName;
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ========== Getters and Setters ==========

    public Long getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(Long credentialId) {
        this.credentialId = credentialId;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
