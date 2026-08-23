package com.securevault.entity;

import com.securevault.enums.TwoFactorMethod;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * TwoFactorAuth Entity
 * 
 * Stores 2FA configuration and verification codes for users.
 * Supports multiple 2FA methods: SMS, Email, Authenticator App
 */
@Entity
@Table(name = "two_factor_auth")
public class TwoFactorAuth {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private TwoFactorMethod method;
    
    @Column(name = "secret_key", length = 32)
    private String secretKey; // For Authenticator apps (TOTP)
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber; // For SMS
    
    @Column(name = "backup_email", length = 100)
    private String backupEmail; // For email codes
    
    @Column(name = "verification_code", length = 10)
    private String verificationCode;
    
    @Column(name = "code_expires_at")
    private LocalDateTime codeExpiresAt;
    
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = false;
    
    @Column(name = "backup_codes", length = 500)
    private String backupCodes; // JSON array of backup codes
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public TwoFactorAuth() {
        this.createdAt = LocalDateTime.now();
    }
    
    public TwoFactorAuth(Long userId, TwoFactorMethod method) {
        this();
        this.userId = userId;
        this.method = method;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public TwoFactorMethod getMethod() { return method; }
    public void setMethod(TwoFactorMethod method) { this.method = method; }
    
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getBackupEmail() { return backupEmail; }
    public void setBackupEmail(String backupEmail) { this.backupEmail = backupEmail; }
    
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    
    public LocalDateTime getCodeExpiresAt() { return codeExpiresAt; }
    public void setCodeExpiresAt(LocalDateTime codeExpiresAt) { this.codeExpiresAt = codeExpiresAt; }
    
    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
    
    public String getBackupCodes() { return backupCodes; }
    public void setBackupCodes(String backupCodes) { this.backupCodes = backupCodes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if verification code is still valid
     */
    public boolean isCodeValid() {
        return verificationCode != null && 
               codeExpiresAt != null && 
               codeExpiresAt.isAfter(LocalDateTime.now());
    }
    
    /**
     * Check if 2FA method requires a phone number
     */
    public boolean requiresPhoneNumber() {
        return method == TwoFactorMethod.SMS;
    }
    
    /**
     * Check if 2FA method requires an email
     */
    public boolean requiresEmail() {
        return method == TwoFactorMethod.EMAIL;
    }
    
    /**
     * Check if 2FA method uses authenticator app
     */
    public boolean isAuthenticatorApp() {
        return method == TwoFactorMethod.AUTHENTICATOR;
    }
}