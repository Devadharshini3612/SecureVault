package com.securevault.dto;

import com.securevault.enums.RiskLevel;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SecuritySummary DTO
 * 
 * Overall security summary for a user or the entire system.
 */
public class SecuritySummary {
    
    private RiskLevel overallRiskLevel;
    private Integer totalSecurityEvents;
    private Integer highRiskEvents;
    private Integer failedLoginAttempts;
    private Integer suspiciousActivities;
    private Integer activeAlerts;
    private LocalDateTime lastSecurityIncident;
    private List<SecurityAlert> recentAlerts;
    private List<String> recommendations;
    private SecurityScore securityScore;
    
    // Nested class for security alerts
    public static class SecurityAlert {
        private String alertType;
        private String description;
        private RiskLevel riskLevel;
        private LocalDateTime timestamp;
        private String actionTaken;
        
        public SecurityAlert() {
        }
        
        public SecurityAlert(String alertType, String description, RiskLevel riskLevel, LocalDateTime timestamp) {
            this.alertType = alertType;
            this.description = description;
            this.riskLevel = riskLevel;
            this.timestamp = timestamp;
        }
        
        // Getters and Setters
        public String getAlertType() {
            return alertType;
        }
        
        public void setAlertType(String alertType) {
            this.alertType = alertType;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public RiskLevel getRiskLevel() {
            return riskLevel;
        }
        
        public void setRiskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
        
        public String getActionTaken() {
            return actionTaken;
        }
        
        public void setActionTaken(String actionTaken) {
            this.actionTaken = actionTaken;
        }
    }
    
    // Nested class for security score breakdown
    public static class SecurityScore {
        private Double overall; // 0-100
        private Double passwordStrength; // 0-100
        private Double loginSecurity; // 0-100
        private Double activityPattern; // 0-100
        private String rating; // Excellent, Good, Fair, Poor
        
        public SecurityScore() {
        }
        
        // Getters and Setters
        public Double getOverall() {
            return overall;
        }
        
        public void setOverall(Double overall) {
            this.overall = overall;
        }
        
        public Double getPasswordStrength() {
            return passwordStrength;
        }
        
        public void setPasswordStrength(Double passwordStrength) {
            this.passwordStrength = passwordStrength;
        }
        
        public Double getLoginSecurity() {
            return loginSecurity;
        }
        
        public void setLoginSecurity(Double loginSecurity) {
            this.loginSecurity = loginSecurity;
        }
        
        public Double getActivityPattern() {
            return activityPattern;
        }
        
        public void setActivityPattern(Double activityPattern) {
            this.activityPattern = activityPattern;
        }
        
        public String getRating() {
            return rating;
        }
        
        public void setRating(String rating) {
            this.rating = rating;
        }
    }
    
    // Constructors
    public SecuritySummary() {
    }
    
    // Getters and Setters
    public RiskLevel getOverallRiskLevel() {
        return overallRiskLevel;
    }
    
    public void setOverallRiskLevel(RiskLevel overallRiskLevel) {
        this.overallRiskLevel = overallRiskLevel;
    }
    
    public Integer getTotalSecurityEvents() {
        return totalSecurityEvents;
    }
    
    public void setTotalSecurityEvents(Integer totalSecurityEvents) {
        this.totalSecurityEvents = totalSecurityEvents;
    }
    
    public Integer getHighRiskEvents() {
        return highRiskEvents;
    }
    
    public void setHighRiskEvents(Integer highRiskEvents) {
        this.highRiskEvents = highRiskEvents;
    }
    
    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }
    
    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }
    
    public Integer getSuspiciousActivities() {
        return suspiciousActivities;
    }
    
    public void setSuspiciousActivities(Integer suspiciousActivities) {
        this.suspiciousActivities = suspiciousActivities;
    }
    
    public Integer getActiveAlerts() {
        return activeAlerts;
    }
    
    public void setActiveAlerts(Integer activeAlerts) {
        this.activeAlerts = activeAlerts;
    }
    
    public LocalDateTime getLastSecurityIncident() {
        return lastSecurityIncident;
    }
    
    public void setLastSecurityIncident(LocalDateTime lastSecurityIncident) {
        this.lastSecurityIncident = lastSecurityIncident;
    }
    
    public List<SecurityAlert> getRecentAlerts() {
        return recentAlerts;
    }
    
    public void setRecentAlerts(List<SecurityAlert> recentAlerts) {
        this.recentAlerts = recentAlerts;
    }
    
    public List<String> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
    
    public SecurityScore getSecurityScore() {
        return securityScore;
    }
    
    public void setSecurityScore(SecurityScore securityScore) {
        this.securityScore = securityScore;
    }
}
