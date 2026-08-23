package com.securevault.mapper;

import com.securevault.dto.*;
import com.securevault.entity.Credential;
import com.securevault.entity.User;
import com.securevault.util.AESUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO/Entity Mapping Utility
 * 
 * Handles conversions between DTOs (Data Transfer Objects) and JPA Entities.
 * This ensures clean separation between API layer (DTOs) and persistence layer (Entities).
 * 
 * Key Benefits:
 * - Centralized mapping logic
 * - Type-safe conversions
 * - Consistent field transformations
 * - Easy to test and maintain
 * 
 * Security Notes:
 * - Handles password encryption/decryption transparently
 * - Never exposes sensitive entity fields in DTOs
 * - Validates required fields during conversion
 */
@Component
public class DtoEntityMapper {

    // ========================================
    // USER MAPPINGS
    // ========================================

    /**
     * Convert User Entity to UserResponse DTO
     * Used when returning user information in API responses
     */
    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserResponse(
            user.getUserId(),
            user.getName(),
            user.getEmail()
        );
    }

    /**
     * Convert RegisterRequest DTO to User Entity
     * Used during user registration
     */
    public User toUserEntity(RegisterRequest request) {
        if (request == null) {
            return null;
        }
        
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        // Note: Password will be hashed in the service layer
        user.setPasswordHash(request.getPassword());
        
        return user;
    }

    /**
     * Convert List of User entities to List of UserResponse DTOs
     */
    public List<UserResponse> toUserResponseList(List<User> users) {
        if (users == null) {
            return null;
        }
        
        return users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    // ========================================
    // CREDENTIAL MAPPINGS
    // ========================================

    /**
     * Convert Credential Entity to CredentialResponse DTO
     * Used when returning credential information in API responses
     * 
     * IMPORTANT: This method decrypts the password from the entity
     */
    public CredentialResponse toCredentialResponse(Credential credential) {
        if (credential == null) {
            return null;
        }
        
        // Decrypt the password for the response
        String decryptedPassword;
        try {
            decryptedPassword = AESUtil.decrypt(credential.getEncryptedPassword());
        } catch (Exception e) {
            // Log the error but don't expose sensitive information
            System.err.println("Failed to decrypt password for credential ID: " + credential.getCredentialId());
            decryptedPassword = "[DECRYPTION_FAILED]";
        }
        
        return new CredentialResponse(
            credential.getCredentialId(),
            credential.getUserId(),
            credential.getServiceName(),
            credential.getUsername(),
            decryptedPassword,
            credential.getCategory(),
            credential.getCreatedAt(),
            credential.getUpdatedAt()
        );
    }

    /**
     * Convert CreateCredentialRequest DTO to Credential Entity
     * Used when creating new credentials
     * 
     * IMPORTANT: This method encrypts the password before storing in entity
     */
    public Credential toCredentialEntity(CreateCredentialRequest request) {
        if (request == null) {
            return null;
        }
        
        // Encrypt the password before storing
        String encryptedPassword;
        try {
            encryptedPassword = AESUtil.encrypt(request.getPassword());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt password: " + e.getMessage(), e);
        }
        
        Credential credential = new Credential();
        credential.setUserId(request.getUserId());
        credential.setServiceName(request.getServiceName());
        credential.setUsername(request.getUsername());
        credential.setEncryptedPassword(encryptedPassword);
        credential.setCategory(request.getCategory());
        
        return credential;
    }

    /**
     * Update Credential Entity from UpdateCredentialRequest DTO
     * Used when updating existing credentials
     * 
     * Only updates fields that are provided (non-null) in the request
     */
    public void updateCredentialEntity(Credential credential, UpdateCredentialRequest request) {
        if (credential == null || request == null) {
            return;
        }
        
        // Update service name if provided
        if (request.getServiceName() != null && !request.getServiceName().trim().isEmpty()) {
            credential.setServiceName(request.getServiceName().trim());
        }
        
        // Update username if provided
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            credential.setUsername(request.getUsername().trim());
        }
        
        // Update password if provided (encrypt it)
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            try {
                String encryptedPassword = AESUtil.encrypt(request.getPassword());
                credential.setEncryptedPassword(encryptedPassword);
            } catch (Exception e) {
                throw new RuntimeException("Failed to encrypt updated password: " + e.getMessage(), e);
            }
        }
        
        // Update category if provided
        if (request.getCategory() != null) {
            credential.setCategory(request.getCategory());
        }
        
        // Update timestamp (will be handled by @PreUpdate in entity)
        credential.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Convert List of Credential entities to List of CredentialResponse DTOs
     */
    public List<CredentialResponse> toCredentialResponseList(List<Credential> credentials) {
        if (credentials == null) {
            return null;
        }
        
        return credentials.stream()
                .map(this::toCredentialResponse)
                .collect(Collectors.toList());
    }

    // ========================================
    // LOGIN RESPONSE MAPPING
    // ========================================

    /**
     * Create LoginResponse DTO from User Entity and JWT token
     * Used after successful authentication
     */
    public LoginResponse toLoginResponse(User user, String jwtToken) {
        if (user == null || jwtToken == null) {
            return null;
        }
        
        return new LoginResponse(
            jwtToken,
            user.getEmail(),
            user.getUserId(),
            "Login successful"
        );
    }

    // ========================================
    // PASSWORD UTILITY MAPPINGS
    // ========================================

    /**
     * Create PasswordStrengthResponse from analysis results
     */
    public PasswordStrengthResponse toPasswordStrengthResponse(String password, int score, String rating, List<String> feedback) {
        if (password == null) {
            return null;
        }
        
        // PasswordStrengthResponse doesn't have PasswordAnalysis inner class
        // Just return basic response with score, rating, and feedback
        return new PasswordStrengthResponse(score, rating, feedback);
    }

    /**
     * Create PasswordGeneratorResponse for single password
     */
    public PasswordGeneratorResponse toPasswordGeneratorResponse(String password, int length, int strength, boolean uppercase, boolean lowercase, boolean digits, boolean special) {
        if (password == null) {
            return null;
        }
        
        // Determine strength rating based on score
        String strengthRating = getStrengthRating(strength);
        
        return new PasswordGeneratorResponse(password, length, strength, strengthRating);
    }

    /**
     * Create PasswordGeneratorResponse for multiple passwords
     * Returns the first password with its strength rating
     */
    public PasswordGeneratorResponse toPasswordGeneratorResponse(List<String> passwords, int length, boolean uppercase, boolean lowercase, boolean digits, boolean special) {
        if (passwords == null || passwords.isEmpty()) {
            return null;
        }
        
        // Use the first password
        String password = passwords.get(0);
        
        // Calculate strength (simple estimation)
        int strength = 50; // Default medium strength
        if (uppercase && lowercase && digits && special) {
            strength = 90;
        } else if ((uppercase || lowercase) && digits && special) {
            strength = 75;
        } else if ((uppercase || lowercase) && (digits || special)) {
            strength = 60;
        }
        
        String strengthRating = getStrengthRating(strength);
        
        return new PasswordGeneratorResponse(password, length, strength, strengthRating);
    }

    /**
     * Create PinGeneratorResponse
     */
    public PinGeneratorResponse toPinGeneratorResponse(String pin) {
        if (pin == null) {
            return null;
        }
        
        return new PinGeneratorResponse(pin, pin.length());
    }

    // ========================================
    // PRIVATE HELPER METHODS
    // ========================================

    /**
     * Get strength rating based on score
     */
    private String getStrengthRating(int score) {
        if (score >= 80) {
            return "Very Strong";
        } else if (score >= 60) {
            return "Strong";
        } else if (score >= 40) {
            return "Moderate";
        } else if (score >= 20) {
            return "Weak";
        } else {
            return "Very Weak";
        }
    }

    /**
     * Check for repeated characters in password
     */
    private boolean hasRepeatedCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) && password.charAt(i) == password.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for sequential characters in password
     */
    private boolean hasSequentialCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            char first = password.charAt(i);
            char second = password.charAt(i + 1);
            char third = password.charAt(i + 2);
            
            // Check for sequential numbers (123, 456, etc.)
            if (Character.isDigit(first) && Character.isDigit(second) && Character.isDigit(third)) {
                if (second == first + 1 && third == second + 1) {
                    return true;
                }
            }
            
            // Check for sequential letters (abc, def, etc.)
            if (Character.isLetter(first) && Character.isLetter(second) && Character.isLetter(third)) {
                if (Character.toLowerCase(second) == Character.toLowerCase(first) + 1 &&
                    Character.toLowerCase(third) == Character.toLowerCase(second) + 1) {
                    return true;
                }
            }
        }
        return false;
    }
}