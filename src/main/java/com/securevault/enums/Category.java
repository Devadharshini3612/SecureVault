package com.securevault.enums;

/**
 * Category Enum for Credentials
 *
 * Defines the available categories for organizing credentials in SecureVault.
 * This helps users organize their credentials by purpose and makes searching easier.
 *
 * Usage Examples:
 * - PERSONAL: Personal email, social media, entertainment accounts
 * - WORK: Corporate accounts, professional services
 * - DEVELOPMENT: GitHub, AWS, Docker Hub, deployment services
 * - SOCIAL: Facebook, Twitter, Instagram, LinkedIn
 * - BANKING: Bank accounts, financial services, payment platforms
 * - ENTERTAINMENT: Netflix, Spotify, gaming platforms
 * - OTHER: Any credentials that don't fit other categories
 */
public enum Category {
    
    /**
     * Personal accounts and services
     * Examples: Personal email, personal cloud storage, personal subscriptions
     */
    PERSONAL("Personal"),
    
    /**
     * Work-related accounts and corporate services
     * Examples: Company email, corporate tools, business applications
     */
    WORK("Work"),
    
    /**
     * Development and technical services
     * Examples: GitHub, GitLab, AWS, Docker Hub, CI/CD platforms
     */
    DEVELOPMENT("Development"),
    
    /**
     * Social media and communication platforms
     * Examples: Facebook, Twitter, Instagram, LinkedIn, Discord
     */
    SOCIAL("Social"),
    
    /**
     * Banking and financial services
     * Examples: Online banking, payment platforms, investment accounts
     */
    BANKING("Banking"),
    
    /**
     * Entertainment and media services
     * Examples: Netflix, Spotify, Steam, gaming platforms, streaming services
     */
    ENTERTAINMENT("Entertainment"),
    
    /**
     * Other credentials that don't fit standard categories
     * Examples: Specialized tools, unique services, miscellaneous accounts
     */
    OTHER("Other");

    private final String displayName;

    /**
     * Constructor for Category enum
     * 
     * @param displayName the human-readable name for the category
     */
    Category(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the display name of the category
     * 
     * @return the human-readable category name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get Category from string value (case-insensitive)
     * 
     * @param value the string value to convert
     * @return the matching Category enum, or OTHER if not found
     */
    public static Category fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return OTHER;
        }
        
        try {
            return Category.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }

    /**
     * Check if a string is a valid category
     * 
     * @param value the string to check
     * @return true if the value represents a valid category
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        try {
            Category.valueOf(value.toUpperCase().trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}