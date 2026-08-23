package com.securevault.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * AsyncNotificationService
 *
 * Service for handling asynchronous notifications and background tasks.
 * Operations in this service run in separate threads and don't block the main request.
 *
 * All methods are annotated with @Async and use the custom thread pool executor.
 */
@Service
public class AsyncNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncNotificationService.class);

    /**
     * Send email notification asynchronously
     * Simulates email sending without blocking the main thread
     *
     * @param toEmail recipient email address
     * @param subject email subject
     * @param body email body
     */
    @Async("taskExecutor")
    public void sendEmailNotification(String toEmail, String subject, String body) {
        String threadName = Thread.currentThread().getName();
        logger.info("[{}] Sending email to: {} | Subject: {}", threadName, toEmail, subject);
        
        try {
            // Simulate email sending delay
            Thread.sleep(2000);
            logger.info("[{}] Email sent successfully to: {}", threadName, toEmail);
        } catch (InterruptedException e) {
            logger.error("[{}] Email sending interrupted: {}", threadName, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Log user activity asynchronously
     * Non-blocking activity logging for analytics
     *
     * @param userId the user ID
     * @param action the action performed
     * @param details additional details
     */
    @Async("taskExecutor")
    public void logActivity(Long userId, String action, String details) {
        String threadName = Thread.currentThread().getName();
        logger.info("[{}] Activity Log | User: {} | Action: {} | Details: {}", 
                   threadName, userId, action, details);
        
        // In production, this would write to a separate analytics database or service
        try {
            Thread.sleep(500); // Simulate processing delay
            logger.debug("[{}] Activity logged successfully", threadName);
        } catch (InterruptedException e) {
            logger.error("[{}] Activity logging interrupted: {}", threadName, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Send password change notification
     *
     * @param userEmail user's email
     * @param credentialName name of the credential that was changed
     */
    @Async("taskExecutor")
    public void sendPasswordChangeNotification(String userEmail, String credentialName) {
        String threadName = Thread.currentThread().getName();
        logger.info("[{}] Sending password change notification for: {}", threadName, credentialName);
        
        String subject = "Password Changed - " + credentialName;
        String body = String.format(
            "Your password for %s has been changed. " +
            "If you did not make this change, please contact support immediately.",
            credentialName
        );
        
        sendEmailNotification(userEmail, subject, body);
    }

    /**
     * Process password strength analysis in background
     * Useful for batch analysis of all user passwords
     *
     * @param credentialId the credential ID to analyze
     */
    @Async("lowPriorityExecutor")
    public void analyzePasswordStrengthAsync(Long credentialId) {
        String threadName = Thread.currentThread().getName();
        logger.info("[{}] Analyzing password strength for credential: {}", threadName, credentialId);
        
        try {
            // Simulate password strength calculation
            Thread.sleep(1000);
            int strength = (int) (Math.random() * 100);
            logger.info("[{}] Password strength calculated: {} for credential: {}", 
                       threadName, strength, credentialId);
        } catch (InterruptedException e) {
            logger.error("[{}] Password analysis interrupted: {}", threadName, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Clean up old audit logs asynchronously
     * Low priority background task
     *
     * @param daysToKeep number of days of audit logs to retain
     */
    @Async("lowPriorityExecutor")
    public void cleanupOldAuditLogs(int daysToKeep) {
        String threadName = Thread.currentThread().getName();
        logger.info("[{}] Starting audit log cleanup (keeping last {} days)", threadName, daysToKeep);
        
        try {
            // Simulate cleanup operation
            Thread.sleep(3000);
            logger.info("[{}] Audit log cleanup completed", threadName);
        } catch (InterruptedException e) {
            logger.error("[{}] Audit log cleanup interrupted: {}", threadName, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Send security alert notification
     *
     * @param userEmail user's email
     * @param alertType type of security alert
     * @param message alert message
     */
    @Async("taskExecutor")
    public void sendSecurityAlert(String userEmail, String alertType, String message) {
        String threadName = Thread.currentThread().getName();
        logger.warn("[{}] Security Alert | Type: {} | User: {} | Message: {}", 
                   threadName, alertType, userEmail, message);
        
        String subject = "Security Alert: " + alertType;
        sendEmailNotification(userEmail, subject, message);
    }
}
