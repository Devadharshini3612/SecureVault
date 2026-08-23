package com.securevault.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * LoginActivityReport DTO
 * 
 * Comprehensive report of user login activities and patterns.
 */
public class LoginActivityReport {
    
    private Long userId;
    private String email;
    private Integer totalLogins;
    private Integer successfulLogins;
    private Integer failedLogins;
    private Integer newDeviceLogins;
    private LocalDateTime lastSuccessfulLogin;
    private LocalDateTime lastFailedLogin;
    private List<LoginAttempt> recentAttempts;
    private List<DeviceInfo> devices;
    private List<String> locations;
    private Map<String, Integer> loginsByHour;
    private Map<String, Integer> loginsByDay;
    
    // Nested class for login attempts
    public static class LoginAttempt {
        private LocalDateTime timestamp;
        private Boolean success;
        private String ipAddress;
        private String location;
        private String device;
        private String reason;
        
        public LoginAttempt() {
        }
        
        public LoginAttempt(LocalDateTime timestamp, Boolean success, String ipAddress, String location, String device, String reason) {
            this.timestamp = timestamp;
            this.success = success;
            this.ipAddress = ipAddress;
            this.location = location;
            this.device = device;
            this.reason = reason;
        }
        
        // Getters and Setters
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
        
        public Boolean getSuccess() {
            return success;
        }
        
        public void setSuccess(Boolean success) {
            this.success = success;
        }
        
        public String getIpAddress() {
            return ipAddress;
        }
        
        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }
        
        public String getLocation() {
            return location;
        }
        
        public void setLocation(String location) {
            this.location = location;
        }
        
        public String getDevice() {
            return device;
        }
        
        public void setDevice(String device) {
            this.device = device;
        }
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
    }
    
    // Nested class for device information
    public static class DeviceInfo {
        private String deviceFingerprint;
        private String userAgent;
        private LocalDateTime firstSeen;
        private LocalDateTime lastUsed;
        private Integer loginCount;
        
        public DeviceInfo() {
        }
        
        // Getters and Setters
        public String getDeviceFingerprint() {
            return deviceFingerprint;
        }
        
        public void setDeviceFingerprint(String deviceFingerprint) {
            this.deviceFingerprint = deviceFingerprint;
        }
        
        public String getUserAgent() {
            return userAgent;
        }
        
        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }
        
        public LocalDateTime getFirstSeen() {
            return firstSeen;
        }
        
        public void setFirstSeen(LocalDateTime firstSeen) {
            this.firstSeen = firstSeen;
        }
        
        public LocalDateTime getLastUsed() {
            return lastUsed;
        }
        
        public void setLastUsed(LocalDateTime lastUsed) {
            this.lastUsed = lastUsed;
        }
        
        public Integer getLoginCount() {
            return loginCount;
        }
        
        public void setLoginCount(Integer loginCount) {
            this.loginCount = loginCount;
        }
    }
    
    // Constructors
    public LoginActivityReport() {
    }
    
    // Getters and Setters
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
    
    public Integer getTotalLogins() {
        return totalLogins;
    }
    
    public void setTotalLogins(Integer totalLogins) {
        this.totalLogins = totalLogins;
    }
    
    public Integer getSuccessfulLogins() {
        return successfulLogins;
    }
    
    public void setSuccessfulLogins(Integer successfulLogins) {
        this.successfulLogins = successfulLogins;
    }
    
    public Integer getFailedLogins() {
        return failedLogins;
    }
    
    public void setFailedLogins(Integer failedLogins) {
        this.failedLogins = failedLogins;
    }
    
    public Integer getNewDeviceLogins() {
        return newDeviceLogins;
    }
    
    public void setNewDeviceLogins(Integer newDeviceLogins) {
        this.newDeviceLogins = newDeviceLogins;
    }
    
    public LocalDateTime getLastSuccessfulLogin() {
        return lastSuccessfulLogin;
    }
    
    public void setLastSuccessfulLogin(LocalDateTime lastSuccessfulLogin) {
        this.lastSuccessfulLogin = lastSuccessfulLogin;
    }
    
    public LocalDateTime getLastFailedLogin() {
        return lastFailedLogin;
    }
    
    public void setLastFailedLogin(LocalDateTime lastFailedLogin) {
        this.lastFailedLogin = lastFailedLogin;
    }
    
    public List<LoginAttempt> getRecentAttempts() {
        return recentAttempts;
    }
    
    public void setRecentAttempts(List<LoginAttempt> recentAttempts) {
        this.recentAttempts = recentAttempts;
    }
    
    public List<DeviceInfo> getDevices() {
        return devices;
    }
    
    public void setDevices(List<DeviceInfo> devices) {
        this.devices = devices;
    }
    
    public List<String> getLocations() {
        return locations;
    }
    
    public void setLocations(List<String> locations) {
        this.locations = locations;
    }
    
    public Map<String, Integer> getLoginsByHour() {
        return loginsByHour;
    }
    
    public void setLoginsByHour(Map<String, Integer> loginsByHour) {
        this.loginsByHour = loginsByHour;
    }
    
    public Map<String, Integer> getLoginsByDay() {
        return loginsByDay;
    }
    
    public void setLoginsByDay(Map<String, Integer> loginsByDay) {
        this.loginsByDay = loginsByDay;
    }
}
