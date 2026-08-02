package com.securevault.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * PasswordHistory Entity
 *
 * Tracks the history of password changes for each credential.
 * This enables password reuse prevention and audit trail of password changes.
 *
 * Security:
 * - Passwords are stored encrypted (AES-256-GCM)
 * - Version tracking for chronological history
 * - Prevents users from reusing recent passwords
 *
 * Database table: password_history
 */
@Entity
@Table(name = "password_history", indexes = {
    @Index(name = "idx_password_history_credential", columnList = "credential_id"),
    @Index(name = "idx_password_history_version", columnList = "credential_id, version")
})
public class PasswordHistory {

    /**
     * Primary key - auto-generated history ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    /**
     * Foreign key - references the credential this history belongs to
     */
    @Column(name = "credential_id", nullable = false)
    private Long credentialId;

    /**
     * The encrypted password at this point in history
     * Stored using AES-256-GCM encryption
     */
    @Column(name = "encrypted_password", nullable = false, length = 500)
    private String encryptedPassword;

    /**
     * Version number of this password
     * Starts at 1 and increments with each password change
     */
    @Column(name = "version", nullable = false)
    private Integer version;

    /**
     * Timestamp when this password was set
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Optional note about why the password was changed
     */
    @Column(name = "change_reason", length = 255)
    private String changeReason;

    /**
     * Automatically set timestamp before persisting
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ========== Constructors ==========

    public PasswordHistory() {
    }

    public PasswordHistory(Long credentialId, String encryptedPassword, Integer version) {
        this.credentialId = credentialId;
        this.encryptedPassword = encryptedPassword;
        this.version = version;
    }

    public PasswordHistory(Long credentialId, String encryptedPassword, Integer version, String changeReason) {
        this.credentialId = credentialId;
        this.encryptedPassword = encryptedPassword;
        this.version = version;
        this.changeReason = changeReason;
    }

    // ========== Getters and Setters ==========

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public Long getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(Long credentialId) {
        this.credentialId = credentialId;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    @Override
    public String toString() {
        return "PasswordHistory{" +
                "historyId=" + historyId +
                ", credentialId=" + credentialId +
                ", version=" + version +
                ", createdAt=" + createdAt +
                ", changeReason='" + changeReason + '\'' +
                '}';
    }
}
