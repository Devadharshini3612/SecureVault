package com.securevault.exception;

/**
 * Exception thrown when attempting to register with an email that already exists.
 * 
 * Used in scenarios like:
 * - User registration with existing email
 * - User profile update with conflicting email
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create exception for duplicate email during registration
     */
    public static DuplicateEmailException forRegistration(String email) {
        return new DuplicateEmailException("Email already registered: " + email);
    }

    /**
     * Create exception for duplicate email during update
     */
    public static DuplicateEmailException forUpdate(String email) {
        return new DuplicateEmailException("Email already in use by another user: " + email);
    }
}