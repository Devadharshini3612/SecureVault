package com.securevault.entity;

import com.securevault.enums.Permission;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * CredentialShare Entity
 *
 * Represents a shared credential between users with specific permissions.
 *
 * Relationships:
 * - Owner (User who shares the credential)
 * - Shared With User (User who receives the shared credential)
 * - Credential (The credential being shared)
 *
 * Business Rules:
 * - Only the owner can share credentials
 * - Cannot share with yourself
 * - Cannot share the same credential twice with the same user
 * - Soft-deleted credentials cannot be shared
 * - Revoked shares immediately lose access
 */
@Entity
@Table(name = "credential_shares",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"credential_id", "shared_with_user_id"},
           name = "uk_credential_shared_with_user"
       ))
public class CredentialShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "share_id")
    private Long shareId;

    @Column(name = "credential_id", nullable = false)
    private Long credentialId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "shared_with_user_id", nullable = false)
    private Long sharedWithUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Permission permission;

    @Column(name = "shared_at", nullable = false)
    private LocalDateTime sharedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public CredentialShare() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.sharedAt = LocalDateTime.now();
    }

    public CredentialShare(Long credentialId, Long ownerId, Long sharedWithUserId, Permission permission) {
        this();
        this.credentialId = credentialId;
        this.ownerId = ownerId;
        this.sharedWithUserId = sharedWithUserId;
        this.permission = permission;
    }

    // Getters and Setters
    public Long getShareId() {
        return shareId;
    }

    public void setShareId(Long shareId) {
        this.shareId = shareId;
    }

    public Long getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(Long credentialId) {
        this.credentialId = credentialId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getSharedWithUserId() {
        return sharedWithUserId;
    }

    public void setSharedWithUserId(Long sharedWithUserId) {
        this.sharedWithUserId = sharedWithUserId;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(LocalDateTime sharedAt) {
        this.sharedAt = sharedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            this.revokedAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Check if the share has expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if the share is valid (active and not expired)
     */
    public boolean isValid() {
        return active && !isExpired();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
