package com.securevault.controller;

import com.securevault.dto.ApiResponse;
import com.securevault.dto.SecurityEventResponse;
import com.securevault.service.SecurityMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SecurityMonitoringController
 * 
 * REST API for security monitoring and event tracking.
 * Provides endpoints for viewing security events, failed logins, and security alerts.
 */
@RestController
@RequestMapping("/api/security")
public class SecurityMonitoringController {
    
    private final SecurityMonitoringService securityMonitoringService;
    
    public SecurityMonitoringController(SecurityMonitoringService securityMonitoringService) {
        this.securityMonitoringService = securityMonitoringService;
    }
    
    /**
     * GET /api/security/events/{userId}
     * 
     * Get all security events for a user.
     * 
     * @param userId User ID
     * @param days Number of days to look back (default: 30)
     * @return List of security events
     */
    @GetMapping("/events/{userId}")
    public ResponseEntity<ApiResponse<List<SecurityEventResponse>>> getUserSecurityEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        
        List<SecurityEventResponse> events = securityMonitoringService.getUserSecurityEvents(userId, days);
        
        return ResponseEntity.ok(
            ApiResponse.success("Security events retrieved successfully", events)
        );
    }
    
    /**
     * GET /api/security/failed-logins
     * 
     * Get failed login attempts for an email.
     * 
     * @param email User email
     * @param hours Number of hours to look back (default: 24)
     * @return List of failed login attempts
     */
    @GetMapping("/failed-logins")
    public ResponseEntity<ApiResponse<List<SecurityEventResponse>>> getFailedLoginAttempts(
            @RequestParam String email,
            @RequestParam(defaultValue = "24") int hours) {
        
        List<SecurityEventResponse> failedAttempts = 
                securityMonitoringService.getFailedLoginAttempts(email, hours);
        
        return ResponseEntity.ok(
            ApiResponse.success("Failed login attempts retrieved", failedAttempts)
        );
    }
    
    /**
     * GET /api/security/high-risk-events
     * 
     * Get all high-risk security events.
     * 
     * @param days Number of days to look back (default: 7)
     * @return List of high-risk events
     */
    @GetMapping("/high-risk-events")
    public ResponseEntity<ApiResponse<List<SecurityEventResponse>>> getHighRiskEvents(
            @RequestParam(defaultValue = "7") int days) {
        
        List<SecurityEventResponse> highRiskEvents = 
                securityMonitoringService.getHighRiskEvents(days);
        
        return ResponseEntity.ok(
            ApiResponse.success("High-risk events retrieved", highRiskEvents)
        );
    }
    
    /**
     * GET /api/security/alerts
     * 
     * Get all events that triggered security alerts.
     * 
     * @return List of events with alerts
     */
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<SecurityEventResponse>>> getEventsWithAlerts() {
        
        List<SecurityEventResponse> alertEvents = 
                securityMonitoringService.getEventsWithAlerts();
        
        return ResponseEntity.ok(
            ApiResponse.success("Security alerts retrieved", alertEvents)
        );
    }
    
    /**
     * GET /api/security/new-device-logins/{userId}
     * 
     * Get new device login events for a user.
     * 
     * @param userId User ID
     * @return List of new device logins
     */
    @GetMapping("/new-device-logins/{userId}")
    public ResponseEntity<ApiResponse<List<SecurityEventResponse>>> getNewDeviceLogins(
            @PathVariable Long userId) {
        
        List<SecurityEventResponse> newDeviceLogins = 
                securityMonitoringService.getNewDeviceLogins(userId);
        
        return ResponseEntity.ok(
            ApiResponse.success("New device logins retrieved", newDeviceLogins)
        );
    }
}
