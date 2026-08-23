package com.securevault.service;

import com.securevault.dto.DashboardMetrics;
import com.securevault.entity.AuditLog;
import com.securevault.entity.Credential;
import com.securevault.entity.CredentialShare;
import com.securevault.entity.SecurityEvent;
import com.securevault.enums.SecurityEventType;
import com.securevault.repository.*;
import com.securevault.util.AESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AnalyticsDashboardService
 * 
 * Service for generating comprehensive dashboard metrics and analytics.
 * Provides a single endpoint for all dashboard data.
 */
@Service
public class AnalyticsDashboardService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsDashboardService.class);
    
    private final CredentialRepository credentialRepository;
    private final CredentialShareRepository credentialShareRepository;
    private final SecurityEventRepository securityEventRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AESUtil aesUtil;
    
    public AnalyticsDashboardService(
            CredentialRepository credentialRepository,
            CredentialShareRepository credentialShareRepository,
            SecurityEventRepository securityEventRepository,
            AuditLogRepository auditLogRepository,
            UserRepository userRepository,
            AESUtil aesUtil) {
        this.credentialRepository = credentialRepository;
        this.credentialShareRepository = credentialShareRepository;
        this.securityEventRepository = securityEventRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.aesUtil = aesUtil;
    }
    
    /**
     * Get comprehensive dashboard metrics for a user
     */
    public DashboardMetrics getDashboardMetrics(Long userId) {
        logger.info("Generating dashboard metrics for user ID: {}", userId);
        
        DashboardMetrics metrics = new DashboardMetrics();
        
        // Credential statistics
        List<Credential> credentials = credentialRepository.findByUserId(userId);
        metrics.setTotalCredentials(credentials.size());
        
        // Shared credentials count
        List<CredentialShare> shares = credentialShareRepository.findByOwnerId(userId);
        metrics.setSharedCredentials(shares.size());
        
        // Weak passwords count
        int weakCount = countWeakPasswords(credentials);
        metrics.setWeakPasswords(weakCount);
        
        // Recently added credentials (last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        int recentCredentials = (int) credentials.stream()
                .filter(c -> c.getCreatedAt().isAfter(sevenDaysAgo))
                .count();
        metrics.setRecentlyAddedCredentials(recentCredentials);
        
        // Security statistics
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        Long failedLogins = securityEventRepository.countFailedLoginAttempts(
                null, SecurityEventType.LOGIN_FAILURE, yesterday);
        metrics.setFailedLoginCount(failedLogins.intValue());
        
        // Security alerts (last 7 days)
        List<SecurityEvent> recentEvents = securityEventRepository.findRecentEventsByUserId(userId, sevenDaysAgo);
        long alertCount = recentEvents.stream()
                .filter(e -> Boolean.TRUE.equals(e.getAlertGenerated()))
                .count();
        metrics.setSecurityAlerts((int) alertCount);
        
        // Suspicious activities (last 7 days)
        long suspiciousCount = recentEvents.stream()
                .filter(e -> e.getEventType() == SecurityEventType.SUSPICIOUS_PATTERN ||
                           e.getEventType() == SecurityEventType.BRUTE_FORCE_ATTEMPT ||
                           e.getEventType() == SecurityEventType.ABNORMAL_ACTIVITY)
                .count();
        metrics.setSuspiciousActivities((int) suspiciousCount);
        
        // New device logins (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<SecurityEvent> monthEvents = securityEventRepository.findRecentEventsByUserId(userId, thirtyDaysAgo);
        long newDeviceCount = monthEvents.stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsNewDevice()))
                .count();
        metrics.setNewDeviceLogins((int) newDeviceCount);
        
        // Recent user activity (last 20 actions)
        List<AuditLog> recentAudits = auditLogRepository.findByPerformedByOrderByTimestampDesc(userId)
                .stream()
                .limit(20)
                .collect(Collectors.toList());
        
        List<DashboardMetrics.RecentActivity> recentActivities = recentAudits.stream()
                .map(audit -> {
                    DashboardMetrics.RecentActivity activity = new DashboardMetrics.RecentActivity();
                    activity.setAction(audit.getAction());
                    activity.setEntityType(audit.getEntityType());
                    activity.setDetails(audit.getDetails());
                    activity.setTimestamp(audit.getTimestamp());
                    activity.setUserId(audit.getPerformedBy());
                    
                    // Get user email
                    userRepository.findById(audit.getPerformedBy())
                            .ifPresent(user -> activity.setUserEmail(user.getEmail()));
                    
                    return activity;
                })
                .collect(Collectors.toList());
        metrics.setRecentUserActivity(recentActivities);
        
        // User statistics
        long totalUsers = userRepository.count();
        metrics.setTotalUsers((int) totalUsers);
        
        // Active users (had login in last 30 days)
        long activeUsers = securityEventRepository.countEventsByType(
                SecurityEventType.LOGIN_SUCCESS, thirtyDaysAgo);
        metrics.setActiveUsers((int) activeUsers);
        
        // Health scores
        double passwordHealthScore = calculatePasswordHealthScore(credentials);
        metrics.setPasswordHealthScore(passwordHealthScore);
        
        double securityScore = calculateSecurityScore(recentEvents);
        metrics.setSecurityScore(securityScore);
        
        // Trends (last 7 days)
        metrics.setCredentialsTrend(generateCredentialsTrend(userId, 7));
        metrics.setLoginsTrend(generateLoginsTrend(userId, 7));
        
        // Quick stats (today)
        metrics.setQuickStats(generateQuickStats(userId));
        
        logger.info("Dashboard metrics generated successfully for user ID: {}", userId);
        
        return metrics;
    }
    
    /**
     * Get system-wide dashboard metrics (admin view)
     */
    public DashboardMetrics getSystemDashboardMetrics() {
        logger.info("Generating system-wide dashboard metrics");
        
        DashboardMetrics metrics = new DashboardMetrics();
        
        // Total credentials
        long totalCredentials = credentialRepository.count();
        metrics.setTotalCredentials((int) totalCredentials);
        
        // Total shared credentials
        long totalShares = credentialShareRepository.count();
        metrics.setSharedCredentials((int) totalShares);
        
        // Count weak passwords across all users
        List<Credential> allCredentials = credentialRepository.findAll();
        int weakCount = countWeakPasswords(allCredentials);
        metrics.setWeakPasswords(weakCount);
        
        // Security statistics
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        Long failedLogins = securityEventRepository.countTotalFailedLogins(yesterday);
        metrics.setFailedLoginCount(failedLogins.intValue());
        
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<SecurityEvent> highRiskEvents = securityEventRepository.findHighRiskEvents(sevenDaysAgo);
        metrics.setSecurityAlerts(highRiskEvents.size());
        
        // Total users
        long totalUsers = userRepository.count();
        metrics.setTotalUsers((int) totalUsers);
        
        // Health scores
        double passwordHealthScore = calculatePasswordHealthScore(allCredentials);
        metrics.setPasswordHealthScore(passwordHealthScore);
        
        logger.info("System dashboard metrics generated successfully");
        
        return metrics;
    }
    
    /**
     * Count weak passwords in a list of credentials
     */
    private int countWeakPasswords(List<Credential> credentials) {
        int weakCount = 0;
        
        for (Credential credential : credentials) {
            try {
                String decryptedPassword = aesUtil.decrypt(credential.getEncryptedPassword());
                int strength = evaluatePasswordStrength(decryptedPassword);
                
                if (strength < 30) {
                    weakCount++;
                }
            } catch (Exception e) {
                logger.error("Error decrypting password for credential ID: {}", 
                        credential.getCredentialId(), e);
            }
        }
        
        return weakCount;
    }
    
    /**
     * Evaluate password strength (0-100)
     */
    private int evaluatePasswordStrength(String password) {
        int score = 0;
        
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        // Length score
        score += Math.min(password.length() * 4, 40);
        
        // Character variety
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        
        if (hasLower) score += 5;
        if (hasUpper) score += 5;
        if (hasDigit) score += 10;
        if (hasSpecial) score += 10;
        
        int variety = (hasLower ? 1 : 0) + (hasUpper ? 1 : 0) + 
                     (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
        score += variety * 7;
        
        return Math.min(score, 100);
    }
    
    /**
     * Calculate password health score
     */
    private double calculatePasswordHealthScore(List<Credential> credentials) {
        if (credentials.isEmpty()) {
            return 100.0;
        }
        
        int totalScore = 0;
        int count = 0;
        
        for (Credential credential : credentials) {
            try {
                String decryptedPassword = aesUtil.decrypt(credential.getEncryptedPassword());
                totalScore += evaluatePasswordStrength(decryptedPassword);
                count++;
            } catch (Exception e) {
                logger.error("Error evaluating password strength", e);
            }
        }
        
        return count > 0 ? (double) totalScore / count : 100.0;
    }
    
    /**
     * Calculate security score from recent events
     */
    private double calculateSecurityScore(List<SecurityEvent> events) {
        if (events.isEmpty()) {
            return 100.0;
        }
        
        long failedLogins = events.stream()
                .filter(e -> e.getEventType() == SecurityEventType.LOGIN_FAILURE)
                .count();
        
        long highRiskEvents = events.stream()
                .filter(e -> e.getRiskLevel() == com.securevault.enums.RiskLevel.HIGH ||
                           e.getRiskLevel() == com.securevault.enums.RiskLevel.CRITICAL)
                .count();
        
        double score = 100.0;
        score -= (failedLogins * 5);
        score -= (highRiskEvents * 10);
        
        return Math.max(0, score);
    }
    
    /**
     * Generate credentials trend for the last N days
     */
    private List<DashboardMetrics.TrendData> generateCredentialsTrend(Long userId, int days) {
        List<DashboardMetrics.TrendData> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(formatter);
            
            // Count credentials created on this day
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            
            List<Credential> credentials = credentialRepository.findByUserId(userId);
            long count = credentials.stream()
                    .filter(c -> c.getCreatedAt().isAfter(startOfDay) && 
                               c.getCreatedAt().isBefore(endOfDay))
                    .count();
            
            trend.add(new DashboardMetrics.TrendData(dateStr, (int) count));
        }
        
        return trend;
    }
    
    /**
     * Generate logins trend for the last N days
     */
    private List<DashboardMetrics.TrendData> generateLoginsTrend(Long userId, int days) {
        List<DashboardMetrics.TrendData> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<SecurityEvent> loginEvents = securityEventRepository.findLoginActivityForUser(userId, since);
        
        Map<String, Integer> loginsByDate = new HashMap<>();
        
        for (SecurityEvent event : loginEvents) {
            String dateStr = event.getTimestamp().toLocalDate().format(formatter);
            loginsByDate.merge(dateStr, 1, Integer::sum);
        }
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(formatter);
            int count = loginsByDate.getOrDefault(dateStr, 0);
            trend.add(new DashboardMetrics.TrendData(dateStr, count));
        }
        
        return trend;
    }
    
    /**
     * Generate quick stats for today
     */
    private DashboardMetrics.QuickStats generateQuickStats(Long userId) {
        DashboardMetrics.QuickStats stats = new DashboardMetrics.QuickStats();
        
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        
        // Today's logins
        List<SecurityEvent> todayEvents = securityEventRepository.findRecentEventsByUserId(userId, startOfDay);
        long todayLogins = todayEvents.stream()
                .filter(e -> e.getEventType() == SecurityEventType.LOGIN_SUCCESS)
                .count();
        stats.setTodayLogins((int) todayLogins);
        
        // Today's failed logins
        long todayFailedLogins = todayEvents.stream()
                .filter(e -> e.getEventType() == SecurityEventType.LOGIN_FAILURE)
                .count();
        stats.setTodayFailedLogins((int) todayFailedLogins);
        
        // Today's new credentials
        List<Credential> credentials = credentialRepository.findByUserId(userId);
        long todayCredentials = credentials.stream()
                .filter(c -> c.getCreatedAt().isAfter(startOfDay))
                .count();
        stats.setTodayNewCredentials((int) todayCredentials);
        
        // Today's shares
        List<CredentialShare> shares = credentialShareRepository.findByOwnerId(userId);
        long todayShares = shares.stream()
                .filter(s -> s.getSharedAt().isAfter(startOfDay))
                .count();
        stats.setTodayShares((int) todayShares);
        
        return stats;
    }
}
