package com.securevault.dto;

import java.util.List;

/**
 * PasswordHealthReport DTO
 * 
 * Report on password health and security status across all credentials.
 */
public class PasswordHealthReport {
    
    private Integer totalCredentials;
    private Integer weakPasswords;
    private Integer moderatePasswords;
    private Integer strongPasswords;
    private Integer reusedPasswords;
    private Integer oldPasswords; // Passwords not changed in 90+ days
    private Double overallHealthScore; // 0-100
    private List<WeakPasswordDetail> weakPasswordDetails;
    private List<String> recommendations;
    
    // Nested class for weak password details
    public static class WeakPasswordDetail {
        private Long credentialId;
        private String serviceName;
        private String reason;
        private String recommendation;
        
        public WeakPasswordDetail() {
        }
        
        public WeakPasswordDetail(Long credentialId, String serviceName, String reason, String recommendation) {
            this.credentialId = credentialId;
            this.serviceName = serviceName;
            this.reason = reason;
            this.recommendation = recommendation;
        }
        
        // Getters and Setters
        public Long getCredentialId() {
            return credentialId;
        }
        
        public void setCredentialId(Long credentialId) {
            this.credentialId = credentialId;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
        
        public String getRecommendation() {
            return recommendation;
        }
        
        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }
    }
    
    // Constructors
    public PasswordHealthReport() {
    }
    
    // Getters and Setters
    public Integer getTotalCredentials() {
        return totalCredentials;
    }
    
    public void setTotalCredentials(Integer totalCredentials) {
        this.totalCredentials = totalCredentials;
    }
    
    public Integer getWeakPasswords() {
        return weakPasswords;
    }
    
    public void setWeakPasswords(Integer weakPasswords) {
        this.weakPasswords = weakPasswords;
    }
    
    public Integer getModeratePasswords() {
        return moderatePasswords;
    }
    
    public void setModeratePasswords(Integer moderatePasswords) {
        this.moderatePasswords = moderatePasswords;
    }
    
    public Integer getStrongPasswords() {
        return strongPasswords;
    }
    
    public void setStrongPasswords(Integer strongPasswords) {
        this.strongPasswords = strongPasswords;
    }
    
    public Integer getReusedPasswords() {
        return reusedPasswords;
    }
    
    public void setReusedPasswords(Integer reusedPasswords) {
        this.reusedPasswords = reusedPasswords;
    }
    
    public Integer getOldPasswords() {
        return oldPasswords;
    }
    
    public void setOldPasswords(Integer oldPasswords) {
        this.oldPasswords = oldPasswords;
    }
    
    public Double getOverallHealthScore() {
        return overallHealthScore;
    }
    
    public void setOverallHealthScore(Double overallHealthScore) {
        this.overallHealthScore = overallHealthScore;
    }
    
    public List<WeakPasswordDetail> getWeakPasswordDetails() {
        return weakPasswordDetails;
    }
    
    public void setWeakPasswordDetails(List<WeakPasswordDetail> weakPasswordDetails) {
        this.weakPasswordDetails = weakPasswordDetails;
    }
    
    public List<String> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}
