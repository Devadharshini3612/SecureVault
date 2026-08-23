package com.securevault.enums;

/**
 * Enum for different Two-Factor Authentication methods
 */
public enum TwoFactorMethod {
    SMS("SMS"),
    EMAIL("Email"), 
    AUTHENTICATOR("Authenticator App");
    
    private final String displayName;
    
    TwoFactorMethod(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}