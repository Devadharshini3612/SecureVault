package com.securevault.repository;

import com.securevault.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AuditLogRepository
 *
 * Repository interface for managing AuditLog entities.
 * Extends JpaRepository to provide basic CRUD operations and additional
 * query methods for audit log retrieval and analysis.
 *
 * This repository provides methods to:
 * 1. Save audit log entries (inherited from JpaRepository)
 * 2. Query audit logs by user, entity, action, or time period
 * 3. Support compliance and security monitoring requirements
 *
 * Spring Data JPA automatically implements all methods at runtime.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find all audit logs for a specific user
     * Useful for tracking all actions performed by a particular user
     *
     * @param performedBy the ID of the user
     * @return List of audit logs performed by the user, ordered by timestamp descending
     */
    List<AuditLog> findByPerformedByOrderByTimestampDesc(Long performedBy);

    /**
     * Find all audit logs for a specific entity
     * Useful for tracking the complete history of changes to a particular credential
     *
     * @param entityType the type of entity (e.g., "CREDENTIAL")
     * @param entityId the ID of the specific entity
     * @return List of audit logs for the entity, ordered by timestamp descending
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);

    /**
     * Find audit logs by action type
     * Useful for security monitoring (e.g., all DELETE operations)
     *
     * @param action the type of action (e.g., "DELETE", "CREATE", "UPDATE")
     * @return List of audit logs for the specific action, ordered by timestamp descending
     */
    List<AuditLog> findByActionOrderByTimestampDesc(String action);

    /**
     * Find audit logs within a specific time period
     * Useful for compliance reporting and security analysis
     *
     * @param startTime the start of the time period
     * @param endTime the end of the time period
     * @return List of audit logs within the time period, ordered by timestamp descending
     */
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find audit logs for a specific user and entity type
     * Useful for tracking what a user has done with specific types of entities
     *
     * @param performedBy the ID of the user
     * @param entityType the type of entity
     * @return List of audit logs, ordered by timestamp descending
     */
    List<AuditLog> findByPerformedByAndEntityTypeOrderByTimestampDesc(Long performedBy, String entityType);

    /**
     * Find recent audit logs (last N entries)
     * Custom query to limit results for dashboard or monitoring views
     *
     * @param limit the maximum number of records to return
     * @return List of most recent audit logs
     */
    @Query("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC LIMIT :limit")
    List<AuditLog> findRecentAuditLogs(@Param("limit") int limit);

    /**
     * Count audit logs by action type within a time period
     * Useful for generating statistics and reports
     *
     * @param action the type of action
     * @param startTime the start of the time period
     * @param endTime the end of the time period
     * @return count of audit logs matching the criteria
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = :action AND a.timestamp BETWEEN :startTime AND :endTime")
    Long countByActionAndTimestampBetween(@Param("action") String action, 
                                         @Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * Find suspicious activities - multiple actions by same user in short time
     * Useful for security monitoring and anomaly detection
     *
     * @param performedBy the ID of the user
     * @param startTime the start time for the analysis window
     * @param endTime the end time for the analysis window
     * @return List of audit logs that might indicate suspicious activity
     */
    @Query("SELECT a FROM AuditLog a WHERE a.performedBy = :performedBy AND a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp DESC")
    List<AuditLog> findUserActivityInTimeWindow(@Param("performedBy") Long performedBy,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * Batch fetch audit logs for multiple entities (optimization to prevent N+1 queries)
     * Useful when displaying audit history for multiple credentials at once
     *
     * @param entityType the type of entity
     * @param entityIds list of entity IDs to fetch logs for
     * @return List of audit logs for all specified entities
     */
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId IN :entityIds ORDER BY a.timestamp DESC")
    List<AuditLog> findByEntityTypeAndEntityIdIn(@Param("entityType") String entityType, 
                                                 @Param("entityIds") List<Long> entityIds);
}