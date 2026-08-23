package com.securevault.enums;

/**
 * SecurityEventType Enum
 * 
 * Defines all types of security events that can be monitored in the system.
 * Used for tracking and analyzing security-related activities.
 */
public enum SecurityEventType {
    
    // Authentication Events
    LOGIN_SUCCESS("Successful login"),
    LOGIN_FAILURE("Failed login attempt"),
    LOGOUT("User logout"),
    
    // Failed Authentication Patterns
    REPEATED_LOGIN_FAILURES("Multiple failed login attempts detected"),
    BRUTE_FORCE_ATTEMPT("Potential brute force attack detected"),
    
    // Device & Location Events
    NEW_DEVICE_LOGIN("Login from new device"),
    NEW_LOCATION_LOGIN("Login from new location"),
    SUSPICIOUS_LOCATION("Login from suspicious location"),
    
    // Account Security Events
    ACCOUNT_LOCKED("Account locked due to security policy"),
    ACCOUNT_UNLOCKED("Account unlocked"),
    PASSWORD_CHANGED("Password changed"),
    
    // Session Events
    SESSION_EXPIRED("Session expired"),
    CONCURRENT_SESSION_DETECTED("Multiple concurrent sessions detected"),
    
    // Suspicious Activities
    ABNORMAL_ACTIVITY("Abnormal user activity detected"),
    SUSPICIOUS_PATTERN("Suspicious behavior pattern detected"),
    RAPID_API_REQUESTS("Unusual number of API requests"),
    
    // Security Alerts
    SECURITY_ALERT_GENERATED("Security alert created"),
    SECURITY_POLICY_VIOLATION("Security policy violated");
    
    private final String description;
    
    SecurityEventType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
