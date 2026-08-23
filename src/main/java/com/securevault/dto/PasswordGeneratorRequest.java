package com.securevault.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * PasswordGeneratorRequest DTO
 *
 * Request object for generating custom passwords.
 * Allows users to specify length and character types to include.
 */
public class PasswordGeneratorRequest {

    /**
     * Desired password length
     * Must be between 8 and 128 characters
     */
    @NotNull(message = "Password length is required")
    @Min(value = 8, message = "Password length must be at least 8 characters")
    @Max(value = 128, message = "Password length cannot exceed 128 characters")
    private Integer length = 16;

    /**
     * Include uppercase letters (A-Z)
     */
    private boolean includeUppercase = true;

    /**
     * Include lowercase letters (a-z)
     */
    private boolean includeLowercase = true;

    /**
     * Include digits (0-9)
     */
    private boolean includeDigits = true;

    /**
     * Include special characters (!@#$%^&*)
     */
    private boolean includeSpecial = true;

    // ========== Constructors ==========

    public PasswordGeneratorRequest() {
    }

    public PasswordGeneratorRequest(Integer length, boolean includeUppercase, boolean includeLowercase, 
                                   boolean includeDigits, boolean includeSpecial) {
        this.length = length;
        this.includeUppercase = includeUppercase;
        this.includeLowercase = includeLowercase;
        this.includeDigits = includeDigits;
        this.includeSpecial = includeSpecial;
    }

    // ========== Getters and Setters ==========

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public boolean isIncludeUppercase() {
        return includeUppercase;
    }

    public void setIncludeUppercase(boolean includeUppercase) {
        this.includeUppercase = includeUppercase;
    }

    public boolean isIncludeLowercase() {
        return includeLowercase;
    }

    public void setIncludeLowercase(boolean includeLowercase) {
        this.includeLowercase = includeLowercase;
    }

    public boolean isIncludeDigits() {
        return includeDigits;
    }

    public void setIncludeDigits(boolean includeDigits) {
        this.includeDigits = includeDigits;
    }

    public boolean isIncludeSpecial() {
        return includeSpecial;
    }

    public void setIncludeSpecial(boolean includeSpecial) {
        this.includeSpecial = includeSpecial;
    }
}
