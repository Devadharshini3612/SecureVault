package com.securevault.controller;

import com.securevault.dto.ApiResponse;
import com.securevault.dto.LoginActivityReport;
import com.securevault.dto.PasswordHealthReport;
import com.securevault.dto.SecuritySummary;
import com.securevault.entity.AuditLog;
import com.securevault.repository.AuditLogRepository;
import com.securevault.service.AuditAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AuditController
 * 
 * REST API for audit logs and analytics reports.
 * Provides endpoints for audit history, password health, login activity, and security summaries.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {
    
    private final AuditLogRepository auditLogRepository;
    private final AuditAnalyticsService auditAnalyticsService;
    
    public AuditController(
            AuditLogRepository auditLogRepository,
            AuditAnalyticsService auditAnalyticsService) {
        this.auditLogRepository = auditLogRepository;
        this.auditAnalyticsService = auditAnalyticsService;
    }
    
    /**
     * GET /api/audit/logs/{userId}
     * 
     * Get all audit logs for a user.
     * 
     * @param userId User ID
     * @return List of audit logs
     */
    @GetMapping("/logs/{userId}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs(@PathVariable Long userId) {
        List<AuditLog> logs = auditLogRepository.findByPerformedByOrderByTimestampDesc(userId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Audit logs retrieved successfully", logs)
        );
    }
    
    /**
     * GET /api/audit/logs/entity/{entityType}/{entityId}
     * 
     * Get audit logs for a specific entity.
     * 
     * @param entityType Type of entity (CREDENTIAL, USER, etc.)
     * @param entityId Entity ID
     * @return List of audit logs
     */
    @GetMapping("/logs/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        
        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(
                entityType, entityId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Entity audit logs retrieved", logs)
        );
    }
    
    /**
     * GET /api/audit/reports/password-health/{userId}
     * 
     * Generate password health report for a user.
     * 
     * @param userId User ID
     * @return Password health report
     */
    @GetMapping("/reports/password-health/{userId}")
    public ResponseEntity<ApiResponse<PasswordHealthReport>> getPasswordHealthReport(
            @PathVariable Long userId) {
        
        PasswordHealthReport report = auditAnalyticsService.generatePasswordHealthReport(userId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Password health report generated", report)
        );
    }
    
    /**
     * GET /api/audit/reports/login-activity/{userId}
     * 
     * Generate login activity report for a user.
     * 
     * @param userId User ID
     * @param days Number of days to analyze (default: 30)
     * @return Login activity report
     */
    @GetMapping("/reports/login-activity/{userId}")
    public ResponseEntity<ApiResponse<LoginActivityReport>> getLoginActivityReport(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        
        LoginActivityReport report = auditAnalyticsService.generateLoginActivityReport(userId, days);
        
        return ResponseEntity.ok(
            ApiResponse.success("Login activity report generated", report)
        );
    }
    
    /**
     * GET /api/audit/reports/security-summary/{userId}
     * 
     * Generate comprehensive security summary for a user.
     * 
     * @param userId User ID
     * @param days Number of days to analyze (default: 30)
     * @return Security summary
     */
    @GetMapping("/reports/security-summary/{userId}")
    public ResponseEntity<ApiResponse<SecuritySummary>> getSecuritySummary(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        
        SecuritySummary summary = auditAnalyticsService.generateSecuritySummary(userId, days);
        
        return ResponseEntity.ok(
            ApiResponse.success("Security summary generated", summary)
        );
    }
    
    /**
     * GET /api/audit/logs/action/{action}
     * 
     * Get audit logs by action type.
     * 
     * @param action Action type (CREATE, UPDATE, DELETE, LOGIN, LOGOUT, SHARE)
     * @return List of audit logs
     */
    @GetMapping("/logs/action/{action}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogsByAction(
            @PathVariable String action) {
        
        List<AuditLog> logs = auditLogRepository.findByActionOrderByTimestampDesc(action);
        
        return ResponseEntity.ok(
            ApiResponse.success("Audit logs for action '" + action + "' retrieved", logs)
        );
    }
    
    /**
     * GET /api/audit/logs/recent
     * 
     * Get recent audit logs across all users (admin view).
     * 
     * @param limit Maximum number of logs to return (default: 50)
     * @return List of recent audit logs
     */
    @GetMapping("/logs/recent")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getRecentAuditLogs(
            @RequestParam(defaultValue = "50") int limit) {
        
        List<AuditLog> logs = auditLogRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("Recent audit logs retrieved", logs)
        );
    }
}
