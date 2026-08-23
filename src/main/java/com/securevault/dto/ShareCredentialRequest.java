package com.securevault.dto;

import com.securevault.enums.Permission;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * ShareCredentialRequest
 *
 * Request DTO for sharing a credential with another user.
 * 
 * UPDATED: Now accepts either sharedWithUserId OR recipientEmail
 * - If recipientEmail is provided, the service will look up the user by email
 * - If sharedWithUserId is provided, it will be used directly
 */
public class ShareCredentialRequest {

    @NotNull(message = "Credential ID is required")
    private Long credentialId;

    // Optional: Either provide user ID directly
    private Long sharedWithUserId;

    // Optional: Or provide recipient email (will be looked up)
    private String recipientEmail;

    @NotNull(message = "Permission is required")
    private Permission permission;

    private LocalDateTime expiresAt;

    // Constructors
    public ShareCredentialRequest() {
    }

    public ShareCredentialRequest(Long credentialId, Long sharedWithUserId, Permission permission) {
        this.credentialId = credentialId;
        this.sharedWithUserId = sharedWithUserId;
        this.permission = permission;
    }

    // Getters and Setters
    public Long getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(Long credentialId) {
        this.credentialId = credentialId;
    }

    public Long getSharedWithUserId() {
        return sharedWithUserId;
    }

    public void setSharedWithUserId(Long sharedWithUserId) {
        this.sharedWithUserId = sharedWithUserId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
