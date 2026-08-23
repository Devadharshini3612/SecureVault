package com.securevault.controller;

import com.securevault.dto.ApiResponse;
import com.securevault.entity.SecurityEvent;
import com.securevault.enums.SecurityEventType;
import com.securevault.service.SecurityMonitoringService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SecurityMonitoringTestController
 * 
 * Controller for testing security monitoring features.
 * Provides endpoints to simulate various security events for testing and demonstration.
 * 
 * WARNING: This controller should be disabled in production or restricted to admin access only.
 */
@RestController
@RequestMapping("/api/security/test")
public class SecurityMonitoringTestController {
    
    private final SecurityMonitoringService securityMonitoringService;
    
    public SecurityMonitoringTestController(SecurityMonitoringService securityMonitoringService) {
        this.securityMonitoringService = securityMonitoringService;
    }
    
    /**
     * POST /api/security/test/simulate-failed-login
     * 
     * Simulate a failed login attempt for testing.
     * 
     * @param email User email
     * @param count Number of failed attempts to simulate
     * @param httpRequest HTTP request
     * @return Test result
     */
    @PostMapping("/simulate-failed-login")
    public ResponseEntity<ApiResponse<String>> simulateFailedLogin(
            @RequestParam String email,
            @RequestParam(defaultValue = "1") int count,
            HttpServletRequest httpRequest) {
        
        for (int i = 0; i < count; i++) {
            securityMonitoringService.trackFailedLogin(
                email, 
                "Test failed login attempt " + (i + 1), 
                httpRequest
            );
        }
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Simulated " + count + " failed login attempt(s)", 
                "Failed logins tracked for email: " + email
            )
        );
    }
    
    /**
     * POST /api/security/test/simulate-brute-force
     * 
     * Simulate a brute force attack for testing alerts.
     * 
     * @param email User email
     * @param httpRequest HTTP request
     * @return Test result
     */
    @PostMapping("/simulate-brute-force")
    public ResponseEntity<ApiResponse<String>> simulateBruteForce(
            @RequestParam String email,
            HttpServletRequest httpRequest) {
        
        // Simulate 10 rapid failed login attempts (brute force threshold)
        for (int i = 0; i < 10; i++) {
            securityMonitoringService.trackFailedLogin(
                email, 
                "Brute force test attempt " + (i + 1), 
                httpRequest
            );
        }
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Simulated brute force attack", 
                "10 failed login attempts tracked - Alert should be generated"
            )
        );
    }
    
    /**
     * POST /api/security/test/simulate-suspicious-activity
     * 
     * Simulate suspicious activity for testing.
     * 
     * @param userId User ID
     * @param email User email
     * @param activityType Type of suspicious activity
     * @param httpRequest HTTP request
     * @return Test result
     */
    @PostMapping("/simulate-suspicious-activity")
    public ResponseEntity<ApiResponse<String>> simulateSuspiciousActivity(
            @RequestParam Long userId,
            @RequestParam String email,
            @RequestParam(defaultValue = "SUSPICIOUS_PATTERN") String activityType,
            HttpServletRequest httpRequest) {
        
        SecurityEventType eventType;
        try {
            eventType = SecurityEventType.valueOf(activityType);
        } catch (IllegalArgumentException e) {
            eventType = SecurityEventType.SUSPICIOUS_PATTERN;
        }
        
        SecurityEvent event = securityMonitoringService.trackSuspiciousActivity(
            userId,
            email,
            eventType,
            "Test suspicious activity - " + activityType,
            httpRequest
        );
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Simulated suspicious activity", 
                "Event ID: " + event.getEventId() + " - Alert generated"
            )
        );
    }
    
    /**
     * POST /api/security/test/simulate-new-device
     * 
     * Simulate a new device login for testing.
     * Note: This will track a successful login which will be detected as a new device
     * if the device fingerprint hasn't been seen before.
     * 
     * @param email User email
     * @param httpRequest HTTP request
     * @return Test result
     */
    @PostMapping("/simulate-new-device")
    public ResponseEntity<ApiResponse<String>> simulateNewDevice(
            @RequestParam String email,
            HttpServletRequest httpRequest) {
        
        SecurityEvent event = securityMonitoringService.trackSuccessfulLogin(email, httpRequest);
        
        boolean isNewDevice = event != null && Boolean.TRUE.equals(event.getIsNewDevice());
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Login tracked", 
                "New device detected: " + isNewDevice + " - Event ID: " + 
                (event != null ? event.getEventId() : "null")
            )
        );
    }
    
    /**
     * GET /api/security/test/info
     * 
     * Get information about testing endpoints.
     * 
     * @return Test information
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<TestInfo>> getTestInfo() {
        TestInfo info = new TestInfo();
        info.setMessage("Security monitoring test endpoints");
        info.setWarning("These endpoints should be disabled in production");
        info.setEndpoints(new String[]{
            "POST /api/security/test/simulate-failed-login?email={email}&count={count}",
            "POST /api/security/test/simulate-brute-force?email={email}",
            "POST /api/security/test/simulate-suspicious-activity?userId={userId}&email={email}",
            "POST /api/security/test/simulate-new-device?email={email}"
        });
        info.setThresholds(new String[]{
            "Medium risk: 3+ failed logins in 30 minutes",
            "High risk: 5+ failed logins in 30 minutes",
            "Critical risk: 10+ failed logins in 30 minutes",
            "Brute force: 5+ failed logins in 10 minutes"
        });
        
        return ResponseEntity.ok(
            ApiResponse.success("Test information retrieved", info)
        );
    }
    
    // DTO for test information
    public static class TestInfo {
        private String message;
        private String warning;
        private String[] endpoints;
        private String[] thresholds;
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getWarning() {
            return warning;
        }
        
        public void setWarning(String warning) {
            this.warning = warning;
        }
        
        public String[] getEndpoints() {
            return endpoints;
        }
        
        public void setEndpoints(String[] endpoints) {
            this.endpoints = endpoints;
        }
        
        public String[] getThresholds() {
            return thresholds;
        }
        
        public void setThresholds(String[] thresholds) {
            this.thresholds = thresholds;
        }
    }
}
