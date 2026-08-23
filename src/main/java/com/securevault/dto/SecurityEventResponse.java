package com.securevault.dto;

import com.securevault.enums.RiskLevel;
import com.securevault.enums.SecurityEventType;
import java.time.LocalDateTime;

/**
 * SecurityEventResponse DTO
 * 
 * Response object for security event data.
 */
public class SecurityEventResponse {
    
    private Long eventId;
    private Long userId;
    private String email;
    private SecurityEventType eventType;
    private RiskLevel riskLevel;
    private String ipAddress;
    private String userAgent;
    private String deviceFingerprint;
    private String location;
    private Boolean success;
    private String details;
    private String reason;
    private Integer failedAttemptCount;
    private Boolean isNewDevice;
    private Boolean alertGenerated;
    private LocalDateTime timestamp;
    
    // Constructors
    
    public SecurityEventResponse() {
    }
    
    // Getters and Setters
    
    public Long getEventId() {
        return eventId;
    }
    
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public SecurityEventType getEventType() {
        return eventType;
    }
    
    public void setEventType(SecurityEventType eventType) {
        this.eventType = eventType;
    }
    
    public RiskLevel getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }
    
    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public Integer getFailedAttemptCount() {
        return failedAttemptCount;
    }
    
    public void setFailedAttemptCount(Integer failedAttemptCount) {
        this.failedAttemptCount = failedAttemptCount;
    }
    
    public Boolean getIsNewDevice() {
        return isNewDevice;
    }
    
    public void setIsNewDevice(Boolean isNewDevice) {
        this.isNewDevice = isNewDevice;
    }
    
    public Boolean getAlertGenerated() {
        return alertGenerated;
    }
    
    public void setAlertGenerated(Boolean alertGenerated) {
        this.alertGenerated = alertGenerated;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
