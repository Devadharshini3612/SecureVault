package com.securevault.entity;

import com.securevault.enums.Category;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Credential Entity
 *
 * Represents a secure credential stored in the vault.
 * Each credential belongs to a user and stores encrypted passwords
 * for various services (e.g., Gmail, Netflix, GitHub).
 *
 * Security:
 * - Passwords are NEVER stored in plaintext
 * - The encryptedPassword field contains AES-256 encrypted data
 * - Only the user who created the credential can access it
 *
 * Database table: credentials
 */
@Entity
@Table(name = "credentials")
public class Credential {

    /**
     * Primary key - auto-generated credential ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credential_id")
    private Long credentialId;

    /**
     * Foreign key - references the user who owns this credential
     * This creates a many-to-one relationship: many credentials belong to one user
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * The name of the service this credential is for
     * Examples: "Gmail", "Netflix", "GitHub", "AWS Console"
     */
    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    /**
     * The username or email used for this service
     * Examples: "john@gmail.com", "john_doe", "john.smith"
     */
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    /**
     * The AES-256 encrypted password
     *
     * SECURITY NOTE:
     * - This field NEVER contains plaintext passwords
     * - Data is encrypted using AES-256-GCM before storage
     * - Encryption/decryption is handled by AESUtil class
     * - The encrypted data is stored as Base64-encoded string
     *
     * Example encrypted value:
     * "xJ4K9mP2fR8sT1vW5yH7jL0nQ3uE6wI9z..."
     */
    @Column(name = "encrypted_password", nullable = false, length = 500)
    private String encryptedPassword;

    /**
     * Category of the credential for organization and filtering
     * Examples: PERSONAL, WORK, BANKING, DEVELOPMENT, etc.
     * Defaults to OTHER if not specified.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = true, length = 20)
    private Category category = Category.OTHER;

    /**
     * Timestamp when the credential was created
     * Set automatically on first save
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the credential was last updated
     * Updated automatically on every save
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Automatically set timestamps before persisting to database
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Automatically update the updatedAt timestamp before updating
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== Constructors ==========

    /**
     * Default constructor required by JPA
     */
    public Credential() {
    }

    /**
     * Constructor for creating a new credential
     * 
     * @param userId the ID of the user who owns this credential
     * @param serviceName the name of the service
     * @param username the username for the service
     * @param encryptedPassword the AES-encrypted password
     */
    public Credential(Long userId, String serviceName, String username, String encryptedPassword) {
        this.userId = userId;
        this.serviceName = serviceName;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
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

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category != null ? category : Category.OTHER;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
