package com.securevault.service;

import com.securevault.dto.LoginActivityReport;
import com.securevault.dto.PasswordHealthReport;
import com.securevault.dto.SecuritySummary;
import com.securevault.entity.AuditLog;
import com.securevault.entity.Credential;
import com.securevault.entity.SecurityEvent;
import com.securevault.entity.User;
import com.securevault.enums.RiskLevel;
import com.securevault.enums.SecurityEventType;
import com.securevault.repository.AuditLogRepository;
import com.securevault.repository.CredentialRepository;
import com.securevault.repository.SecurityEventRepository;
import com.securevault.repository.UserRepository;
import com.securevault.util.AESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AuditAnalyticsService
 * 
 * Service for generating analytics reports and insights from audit logs and security events.
 * Provides password health reports, login activity analysis, and security summaries.
 */
@Service
public class AuditAnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditAnalyticsService.class);
    
    private final AuditLogRepository auditLogRepository;
    private final SecurityEventRepository securityEventRepository;
    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final AESUtil aesUtil;
    
    public AuditAnalyticsService(
            AuditLogRepository auditLogRepository,
            SecurityEventRepository securityEventRepository,
            CredentialRepository credentialRepository,
            UserRepository userRepository,
            AESUtil aesUtil) {
        this.auditLogRepository = auditLogRepository;
        this.securityEventRepository = securityEventRepository;
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.aesUtil = aesUtil;
    }
    
    /**
     * Generate password health report for a user
     */
    public PasswordHealthReport generatePasswordHealthReport(Long userId) {
        logger.info("Generating password health report for user ID: {}", userId);
        
        PasswordHealthReport report = new PasswordHealthReport();
        List<Credential> credentials = credentialRepository.findByUserId(userId);
        
        report.setTotalCredentials(credentials.size());
        
        int weakCount = 0;
        int moderateCount = 0;
        int strongCount = 0;
        List<PasswordHealthReport.WeakPasswordDetail> weakDetails = new ArrayList<>();
        
        for (Credential credential : credentials) {
            try {
                String decryptedPassword = aesUtil.decrypt(credential.getEncryptedPassword());
                int strength = evaluatePasswordStrength(decryptedPassword);
                
                if (strength < 30) {
                    weakCount++;
                    weakDetails.add(new PasswordHealthReport.WeakPasswordDetail(
                        credential.getCredentialId(),
                        credential.getServiceName(),
                        "Password is too weak (score: " + strength + ")",
                        "Use a longer password with mixed characters"
                    ));
                } else if (strength < 60) {
                    moderateCount++;
                } else {
                    strongCount++;
                }
            } catch (Exception e) {
                logger.error("Error decrypting password for credential ID: {}", credential.getCredentialId(), e);
            }
        }
        
        report.setWeakPasswords(weakCount);
        report.setModeratePasswords(moderateCount);
        report.setStrongPasswords(strongCount);
        report.setWeakPasswordDetails(weakDetails);
        
        // Calculate overall health score
        double healthScore = calculatePasswordHealthScore(strongCount, moderateCount, weakCount, credentials.size());
        report.setOverallHealthScore(healthScore);
        
        // Generate recommendations
        List<String> recommendations = generatePasswordRecommendations(report);
        report.setRecommendations(recommendations);
        
        logger.info("Password health report generated. Total: {}, Weak: {}, Strong: {}", 
                credentials.size(), weakCount, strongCount);
        
        return report;
    }
    
    /**
     * Generate login activity report for a user
     */
    public LoginActivityReport generateLoginActivityReport(Long userId, int days) {
        logger.info("Generating login activity report for user ID: {} (last {} days)", userId, days);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        LoginActivityReport report = new LoginActivityReport();
        report.setUserId(userId);
        report.setEmail(user.getEmail());
        
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<SecurityEvent> loginEvents = securityEventRepository.findLoginActivityForUser(userId, since);
        
        // Count login statistics
        int successfulLogins = 0;
        int failedLogins = 0;
        int newDeviceLogins = 0;
        LocalDateTime lastSuccessful = null;
        LocalDateTime lastFailed = null;
        
        for (SecurityEvent event : loginEvents) {
            if (event.getEventType() == SecurityEventType.LOGIN_SUCCESS) {
                successfulLogins++;
                if (lastSuccessful == null || event.getTimestamp().isAfter(lastSuccessful)) {
                    lastSuccessful = event.getTimestamp();
                }
            } else if (event.getEventType() == SecurityEventType.LOGIN_FAILURE) {
                failedLogins++;
                if (lastFailed == null || event.getTimestamp().isAfter(lastFailed)) {
                    lastFailed = event.getTimestamp();
                }
            }
            
            if (Boolean.TRUE.equals(event.getIsNewDevice())) {
                newDeviceLogins++;
            }
        }
        
        report.setTotalLogins(loginEvents.size());
        report.setSuccessfulLogins(successfulLogins);
        report.setFailedLogins(failedLogins);
        report.setNewDeviceLogins(newDeviceLogins);
        report.setLastSuccessfulLogin(lastSuccessful);
        report.setLastFailedLogin(lastFailed);
        
        // Get recent login attempts (last 20)
        List<LoginActivityReport.LoginAttempt> recentAttempts = loginEvents.stream()
                .limit(20)
                .map(event -> new LoginActivityReport.LoginAttempt(
                    event.getTimestamp(),
                    event.getSuccess(),
                    event.getIpAddress(),
                    event.getLocation(),
                    event.getUserAgent(),
                    event.getReason()
                ))
                .collect(Collectors.toList());
        report.setRecentAttempts(recentAttempts);
        
        // Collect unique devices
        Map<String, LoginActivityReport.DeviceInfo> deviceMap = new HashMap<>();
        for (SecurityEvent event : loginEvents) {
            String fingerprint = event.getDeviceFingerprint();
            if (fingerprint != null) {
                deviceMap.computeIfAbsent(fingerprint, k -> {
                    LoginActivityReport.DeviceInfo device = new LoginActivityReport.DeviceInfo();
                    device.setDeviceFingerprint(fingerprint);
                    device.setUserAgent(event.getUserAgent());
                    device.setFirstSeen(event.getTimestamp());
                    device.setLastUsed(event.getTimestamp());
                    device.setLoginCount(0);
                    return device;
                });
                
                LoginActivityReport.DeviceInfo device = deviceMap.get(fingerprint);
                device.setLoginCount(device.getLoginCount() + 1);
                if (event.getTimestamp().isAfter(device.getLastUsed())) {
                    device.setLastUsed(event.getTimestamp());
                }
                if (event.getTimestamp().isBefore(device.getFirstSeen())) {
                    device.setFirstSeen(event.getTimestamp());
                }
            }
        }
        report.setDevices(new ArrayList<>(deviceMap.values()));
        
        // Collect unique locations
        Set<String> locations = loginEvents.stream()
                .map(SecurityEvent::getLocation)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        report.setLocations(new ArrayList<>(locations));
        
        logger.info("Login activity report generated. Total logins: {}, Successful: {}, Failed: {}", 
                loginEvents.size(), successfulLogins, failedLogins);
        
        return report;
    }
    
    /**
     * Generate security summary for a user
     */
    public SecuritySummary generateSecuritySummary(Long userId, int days) {
        logger.info("Generating security summary for user ID: {} (last {} days)", userId, days);
        
        SecuritySummary summary = new SecuritySummary();
        
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<SecurityEvent> securityEvents = securityEventRepository.findRecentEventsByUserId(userId, since);
        
        summary.setTotalSecurityEvents(securityEvents.size());
        
        // Count high-risk events
        long highRiskCount = securityEvents.stream()
                .filter(event -> event.getRiskLevel() == RiskLevel.HIGH || 
                               event.getRiskLevel() == RiskLevel.CRITICAL)
                .count();
        summary.setHighRiskEvents((int) highRiskCount);
        
        // Count failed logins
        long failedLoginCount = securityEvents.stream()
                .filter(event -> event.getEventType() == SecurityEventType.LOGIN_FAILURE)
                .count();
        summary.setFailedLoginAttempts((int) failedLoginCount);
        
        // Count suspicious activities
        long suspiciousCount = securityEvents.stream()
                .filter(event -> event.getEventType() == SecurityEventType.SUSPICIOUS_PATTERN ||
                               event.getEventType() == SecurityEventType.BRUTE_FORCE_ATTEMPT ||
                               event.getEventType() == SecurityEventType.ABNORMAL_ACTIVITY)
                .count();
        summary.setSuspiciousActivities((int) suspiciousCount);
        
        // Count active alerts
        long alertCount = securityEvents.stream()
                .filter(event -> Boolean.TRUE.equals(event.getAlertGenerated()))
                .count();
        summary.setActiveAlerts((int) alertCount);
        
        // Get last security incident
        Optional<SecurityEvent> lastIncident = securityEvents.stream()
                .filter(event -> event.getRiskLevel() == RiskLevel.HIGH || 
                               event.getRiskLevel() == RiskLevel.CRITICAL)
                .max(Comparator.comparing(SecurityEvent::getTimestamp));
        lastIncident.ifPresent(event -> summary.setLastSecurityIncident(event.getTimestamp()));
        
        // Determine overall risk level
        RiskLevel overallRisk = determineOverallRiskLevel(securityEvents);
        summary.setOverallRiskLevel(overallRisk);
        
        // Get recent alerts
        List<SecuritySummary.SecurityAlert> recentAlerts = securityEvents.stream()
                .filter(event -> Boolean.TRUE.equals(event.getAlertGenerated()))
                .limit(10)
                .map(event -> new SecuritySummary.SecurityAlert(
                    event.getEventType().toString(),
                    event.getDetails() != null ? event.getDetails() : event.getEventType().getDescription(),
                    event.getRiskLevel(),
                    event.getTimestamp()
                ))
                .collect(Collectors.toList());
        summary.setRecentAlerts(recentAlerts);
        
        // Generate recommendations
        List<String> recommendations = generateSecurityRecommendations(summary, securityEvents);
        summary.setRecommendations(recommendations);
        
        // Calculate security score
        SecuritySummary.SecurityScore securityScore = calculateSecurityScore(userId, securityEvents);
        summary.setSecurityScore(securityScore);
        
        logger.info("Security summary generated. Risk Level: {}, High Risk Events: {}", 
                overallRisk, highRiskCount);
        
        return summary;
    }
    
    /**
     * Evaluate password strength (0-100)
     */
    private int evaluatePasswordStrength(String password) {
        int score = 0;
        
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        // Length score (max 40 points)
        score += Math.min(password.length() * 4, 40);
        
        // Character variety (max 30 points)
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        
        if (hasLower) score += 5;
        if (hasUpper) score += 5;
        if (hasDigit) score += 10;
        if (hasSpecial) score += 10;
        
        // Variety bonus (max 30 points)
        int variety = (hasLower ? 1 : 0) + (hasUpper ? 1 : 0) + 
                     (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
        score += variety * 7;
        
        return Math.min(score, 100);
    }
    
    /**
     * Calculate password health score
     */
    private double calculatePasswordHealthScore(int strong, int moderate, int weak, int total) {
        if (total == 0) {
            return 100.0;
        }
        
        double strongWeight = 1.0;
        double moderateWeight = 0.6;
        double weakWeight = 0.2;
        
        double weightedScore = (strong * strongWeight + moderate * moderateWeight + weak * weakWeight) / total;
        return weightedScore * 100;
    }
    
    /**
     * Generate password recommendations
     */
    private List<String> generatePasswordRecommendations(PasswordHealthReport report) {
        List<String> recommendations = new ArrayList<>();
        
        if (report.getWeakPasswords() > 0) {
            recommendations.add(String.format("Update %d weak password(s) to stronger alternatives", 
                    report.getWeakPasswords()));
        }
        
        if (report.getOverallHealthScore() < 50) {
            recommendations.add("Consider using a password manager to generate strong passwords");
            recommendations.add("Enable two-factor authentication for critical accounts");
        }
        
        recommendations.add("Use unique passwords for each service");
        recommendations.add("Avoid using personal information in passwords");
        recommendations.add("Change passwords regularly (every 90 days)");
        
        return recommendations;
    }
    
    /**
     * Determine overall risk level from events
     */
    private RiskLevel determineOverallRiskLevel(List<SecurityEvent> events) {
        long criticalCount = events.stream()
                .filter(e -> e.getRiskLevel() == RiskLevel.CRITICAL)
                .count();
        
        long highCount = events.stream()
                .filter(e -> e.getRiskLevel() == RiskLevel.HIGH)
                .count();
        
        if (criticalCount > 0) {
            return RiskLevel.CRITICAL;
        } else if (highCount > 2) {
            return RiskLevel.HIGH;
        } else if (highCount > 0) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }
    
    /**
     * Generate security recommendations
     */
    private List<String> generateSecurityRecommendations(SecuritySummary summary, 
                                                          List<SecurityEvent> events) {
        List<String> recommendations = new ArrayList<>();
        
        if (summary.getFailedLoginAttempts() > 5) {
            recommendations.add("Review failed login attempts and consider changing your password");
        }
        
        if (summary.getSuspiciousActivities() > 0) {
            recommendations.add("Suspicious activity detected - review your recent account activity");
        }
        
        long newDeviceCount = events.stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsNewDevice()))
                .count();
        if (newDeviceCount > 3) {
            recommendations.add("Multiple new devices detected - verify all devices are yours");
        }
        
        recommendations.add("Enable two-factor authentication for additional security");
        recommendations.add("Regularly review your security events and login history");
        recommendations.add("Keep your passwords strong and unique");
        
        return recommendations;
    }
    
    /**
     * Calculate security score
     */
    private SecuritySummary.SecurityScore calculateSecurityScore(Long userId, 
                                                                  List<SecurityEvent> events) {
        SecuritySummary.SecurityScore score = new SecuritySummary.SecurityScore();
        
        // Calculate login security score
        long failedLogins = events.stream()
                .filter(e -> e.getEventType() == SecurityEventType.LOGIN_FAILURE)
                .count();
        double loginSecurity = Math.max(0, 100 - (failedLogins * 10));
        score.setLoginSecurity(loginSecurity);
        
        // Calculate activity pattern score
        long suspiciousEvents = events.stream()
                .filter(e -> e.getRiskLevel() == RiskLevel.HIGH || 
                           e.getRiskLevel() == RiskLevel.CRITICAL)
                .count();
        double activityPattern = Math.max(0, 100 - (suspiciousEvents * 15));
        score.setActivityPattern(activityPattern);
        
        // Get password strength (simplified for demo)
        score.setPasswordStrength(75.0);
        
        // Calculate overall score
        double overall = (loginSecurity + activityPattern + score.getPasswordStrength()) / 3;
        score.setOverall(overall);
        
        // Determine rating
        if (overall >= 85) {
            score.setRating("Excellent");
        } else if (overall >= 70) {
            score.setRating("Good");
        } else if (overall >= 50) {
            score.setRating("Fair");
        } else {
            score.setRating("Poor");
        }
        
        return score;
    }
}
