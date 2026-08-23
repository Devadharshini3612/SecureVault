package com.securevault.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AuditLog Entity
 *
 * Tracks all operations performed on sensitive entities in the system.
 * This provides a complete audit trail for compliance and security monitoring.
 *
 * Each audit log entry records:
 * - What action was performed (CREATE, UPDATE, DELETE)
 * - On which entity type (CREDENTIAL, USER, etc.)
 * - The specific entity ID that was affected
 * - Who performed the action (user ID)
 * - When the action occurred (timestamp)
 *
 * Database table: audit_logs
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    /**
     * Primary key - auto-generated audit log ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    /**
     * The type of action that was performed
     * Examples: "CREATE", "UPDATE", "DELETE", "LOGIN", "LOGOUT"
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * The type of entity that was affected
     * Examples: "CREDENTIAL", "USER", "SESSION"
     */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /**
     * The ID of the specific entity that was affected
     * For example: if a credential with ID 123 was updated, this would be 123
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * The ID of the user who performed the action
     * This creates accountability and traceability for all operations
     */
    @Column(name = "performed_by", nullable = false)
    private Long performedBy;

    /**
     * Timestamp when the action was performed
     * Set automatically when the audit log entry is created
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Optional additional details about the action
     * Can store JSON or plain text with more context
     */
    @Column(name = "details", length = 500)
    private String details;

    /**
     * Automatically set timestamp before persisting to database
     */
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    // ========== Constructors ==========

    /**
     * Default constructor required by JPA
     */
    public AuditLog() {
    }

    /**
     * Constructor for creating a new audit log entry
     * 
     * @param action the type of action performed
     * @param entityType the type of entity affected
     * @param entityId the ID of the affected entity
     * @param performedBy the ID of the user who performed the action
     */
    public AuditLog(String action, String entityType, Long entityId, Long performedBy) {
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.performedBy = performedBy;
    }

    /**
     * Constructor with optional details
     * 
     * @param action the type of action performed
     * @param entityType the type of entity affected
     * @param entityId the ID of the affected entity
     * @param performedBy the ID of the user who performed the action
     * @param details additional details about the action
     */
    public AuditLog(String action, String entityType, Long entityId, Long performedBy, String details) {
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.performedBy = performedBy;
        this.details = details;
    }

    // ========== Getters and Setters ==========

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(Long performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "auditId=" + auditId +
                ", action='" + action + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", performedBy=" + performedBy +
                ", timestamp=" + timestamp +
                ", details='" + details + '\'' +
                '}';
    }
}