package com.securevault.service;

import com.securevault.entity.AuditLog;
import com.securevault.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AuditService
 *
 * Service layer for managing audit logging functionality.
 * This service provides methods to create and retrieve audit log entries
 * for tracking all operations performed on sensitive entities.
 *
 * Key responsibilities:
 * 1. Create audit log entries for credential operations (CREATE, UPDATE, DELETE)
 * 2. Ensure audit logs are saved within the same transaction as the main operation
 * 3. Provide methods to retrieve audit logs for compliance and monitoring
 * 4. Handle audit log creation failures gracefully
 *
 * Transaction Management:
 * - Uses REQUIRES_NEW propagation for critical audit entries that must be saved
 * - Uses REQUIRED propagation for normal audit entries that should rollback with main transaction
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Constructor injection - Spring automatically injects dependencies
     */
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // ========== Audit Log Creation Methods ==========

    /**
     * Creates an audit log entry within the same transaction as the calling method
     * If the main transaction rolls back, this audit log will also be rolled back.
     *
     * @param action the action performed (CREATE, UPDATE, DELETE)
     * @param entityType the type of entity affected (CREDENTIAL, USER)
     * @param entityId the ID of the affected entity
     * @param performedBy the ID of the user who performed the action
     * @return the created AuditLog entity
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AuditLog createAuditLog(String action, String entityType, Long entityId, Long performedBy) {
        AuditLog auditLog = new AuditLog(action, entityType, entityId, performedBy);
        return auditLogRepository.save(auditLog);
    }

    /**
     * Creates an audit log entry with additional details
     *
     * @param action the action performed
     * @param entityType the type of entity affected
     * @param entityId the ID of the affected entity
     * @param performedBy the ID of the user who performed the action
     * @param details additional details about the operation
     * @return the created AuditLog entity
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AuditLog createAuditLog(String action, String entityType, Long entityId, Long performedBy, String details) {
        AuditLog auditLog = new AuditLog(action, entityType, entityId, performedBy, details);
        return auditLogRepository.save(auditLog);
    }

    /**
     * Creates an audit log entry in a separate transaction (REQUIRES_NEW)
     * This audit log will be saved even if the main transaction rolls back.
     * Use this for critical security events that must always be logged.
     *
     * @param action the action performed
     * @param entityType the type of entity affected
     * @param entityId the ID of the affected entity
     * @param performedBy the ID of the user who performed the action
     * @return the created AuditLog entity
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog createCriticalAuditLog(String action, String entityType, Long entityId, Long performedBy) {
        AuditLog auditLog = new AuditLog(action, entityType, entityId, performedBy);
        return auditLogRepository.save(auditLog);
    }

    // ========== Convenience Methods for Credential Operations ==========

    /**
     * Creates an audit log for credential creation
     *
     * @param credentialId the ID of the created credential
     * @param userId the ID of the user who created the credential
     * @return the created AuditLog entity
     */
    public AuditLog logCredentialCreation(Long credentialId, Long userId) {
        return createAuditLog("CREATE", "CREDENTIAL", credentialId, userId, "Credential created successfully");
    }

    /**
     * Creates an audit log for credential update
     *
     * @param credentialId the ID of the updated credential
     * @param userId the ID of the user who updated the credential
     * @param details specific fields that were updated
     * @return the created AuditLog entity
     */
    public AuditLog logCredentialUpdate(Long credentialId, Long userId, String details) {
        return createAuditLog("UPDATE", "CREDENTIAL", credentialId, userId, details);
    }

    /**
     * Creates an audit log for credential deletion
     *
     * @param credentialId the ID of the deleted credential
     * @param userId the ID of the user who deleted the credential
     * @return the created AuditLog entity
     */
    public AuditLog logCredentialDeletion(Long credentialId, Long userId) {
        return createAuditLog("DELETE", "CREDENTIAL", credentialId, userId, "Credential deleted successfully");
    }

    /**
     * Creates an audit log for failed credential operations
     *
     * @param action the attempted action
     * @param credentialId the ID of the credential (if available)
     * @param userId the ID of the user who attempted the action
     * @param errorDetails details about the failure
     * @return the created AuditLog entity
     */
    public AuditLog logCredentialOperationFailure(String action, Long credentialId, Long userId, String errorDetails) {
        String details = "Operation failed: " + errorDetails;
        return createCriticalAuditLog(action + "_FAILED", "CREDENTIAL", credentialId, userId);
    }

    // ========== Audit Log Retrieval Methods ==========

    /**
     * Gets all audit logs for a specific user
     *
     * @param userId the ID of the user
     * @return List of audit logs performed by the user
     */
    public List<AuditLog> getAuditLogsForUser(Long userId) {
        return auditLogRepository.findByPerformedByOrderByTimestampDesc(userId);
    }

    /**
     * Gets audit trail for a specific credential
     *
     * @param credentialId the ID of the credential
     * @return List of audit logs for the credential
     */
    public List<AuditLog> getCredentialAuditTrail(Long credentialId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc("CREDENTIAL", credentialId);
    }

    /**
     * Gets recent audit logs for monitoring dashboard
     *
     * @param limit maximum number of logs to return
     * @return List of recent audit logs
     */
    public List<AuditLog> getRecentAuditLogs(int limit) {
        return auditLogRepository.findRecentAuditLogs(limit);
    }

    /**
     * Gets audit logs for a specific action type
     *
     * @param action the action type (CREATE, UPDATE, DELETE)
     * @return List of audit logs for the action
     */
    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action);
    }

    /**
     * Gets audit logs within a time period
     *
     * @param startTime start of the time period
     * @param endTime end of the time period
     * @return List of audit logs within the time period
     */
    public List<AuditLog> getAuditLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startTime, endTime);
    }

    // ========== Statistics and Monitoring Methods ==========

    /**
     * Counts operations by action type within a time period
     *
     * @param action the action type
     * @param startTime start of the time period
     * @param endTime end of the time period
     * @return count of operations
     */
    public Long countOperationsByAction(String action, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.countByActionAndTimestampBetween(action, startTime, endTime);
    }

    /**
     * Gets user activity within a specific time window
     * Useful for detecting suspicious behavior
     *
     * @param userId the ID of the user
     * @param startTime start of the time window
     * @param endTime end of the time window
     * @return List of audit logs for the user within the time window
     */
    public List<AuditLog> getUserActivityInTimeWindow(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.findUserActivityInTimeWindow(userId, startTime, endTime);
    }
}