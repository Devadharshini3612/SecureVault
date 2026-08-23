package com.securevault.repository;

import com.securevault.entity.SecurityEvent;
import com.securevault.enums.RiskLevel;
import com.securevault.enums.SecurityEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SecurityEventRepository
 * 
 * Repository for managing security events.
 * Provides queries for tracking login attempts, suspicious activities, and security monitoring.
 */
@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {
    
    /**
     * Find all events for a specific user
     */
    List<SecurityEvent> findByUserIdOrderByTimestampDesc(Long userId);
    
    /**
     * Find all events for a specific email (even if user doesn't exist)
     */
    List<SecurityEvent> findByEmailOrderByTimestampDesc(String email);
    
    /**
     * Find recent events for a user (last N days)
     */
    @Query("SELECT se FROM SecurityEvent se WHERE se.userId = :userId AND se.timestamp >= :since ORDER BY se.timestamp DESC")
    List<SecurityEvent> findRecentEventsByUserId(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    /**
     * Count failed login attempts for a user/email within a time window
     */
    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.email = :email AND se.eventType = :eventType AND se.success = false AND se.timestamp >= :since")
    Long countFailedLoginAttempts(@Param("email") String email, @Param("eventType") SecurityEventType eventType, @Param("since") LocalDateTime since);
    
    /**
     * Find failed login attempts within a time window
     */
    @Query("SELECT se FROM SecurityEvent se WHERE se.email = :email AND se.eventType = 'LOGIN_FAILURE' AND se.timestamp >= :since ORDER BY se.timestamp DESC")
    List<SecurityEvent> findRecentFailedLogins(@Param("email") String email, @Param("since") LocalDateTime since);
    
    /**
     * Check if device has been used before by user
     */
    @Query("SELECT CASE WHEN COUNT(se) > 0 THEN true ELSE false END FROM SecurityEvent se WHERE se.userId = :userId AND se.deviceFingerprint = :deviceFingerprint")
    Boolean hasDeviceBeenUsedByUser(@Param("userId") Long userId, @Param("deviceFingerprint") String deviceFingerprint);
    
    /**
     * Find all events from a specific IP address
     */
    List<SecurityEvent> findByIpAddressOrderByTimestampDesc(String ipAddress);
    
    /**
     * Find events by risk level
     */
    List<SecurityEvent> findByRiskLevelOrderByTimestampDesc(RiskLevel riskLevel);
    
    /**
     * Find high-risk events within a time period
     */
    @Query("SELECT se FROM SecurityEvent se WHERE se.riskLevel IN ('HIGH', 'CRITICAL') AND se.timestamp >= :since ORDER BY se.timestamp DESC")
    List<SecurityEvent> findHighRiskEvents(@Param("since") LocalDateTime since);
    
    /**
     * Find events that generated alerts
     */
    @Query("SELECT se FROM SecurityEvent se WHERE se.alertGenerated = true ORDER BY se.timestamp DESC")
    List<SecurityEvent> findEventsWithAlerts();
    
    /**
     * Find new device logins for a user
     */
    @Query("SELECT se FROM SecurityEvent se WHERE se.userId = :userId AND se.isNewDevice = true ORDER BY se.timestamp DESC")
    List<SecurityEvent> findNewDeviceLogins(@Param("userId") Long userId);
    
    /**
     * Find suspicious events (high/critical risk) for a user
     */
    @Query("SELECT se FROM SecurityEvent se WHERE se.userId = :userId AND se.riskLevel IN ('HIGH', 'CRITICAL') ORDER BY se.timestamp DESC")
    List<SecurityEvent> findSuspiciousEventsForUser(@Param("userId") Long userId);
    
    /**
     * Count events by type within a time period
     */
    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.eventType = :eventType AND se.timestamp >= :since")
    Long countEventsByType(@Param("eventType") SecurityEventType eventType, @Param("since") LocalDateTime since);
    
    /**
     * Find most recent successful login for a user
     */
    @Query("SELECT se FROM SecurityEvent se WHERE se.userId = :userId AND se.eventType = 'LOGIN_SUCCESS' ORDER BY se.timestamp DESC")
    Optional<SecurityEvent> findMostRecentSuccessfulLogin(@Param("userId") Long userId);
    
    /**
     * Count total failed login attempts in the system within a time period
     */
    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.success = false AND se.timestamp >= :since")
    Long countTotalFailedLogins(@Param("since") LocalDateTime since);
    
    /**
     * Find events by type within a time period
     */
    @Query("SELECT se FROM SecurityEvent se WHERE se.eventType = :eventType AND se.timestamp >= :since ORDER BY se.timestamp DESC")
    List<SecurityEvent> findEventsByTypeAndTimeRange(@Param("eventType") SecurityEventType eventType, @Param("since") LocalDateTime since);
    
    /**
     * Get login activity summary for a user (last 30 days)
     */
    @Query("SELECT se FROM SecurityEvent se WHERE se.userId = :userId AND se.eventType IN ('LOGIN_SUCCESS', 'LOGIN_FAILURE') AND se.timestamp >= :since ORDER BY se.timestamp DESC")
    List<SecurityEvent> findLoginActivityForUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
