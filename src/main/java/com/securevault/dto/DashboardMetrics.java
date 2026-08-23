package com.securevault.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DashboardMetrics DTO
 * 
 * Comprehensive metrics for the analytics dashboard.
 * Combines all key metrics in a single response.
 */
public class DashboardMetrics {
    
    // Credential Statistics
    private Integer totalCredentials;
    private Integer sharedCredentials;
    private Integer weakPasswords;
    private Integer recentlyAddedCredentials; // Last 7 days
    
    // Security Statistics
    private Integer failedLoginCount; // Last 24 hours
    private Integer securityAlerts; // Active alerts
    private Integer suspiciousActivities; // Last 7 days
    private Integer newDeviceLogins; // Last 30 days
    
    // User Activity
    private List<RecentActivity> recentUserActivity;
    private Integer totalUsers;
    private Integer activeUsers; // Active in last 30 days
    
    // Health Scores
    private Double passwordHealthScore; // 0-100
    private Double securityScore; // 0-100
    
    // Trends
    private List<TrendData> credentialsTrend; // Last 7 days
    private List<TrendData> loginsTrend; // Last 7 days
    
    // Quick Stats
    private QuickStats quickStats;
    
    // Nested class for recent activity
    public static class RecentActivity {
        private String action;
        private String entityType;
        private String details;
        private LocalDateTime timestamp;
        private Long userId;
        private String userEmail;
        
        public RecentActivity() {
        }
        
        // Getters and Setters
        public String getAction() {
            return action;
        }
        
        public void setAction(String action) {
            this.action = action;
        }
        
        public String getEntityType() {
            return entityType;
        }
        
        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }
        
        public String getDetails() {
            return details;
        }
        
        public void setDetails(String details) {
            this.details = details;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public String getUserEmail() {
            return userEmail;
        }
        
        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }
    }
    
    // Nested class for trend data
    public static class TrendData {
        private String date;
        private Integer count;
        
        public TrendData() {
        }
        
        public TrendData(String date, Integer count) {
            this.date = date;
            this.count = count;
        }
        
        // Getters and Setters
        public String getDate() {
            return date;
        }
        
        public void setDate(String date) {
            this.date = date;
        }
        
        public Integer getCount() {
            return count;
        }
        
        public void setCount(Integer count) {
            this.count = count;
        }
    }
    
    // Nested class for quick stats
    public static class QuickStats {
        private Integer todayLogins;
        private Integer todayFailedLogins;
        private Integer todayNewCredentials;
        private Integer todayShares;
        
        public QuickStats() {
        }
        
        // Getters and Setters
        public Integer getTodayLogins() {
            return todayLogins;
        }
        
        public void setTodayLogins(Integer todayLogins) {
            this.todayLogins = todayLogins;
        }
        
        public Integer getTodayFailedLogins() {
            return todayFailedLogins;
        }
        
        public void setTodayFailedLogins(Integer todayFailedLogins) {
            this.todayFailedLogins = todayFailedLogins;
        }
        
        public Integer getTodayNewCredentials() {
            return todayNewCredentials;
        }
        
        public void setTodayNewCredentials(Integer todayNewCredentials) {
            this.todayNewCredentials = todayNewCredentials;
        }
        
        public Integer getTodayShares() {
            return todayShares;
        }
        
        public void setTodayShares(Integer todayShares) {
            this.todayShares = todayShares;
        }
    }
    
    // Constructors
    public DashboardMetrics() {
    }
    
    // Getters and Setters
    public Integer getTotalCredentials() {
        return totalCredentials;
    }
    
    public void setTotalCredentials(Integer totalCredentials) {
        this.totalCredentials = totalCredentials;
    }
    
    public Integer getSharedCredentials() {
        return sharedCredentials;
    }
    
    public void setSharedCredentials(Integer sharedCredentials) {
        this.sharedCredentials = sharedCredentials;
    }
    
    public Integer getWeakPasswords() {
        return weakPasswords;
    }
    
    public void setWeakPasswords(Integer weakPasswords) {
        this.weakPasswords = weakPasswords;
    }
    
    public Integer getRecentlyAddedCredentials() {
        return recentlyAddedCredentials;
    }
    
    public void setRecentlyAddedCredentials(Integer recentlyAddedCredentials) {
        this.recentlyAddedCredentials = recentlyAddedCredentials;
    }
    
    public Integer getFailedLoginCount() {
        return failedLoginCount;
    }
    
    public void setFailedLoginCount(Integer failedLoginCount) {
        this.failedLoginCount = failedLoginCount;
    }
    
    public Integer getSecurityAlerts() {
        return securityAlerts;
    }
    
    public void setSecurityAlerts(Integer securityAlerts) {
        this.securityAlerts = securityAlerts;
    }
    
    public Integer getSuspiciousActivities() {
        return suspiciousActivities;
    }
    
    public void setSuspiciousActivities(Integer suspiciousActivities) {
        this.suspiciousActivities = suspiciousActivities;
    }
    
    public Integer getNewDeviceLogins() {
        return newDeviceLogins;
    }
    
    public void setNewDeviceLogins(Integer newDeviceLogins) {
        this.newDeviceLogins = newDeviceLogins;
    }
    
    public List<RecentActivity> getRecentUserActivity() {
        return recentUserActivity;
    }
    
    public void setRecentUserActivity(List<RecentActivity> recentUserActivity) {
        this.recentUserActivity = recentUserActivity;
    }
    
    public Integer getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public Integer getActiveUsers() {
        return activeUsers;
    }
    
    public void setActiveUsers(Integer activeUsers) {
        this.activeUsers = activeUsers;
    }
    
    public Double getPasswordHealthScore() {
        return passwordHealthScore;
    }
    
    public void setPasswordHealthScore(Double passwordHealthScore) {
        this.passwordHealthScore = passwordHealthScore;
    }
    
    public Double getSecurityScore() {
        return securityScore;
    }
    
    public void setSecurityScore(Double securityScore) {
        this.securityScore = securityScore;
    }
    
    public List<TrendData> getCredentialsTrend() {
        return credentialsTrend;
    }
    
    public void setCredentialsTrend(List<TrendData> credentialsTrend) {
        this.credentialsTrend = credentialsTrend;
    }
    
    public List<TrendData> getLoginsTrend() {
        return loginsTrend;
    }
    
    public void setLoginsTrend(List<TrendData> loginsTrend) {
        this.loginsTrend = loginsTrend;
    }
    
    public QuickStats getQuickStats() {
        return quickStats;
    }
    
    public void setQuickStats(QuickStats quickStats) {
        this.quickStats = quickStats;
    }
}
