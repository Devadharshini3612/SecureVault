package com.securevault.service;

import com.securevault.dto.SecurityEventResponse;
import com.securevault.entity.SecurityEvent;
import com.securevault.entity.User;
import com.securevault.enums.RiskLevel;
import com.securevault.enums.SecurityEventType;
import com.securevault.repository.SecurityEventRepository;
import com.securevault.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SecurityMonitoringService
 * 
 * Comprehensive security monitoring service for tracking and analyzing security events.
 * Monitors login attempts, detects suspicious activities, tracks devices, and classifies risks.
 */
@Service
public class SecurityMonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityMonitoringService.class);
    
    // Configuration thresholds
    private static final int FAILED_LOGIN_THRESHOLD_MEDIUM = 3;
    private static final int FAILED_LOGIN_THRESHOLD_HIGH = 5;
    private static final int FAILED_LOGIN_THRESHOLD_CRITICAL = 10;
    private static final int FAILED_LOGIN_TIME_WINDOW_MINUTES = 30;
    private static final int BRUTE_FORCE_THRESHOLD = 5;
    private static final int BRUTE_FORCE_TIME_WINDOW_MINUTES = 10;
    
    private final SecurityEventRepository securityEventRepository;
    private final UserRepository userRepository;
    private final SecurityAlertService securityAlertService;
    
    public SecurityMonitoringService(
            SecurityEventRepository securityEventRepository,
            UserRepository userRepository,
            SecurityAlertService securityAlertService) {
        this.securityEventRepository = securityEventRepository;
        this.userRepository = userRepository;
        this.securityAlertService = securityAlertService;
    }
    
    /**
     * Track successful login attempt
     */
    @Transactional
    public SecurityEvent trackSuccessfulLogin(String email, HttpServletRequest request) {
        logger.info("Tracking successful login for email: {}", email);
        
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            logger.warn("User not found for email: {}", email);
            return null;
        }
        
        SecurityEvent event = createSecurityEvent(user.getUserId(), email, request);
        event.setEventType(SecurityEventType.LOGIN_SUCCESS);
        event.setSuccess(true);
        event.setRiskLevel(RiskLevel.LOW);
        
        // Check if this is a new device
        boolean isNewDevice = !isKnownDevice(user.getUserId(), event.getDeviceFingerprint());
        event.setIsNewDevice(isNewDevice);
        
        if (isNewDevice) {
            logger.info("New device detected for user: {}", email);
            event.setRiskLevel(RiskLevel.MEDIUM);
            event.setDetails("Login from new device");
            
            // Generate alert for new device login
            securityAlertService.generateNewDeviceAlert(user.getUserId(), event);
        }
        
        SecurityEvent savedEvent = securityEventRepository.save(event);
        logger.info("Successful login tracked. Event ID: {}", savedEvent.getEventId());
        
        return savedEvent;
    }
    
    /**
     * Track failed login attempt
     */
    @Transactional
    public SecurityEvent trackFailedLogin(String email, String reason, HttpServletRequest request) {
        logger.warn("Tracking failed login attempt for email: {}", email);
        
        User user = userRepository.findByEmail(email).orElse(null);
        Long userId = user != null ? user.getUserId() : null;
        
        SecurityEvent event = createSecurityEvent(userId, email, request);
        event.setEventType(SecurityEventType.LOGIN_FAILURE);
        event.setSuccess(false);
        event.setReason(reason);
        
        // Count recent failed login attempts
        LocalDateTime timeWindow = LocalDateTime.now().minusMinutes(FAILED_LOGIN_TIME_WINDOW_MINUTES);
        Long failedCount = securityEventRepository.countFailedLoginAttempts(
                email, SecurityEventType.LOGIN_FAILURE, timeWindow);
        
        event.setFailedAttemptCount(failedCount.intValue() + 1);
        
        // Classify risk level based on failed attempts
        RiskLevel riskLevel = classifyFailedLoginRisk(failedCount.intValue() + 1);
        event.setRiskLevel(riskLevel);
        
        // Check for brute force attack
        if (isBruteForceAttempt(email)) {
            logger.error("Potential brute force attack detected for email: {}", email);
            event.setEventType(SecurityEventType.BRUTE_FORCE_ATTEMPT);
            event.setRiskLevel(RiskLevel.CRITICAL);
            event.setAlertGenerated(true);
            
            // Generate security alert
            securityAlertService.generateBruteForceAlert(userId, email, event);
        } else if (failedCount >= FAILED_LOGIN_THRESHOLD_MEDIUM) {
            logger.warn("Multiple failed login attempts detected for email: {} (count: {})", email, failedCount + 1);
            event.setAlertGenerated(true);
            
            // Generate alert for repeated failures
            securityAlertService.generateRepeatedFailureAlert(userId, email, event);
        }
        
        SecurityEvent savedEvent = securityEventRepository.save(event);
        logger.info("Failed login tracked. Event ID: {}, Risk Level: {}", 
                savedEvent.getEventId(), savedEvent.getRiskLevel());
        
        return savedEvent;
    }
    
    /**
     * Track logout event
     */
    @Transactional
    public SecurityEvent trackLogout(Long userId, String email, HttpServletRequest request) {
        logger.info("Tracking logout for user: {}", email);
        
        SecurityEvent event = createSecurityEvent(userId, email, request);
        event.setEventType(SecurityEventType.LOGOUT);
        event.setSuccess(true);
        event.setRiskLevel(RiskLevel.LOW);
        
        return securityEventRepository.save(event);
    }
    
    /**
     * Track suspicious activity
     */
    @Transactional
    public SecurityEvent trackSuspiciousActivity(Long userId, String email, 
                                                   SecurityEventType eventType, 
                                                   String details, 
                                                   HttpServletRequest request) {
        logger.warn("Tracking suspicious activity for user: {} - Type: {}", email, eventType);
        
        SecurityEvent event = createSecurityEvent(userId, email, request);
        event.setEventType(eventType);
        event.setSuccess(false);
        event.setRiskLevel(RiskLevel.HIGH);
        event.setDetails(details);
        event.setAlertGenerated(true);
        
        SecurityEvent savedEvent = securityEventRepository.save(event);
        
        // Generate security alert
        securityAlertService.generateSuspiciousActivityAlert(userId, email, savedEvent);
        
        return savedEvent;
    }
    
    /**
     * Get security events for a user
     */
    public List<SecurityEventResponse> getUserSecurityEvents(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<SecurityEvent> events = securityEventRepository.findRecentEventsByUserId(userId, since);
        
        return events.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get failed login attempts for a user
     */
    public List<SecurityEventResponse> getFailedLoginAttempts(String email, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<SecurityEvent> events = securityEventRepository.findRecentFailedLogins(email, since);
        
        return events.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get high-risk security events
     */
    public List<SecurityEventResponse> getHighRiskEvents(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<SecurityEvent> events = securityEventRepository.findHighRiskEvents(since);
        
        return events.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get events that triggered alerts
     */
    public List<SecurityEventResponse> getEventsWithAlerts() {
        List<SecurityEvent> events = securityEventRepository.findEventsWithAlerts();
        
        return events.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get new device logins for a user
     */
    public List<SecurityEventResponse> getNewDeviceLogins(Long userId) {
        List<SecurityEvent> events = securityEventRepository.findNewDeviceLogins(userId);
        
        return events.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if device is known for a user
     */
    private boolean isKnownDevice(Long userId, String deviceFingerprint) {
        if (deviceFingerprint == null || deviceFingerprint.isEmpty()) {
            return false;
        }
        
        Boolean isKnown = securityEventRepository.hasDeviceBeenUsedByUser(userId, deviceFingerprint);
        return isKnown != null && isKnown;
    }
    
    /**
     * Check for brute force attack pattern
     */
    private boolean isBruteForceAttempt(String email) {
        LocalDateTime timeWindow = LocalDateTime.now().minusMinutes(BRUTE_FORCE_TIME_WINDOW_MINUTES);
        Long failedCount = securityEventRepository.countFailedLoginAttempts(
                email, SecurityEventType.LOGIN_FAILURE, timeWindow);
        
        return failedCount >= BRUTE_FORCE_THRESHOLD;
    }
    
    /**
     * Classify risk level based on failed login count
     */
    private RiskLevel classifyFailedLoginRisk(int failedAttempts) {
        if (failedAttempts >= FAILED_LOGIN_THRESHOLD_CRITICAL) {
            return RiskLevel.CRITICAL;
        } else if (failedAttempts >= FAILED_LOGIN_THRESHOLD_HIGH) {
            return RiskLevel.HIGH;
        } else if (failedAttempts >= FAILED_LOGIN_THRESHOLD_MEDIUM) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }
    
    /**
     * Create a security event from HTTP request
     */
    private SecurityEvent createSecurityEvent(Long userId, String email, HttpServletRequest request) {
        SecurityEvent event = new SecurityEvent();
        event.setUserId(userId);
        event.setEmail(email);
        event.setIpAddress(getClientIpAddress(request));
        event.setUserAgent(request.getHeader("User-Agent"));
        event.setDeviceFingerprint(generateDeviceFingerprint(request));
        event.setLocation(getLocationFromIp(event.getIpAddress()));
        event.setTimestamp(LocalDateTime.now());
        
        return event;
    }
    
    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Generate device fingerprint from request
     */
    private String generateDeviceFingerprint(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String acceptLanguage = request.getHeader("Accept-Language");
        String acceptEncoding = request.getHeader("Accept-Encoding");
        
        if (userAgent == null) {
            return "unknown";
        }
        
        // Simple fingerprint: hash of user agent + accept headers
        String fingerprint = userAgent + "|" + 
                             (acceptLanguage != null ? acceptLanguage : "") + "|" + 
                             (acceptEncoding != null ? acceptEncoding : "");
        
        return Integer.toHexString(fingerprint.hashCode());
    }
    
    /**
     * Get location from IP address (placeholder - would use GeoIP service in production)
     */
    private String getLocationFromIp(String ipAddress) {
        if (ipAddress == null || ipAddress.equals("0:0:0:0:0:0:0:1") || ipAddress.equals("127.0.0.1")) {
            return "Localhost";
        }
        
        // In production, integrate with GeoIP service (e.g., MaxMind, IP2Location)
        return "Unknown Location";
    }
    
    /**
     * Map SecurityEvent entity to SecurityEventResponse DTO
     */
    private SecurityEventResponse mapToResponse(SecurityEvent event) {
        SecurityEventResponse response = new SecurityEventResponse();
        response.setEventId(event.getEventId());
        response.setUserId(event.getUserId());
        response.setEmail(event.getEmail());
        response.setEventType(event.getEventType());
        response.setRiskLevel(event.getRiskLevel());
        response.setIpAddress(event.getIpAddress());
        response.setUserAgent(event.getUserAgent());
        response.setDeviceFingerprint(event.getDeviceFingerprint());
        response.setLocation(event.getLocation());
        response.setSuccess(event.getSuccess());
        response.setDetails(event.getDetails());
        response.setReason(event.getReason());
        response.setFailedAttemptCount(event.getFailedAttemptCount());
        response.setIsNewDevice(event.getIsNewDevice());
        response.setAlertGenerated(event.getAlertGenerated());
        response.setTimestamp(event.getTimestamp());
        
        return response;
    }
}
