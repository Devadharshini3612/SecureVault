package com.securevault.dto;

import com.securevault.enums.Permission;
import java.time.LocalDateTime;

/**
 * ShareResponse
 *
 * Response DTO for credential share operations.
 */
public class ShareResponse {

    private Long shareId;
    private Long credentialId;
    private String serviceName;
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private Long sharedWithUserId;
    private String sharedWithUserName;
    private String sharedWithUserEmail;
    private Permission permission;
    private LocalDateTime sharedAt;
    private LocalDateTime expiresAt;
    private boolean active;
    private LocalDateTime revokedAt;

    // Constructors
    public ShareResponse() {
    }

    public ShareResponse(Long shareId, Long credentialId, String serviceName,
                        Long ownerId, String ownerName, String ownerEmail,
                        Long sharedWithUserId, String sharedWithUserName, String sharedWithUserEmail,
                        Permission permission, LocalDateTime sharedAt) {
        this.shareId = shareId;
        this.credentialId = credentialId;
        this.serviceName = serviceName;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.sharedWithUserId = sharedWithUserId;
        this.sharedWithUserName = sharedWithUserName;
        this.sharedWithUserEmail = sharedWithUserEmail;
        this.permission = permission;
        this.sharedAt = sharedAt;
        this.active = true;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public Long getSharedWithUserId() {
        return sharedWithUserId;
    }

    public void setSharedWithUserId(Long sharedWithUserId) {
        this.sharedWithUserId = sharedWithUserId;
    }

    public String getSharedWithUserName() {
        return sharedWithUserName;
    }

    public void setSharedWithUserName(String sharedWithUserName) {
        this.sharedWithUserName = sharedWithUserName;
    }

    public String getSharedWithUserEmail() {
        return sharedWithUserEmail;
    }

    public void setSharedWithUserEmail(String sharedWithUserEmail) {
        this.sharedWithUserEmail = sharedWithUserEmail;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
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
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}
