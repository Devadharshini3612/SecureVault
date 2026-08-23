package com.securevault.entity;

import com.securevault.enums.RiskLevel;
import com.securevault.enums.SecurityEventType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * SecurityEvent Entity
 * 
 * Stores all security-related events for monitoring and analysis.
 * Tracks login attempts, suspicious activities, device information, and risk levels.
 */
@Entity
@Table(name = "security_events", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_event_type", columnList = "event_type"),
    @Index(name = "idx_risk_level", columnList = "risk_level"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_ip_address", columnList = "ip_address")
})
public class SecurityEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;
    
    /**
     * User associated with this event (nullable for failed login attempts with invalid email)
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * Email used in the login attempt (stored even if user doesn't exist)
     */
    @Column(name = "email")
    private String email;
    
    /**
     * Type of security event
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private SecurityEventType eventType;
    
    /**
     * Risk level classification
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel;
    
    /**
     * IP address of the request
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    /**
     * User agent string (browser/device info)
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    /**
     * Device fingerprint (unique identifier for device)
     */
    @Column(name = "device_fingerprint", length = 255)
    private String deviceFingerprint;
    
    /**
     * Geographic location (city, country)
     */
    @Column(name = "location", length = 255)
    private String location;
    
    /**
     * Whether the event was successful (e.g., successful vs failed login)
     */
    @Column(name = "success")
    private Boolean success;
    
    /**
     * Additional details about the event
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    /**
     * Reason for the event (e.g., "Invalid password", "Account locked")
     */
    @Column(name = "reason", length = 255)
    private String reason;
    
    /**
     * Number of failed attempts before this event
     */
    @Column(name = "failed_attempt_count")
    private Integer failedAttemptCount;
    
    /**
     * Whether this is a new device
     */
    @Column(name = "is_new_device")
    private Boolean isNewDevice;
    
    /**
     * Whether this triggered a security alert
     */
    @Column(name = "alert_generated")
    private Boolean alertGenerated;
    
    /**
     * Timestamp when the event occurred
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * Default constructor
     */
    public SecurityEvent() {
        this.timestamp = LocalDateTime.now();
        this.alertGenerated = false;
        this.isNewDevice = false;
        this.failedAttemptCount = 0;
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
