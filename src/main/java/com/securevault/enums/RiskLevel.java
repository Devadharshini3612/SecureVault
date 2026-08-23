package com.securevault.enums;

/**
 * RiskLevel Enum
 * 
 * Classifies the severity/risk level of security events.
 * Used for prioritizing security alerts and monitoring.
 */
public enum RiskLevel {
    
    LOW("Low risk - Normal activity"),
    MEDIUM("Medium risk - Requires attention"),
    HIGH("High risk - Immediate action required"),
    CRITICAL("Critical risk - Severe security threat");
    
    private final String description;
    
    RiskLevel(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Determine risk level based on failed login count
     */
    public static RiskLevel fromFailedLoginCount(int failedAttempts) {
        if (failedAttempts >= 10) {
            return CRITICAL;
        } else if (failedAttempts >= 5) {
            return HIGH;
        } else if (failedAttempts >= 3) {
            return MEDIUM;
        } else {
            return LOW;
        }
    }
}
