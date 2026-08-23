package com.securevault.service;

import com.securevault.entity.SecurityEvent;
import com.securevault.enums.RiskLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * SecurityAlertService
 * 
 * Service for generating and managing security alerts.
 * Sends notifications when suspicious activities or security threats are detected.
 */
@Service
public class SecurityAlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityAlertService.class);
    
    private final AsyncNotificationService asyncNotificationService;
    
    public SecurityAlertService(AsyncNotificationService asyncNotificationService) {
        this.asyncNotificationService = asyncNotificationService;
    }
    
    /**
     * Generate alert for new device login
     */
    public void generateNewDeviceAlert(Long userId, SecurityEvent event) {
        logger.warn("SECURITY ALERT: New device login detected for user ID: {}", userId);
        
        String alertMessage = String.format(
            "New Device Login Alert\n" +
            "User ID: %d\n" +
            "IP Address: %s\n" +
            "Device: %s\n" +
            "Location: %s\n" +
            "Time: %s\n" +
            "Risk Level: %s\n\n" +
            "If this was not you, please secure your account immediately.",
            userId,
            event.getIpAddress(),
            event.getUserAgent(),
            event.getLocation(),
            event.getTimestamp(),
            event.getRiskLevel()
        );
        
        // Send async notification (email, SMS, push notification, etc.)
        asyncNotificationService.sendSecurityAlert(String.valueOf(userId), "New Device Login", alertMessage);
        
        logger.info("New device alert generated for user ID: {}", userId);
    }
    
    /**
     * Generate alert for brute force attack
     */
    public void generateBruteForceAlert(Long userId, String email, SecurityEvent event) {
        logger.error("SECURITY ALERT: Brute force attack detected for email: {}", email);
        
        String alertMessage = String.format(
            "CRITICAL: Brute Force Attack Detected\n" +
            "Email: %s\n" +
            "IP Address: %s\n" +
            "Failed Attempts: %d\n" +
            "Time: %s\n" +
            "Risk Level: CRITICAL\n\n" +
            "Your account has been temporarily locked for security. " +
            "Please contact support if you need assistance.",
            email,
            event.getIpAddress(),
            event.getFailedAttemptCount(),
            event.getTimestamp()
        );
        
        // Send high-priority alert
        if (userId != null) {
            asyncNotificationService.sendSecurityAlert(String.valueOf(userId), "CRITICAL: Brute Force Attack", alertMessage);
        }
        
        // Log to security monitoring system
        logger.error("Brute force alert generated. Email: {}, IP: {}, Failed Attempts: {}", 
                email, event.getIpAddress(), event.getFailedAttemptCount());
    }
    
    /**
     * Generate alert for repeated login failures
     */
    public void generateRepeatedFailureAlert(Long userId, String email, SecurityEvent event) {
        logger.warn("SECURITY ALERT: Multiple failed login attempts for email: {}", email);
        
        String alertMessage = String.format(
            "Multiple Failed Login Attempts\n" +
            "Email: %s\n" +
            "IP Address: %s\n" +
            "Failed Attempts: %d\n" +
            "Time: %s\n" +
            "Risk Level: %s\n\n" +
            "If this was not you, your account may be under attack. " +
            "Consider changing your password immediately.",
            email,
            event.getIpAddress(),
            event.getFailedAttemptCount(),
            event.getTimestamp(),
            event.getRiskLevel()
        );
        
        if (userId != null) {
            asyncNotificationService.sendSecurityAlert(String.valueOf(userId), "Failed Login Attempts", alertMessage);
        }
        
        logger.info("Repeated failure alert generated for email: {}", email);
    }
    
    /**
     * Generate alert for suspicious activity
     */
    public void generateSuspiciousActivityAlert(Long userId, String email, SecurityEvent event) {
        logger.warn("SECURITY ALERT: Suspicious activity detected for user: {}", email);
        
        String alertMessage = String.format(
            "Suspicious Activity Detected\n" +
            "Event Type: %s\n" +
            "Email: %s\n" +
            "IP Address: %s\n" +
            "Details: %s\n" +
            "Time: %s\n" +
            "Risk Level: %s\n\n" +
            "We detected unusual activity on your account. " +
            "Please review your recent activity and secure your account if necessary.",
            event.getEventType(),
            email,
            event.getIpAddress(),
            event.getDetails(),
            event.getTimestamp(),
            event.getRiskLevel()
        );
        
        if (userId != null) {
            asyncNotificationService.sendSecurityAlert(String.valueOf(userId), "Suspicious Activity", alertMessage);
        }
        
        logger.info("Suspicious activity alert generated for user: {}", email);
    }
    
    /**
     * Generate alert for account lockout
     */
    public void generateAccountLockoutAlert(Long userId, String email, String reason) {
        logger.warn("SECURITY ALERT: Account locked for user: {}", email);
        
        String alertMessage = String.format(
            "Account Locked\n" +
            "Your account has been temporarily locked for security reasons.\n\n" +
            "Reason: %s\n" +
            "Email: %s\n" +
            "Time: %s\n\n" +
            "To unlock your account, please contact support or wait for the lockout period to expire.",
            reason,
            email,
            java.time.LocalDateTime.now()
        );
        
        if (userId != null) {
            asyncNotificationService.sendSecurityAlert(String.valueOf(userId), "Account Locked", alertMessage);
        }
        
        logger.info("Account lockout alert generated for user: {}", email);
    }
    
    /**
     * Generate alert for unusual login location
     */
    public void generateUnusualLocationAlert(Long userId, String email, SecurityEvent event) {
        logger.warn("SECURITY ALERT: Login from unusual location for user: {}", email);
        
        String alertMessage = String.format(
            "Login from Unusual Location\n" +
            "We detected a login from a location you don't usually sign in from.\n\n" +
            "Location: %s\n" +
            "IP Address: %s\n" +
            "Device: %s\n" +
            "Time: %s\n\n" +
            "If this was you, you can safely ignore this message. " +
            "If not, please secure your account immediately.",
            event.getLocation(),
            event.getIpAddress(),
            event.getUserAgent(),
            event.getTimestamp()
        );
        
        if (userId != null) {
            asyncNotificationService.sendSecurityAlert(String.valueOf(userId), "Unusual Login Location", alertMessage);
        }
        
        logger.info("Unusual location alert generated for user: {}", email);
    }
    
    /**
     * Generate generic security alert
     */
    public void generateGenericAlert(Long userId, String title, String message, RiskLevel riskLevel) {
        logger.warn("SECURITY ALERT: {} (Risk: {}) for user ID: {}", title, riskLevel, userId);
        
        String fullMessage = String.format(
            "%s\n\n%s\n\nRisk Level: %s\nTime: %s",
            title,
            message,
            riskLevel,
            java.time.LocalDateTime.now()
        );
        
        if (userId != null) {
            asyncNotificationService.sendSecurityAlert(String.valueOf(userId), title, fullMessage);
        }
        
        logger.info("Generic security alert generated: {}", title);
    }
}
