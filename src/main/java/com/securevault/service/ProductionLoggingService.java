package com.securevault.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Production Logging Service
 * 
 * Centralized logging service that provides structured, production-ready logging
 * for the SecureVault application. This service replaces System.out.println
 * statements with proper SLF4J logging and adds contextual information.
 * 
 * Features:
 * - Structured logging with consistent format
 * - Request correlation IDs for tracing
 * - Security event logging
 * - Performance metrics logging
 * - Error tracking with context
 * - Audit trail logging
 * 
 * Usage: Inject this service into controllers and services for centralized logging.
 */
@Service
public class ProductionLoggingService {

    private static final Logger logger = LoggerFactory.getLogger(ProductionLoggingService.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE");
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Logs application startup and initialization events
     */
    public void logApplicationStartup(String component, String message) {
        setCorrelationId();
        logger.info("[STARTUP] Component: {} | Message: {}", component, message);
        clearMDC();
    }

    /**
     * Logs user registration events
     */
    public void logUserRegistration(String email, boolean success, String reason) {
        setCorrelationId();
        if (success) {
            auditLogger.info("[USER_REGISTRATION] Email: {} | Status: SUCCESS | Timestamp: {}", 
                           maskEmail(email), getCurrentTimestamp());
        } else {
            auditLogger.warn("[USER_REGISTRATION] Email: {} | Status: FAILED | Reason: {} | Timestamp: {}", 
                           maskEmail(email), reason, getCurrentTimestamp());
        }
        clearMDC();
    }

    /**
     * Logs user authentication events
     */
    public void logUserAuthentication(String email, boolean success, String clientIp, String userAgent) {
        setCorrelationId();
        MDC.put("clientIp", clientIp);
        MDC.put("userAgent", userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 100)) : "unknown");
        
        if (success) {
            securityLogger.info("[AUTHENTICATION] Email: {} | Status: SUCCESS | IP: {} | Timestamp: {}", 
                              maskEmail(email), clientIp, getCurrentTimestamp());
        } else {
            securityLogger.warn("[AUTHENTICATION] Email: {} | Status: FAILED | IP: {} | Timestamp: {}", 
                              maskEmail(email), clientIp, getCurrentTimestamp());
        }
        clearMDC();
    }

    /**
     * Logs credential operations (create, read, update, delete)
     */
    public void logCredentialOperation(Long userId, String operation, Long credentialId, boolean success, String details) {
        setCorrelationId();
        MDC.put("userId", userId.toString());
        MDC.put("operation", operation);
        
        if (success) {
            auditLogger.info("[CREDENTIAL_{}] UserID: {} | CredentialID: {} | Status: SUCCESS | Details: {} | Timestamp: {}", 
                           operation, userId, credentialId, details, getCurrentTimestamp());
        } else {
            auditLogger.error("[CREDENTIAL_{}] UserID: {} | CredentialID: {} | Status: FAILED | Details: {} | Timestamp: {}", 
                            operation, userId, credentialId, details, getCurrentTimestamp());
        }
        clearMDC();
    }

    /**
     * Logs API request performance metrics
     */
    public void logApiPerformance(String endpoint, String method, long durationMs, int statusCode, Long userId) {
        setCorrelationId();
        MDC.put("endpoint", endpoint);
        MDC.put("method", method);
        MDC.put("duration", String.valueOf(durationMs));
        MDC.put("statusCode", String.valueOf(statusCode));
        
        if (userId != null) {
            MDC.put("userId", userId.toString());
        }
        
        if (durationMs > 5000) { // Log slow requests (>5s) as warnings
            performanceLogger.warn("[API_PERFORMANCE] {} {} | Duration: {}ms | Status: {} | UserID: {} | SLOW_REQUEST", 
                                 method, endpoint, durationMs, statusCode, userId);
        } else if (durationMs > 1000) { // Log medium requests (>1s) as info
            performanceLogger.info("[API_PERFORMANCE] {} {} | Duration: {}ms | Status: {} | UserID: {}", 
                                 method, endpoint, durationMs, statusCode, userId);
        } else { // Fast requests as debug
            performanceLogger.debug("[API_PERFORMANCE] {} {} | Duration: {}ms | Status: {} | UserID: {}", 
                                  method, endpoint, durationMs, statusCode, userId);
        }
        clearMDC();
    }

    /**
     * Logs security-related events (suspicious activity, failed attempts, etc.)
     */
    public void logSecurityEvent(String eventType, String email, String clientIp, String description, String severity) {
        setCorrelationId();
        MDC.put("eventType", eventType);
        MDC.put("clientIp", clientIp);
        MDC.put("severity", severity);
        
        switch (severity.toUpperCase()) {
            case "HIGH":
                securityLogger.error("[SECURITY_{}] Email: {} | IP: {} | Description: {} | Severity: {} | Timestamp: {}", 
                                   eventType, maskEmail(email), clientIp, description, severity, getCurrentTimestamp());
                break;
            case "MEDIUM":
                securityLogger.warn("[SECURITY_{}] Email: {} | IP: {} | Description: {} | Severity: {} | Timestamp: {}", 
                                  eventType, maskEmail(email), clientIp, description, severity, getCurrentTimestamp());
                break;
            default:
                securityLogger.info("[SECURITY_{}] Email: {} | IP: {} | Description: {} | Severity: {} | Timestamp: {}", 
                                  eventType, maskEmail(email), clientIp, description, severity, getCurrentTimestamp());
        }
        clearMDC();
    }

    /**
     * Logs database operations and performance
     */
    public void logDatabaseOperation(String operation, String table, long durationMs, boolean success, String error) {
        setCorrelationId();
        MDC.put("dbOperation", operation);
        MDC.put("table", table);
        MDC.put("duration", String.valueOf(durationMs));
        
        if (success) {
            if (durationMs > 1000) {
                performanceLogger.warn("[DATABASE] Operation: {} | Table: {} | Duration: {}ms | Status: SUCCESS | SLOW_QUERY", 
                                     operation, table, durationMs);
            } else {
                performanceLogger.debug("[DATABASE] Operation: {} | Table: {} | Duration: {}ms | Status: SUCCESS", 
                                      operation, table, durationMs);
            }
        } else {
            logger.error("[DATABASE] Operation: {} | Table: {} | Duration: {}ms | Status: FAILED | Error: {}", 
                        operation, table, durationMs, error);
        }
        clearMDC();
    }

    /**
     * Logs application errors with context
     */
    public void logApplicationError(String component, String operation, Exception exception, Map<String, Object> context) {
        setCorrelationId();
        MDC.put("component", component);
        MDC.put("operation", operation);
        
        // Add context information to MDC
        if (context != null) {
            context.forEach((key, value) -> {
                if (value != null) {
                    MDC.put("context_" + key, value.toString());
                }
            });
        }
        
        logger.error("[APPLICATION_ERROR] Component: {} | Operation: {} | Error: {} | Context: {} | Timestamp: {}", 
                    component, operation, exception.getMessage(), context, getCurrentTimestamp(), exception);
        clearMDC();
    }

    /**
     * Logs business logic events
     */
    public void logBusinessEvent(String eventType, String description, Map<String, Object> data) {
        setCorrelationId();
        MDC.put("eventType", eventType);
        
        logger.info("[BUSINESS_EVENT] Type: {} | Description: {} | Data: {} | Timestamp: {}", 
                   eventType, description, sanitizeData(data), getCurrentTimestamp());
        clearMDC();
    }

    /**
     * Logs system health and monitoring events
     */
    public void logSystemHealth(String metric, Object value, String status, String threshold) {
        setCorrelationId();
        MDC.put("metric", metric);
        MDC.put("value", value.toString());
        MDC.put("status", status);
        
        switch (status.toUpperCase()) {
            case "CRITICAL":
                logger.error("[SYSTEM_HEALTH] Metric: {} | Value: {} | Status: {} | Threshold: {} | Timestamp: {}", 
                           metric, value, status, threshold, getCurrentTimestamp());
                break;
            case "WARNING":
                logger.warn("[SYSTEM_HEALTH] Metric: {} | Value: {} | Status: {} | Threshold: {} | Timestamp: {}", 
                          metric, value, status, threshold, getCurrentTimestamp());
                break;
            default:
                logger.info("[SYSTEM_HEALTH] Metric: {} | Value: {} | Status: {} | Threshold: {} | Timestamp: {}", 
                          metric, value, status, threshold, getCurrentTimestamp());
        }
        clearMDC();
    }

    // Helper methods

    /**
     * Sets a correlation ID for request tracking
     */
    private void setCorrelationId() {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString().substring(0, 8);
            MDC.put("correlationId", correlationId);
        }
    }

    /**
     * Clears MDC context
     */
    private void clearMDC() {
        MDC.clear();
    }

    /**
     * Gets current timestamp in consistent format
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }

    /**
     * Masks sensitive email information for logging
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 3) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            String username = email.substring(0, atIndex);
            String domain = email.substring(atIndex);
            if (username.length() <= 2) {
                return "**" + domain;
            } else {
                return username.charAt(0) + "***" + username.charAt(username.length() - 1) + domain;
            }
        }
        return email.charAt(0) + "***" + email.charAt(email.length() - 1);
    }

    /**
     * Sanitizes data for logging (removes sensitive information)
     */
    private Map<String, Object> sanitizeData(Map<String, Object> data) {
        if (data == null) return null;
        
        Map<String, Object> sanitized = new java.util.HashMap<>();
        data.forEach((key, value) -> {
            String lowerKey = key.toLowerCase();
            if (lowerKey.contains("password") || lowerKey.contains("secret") || lowerKey.contains("token")) {
                sanitized.put(key, "***REDACTED***");
            } else {
                sanitized.put(key, value);
            }
        });
        return sanitized;
    }
}