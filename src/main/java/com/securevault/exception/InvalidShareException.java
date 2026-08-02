package com.securevault.exception;

/**
 * InvalidShareException
 *
 * Thrown when a share operation violates business rules:
 * - Attempting to share with yourself
 * - Sharing deleted credentials
 * - Duplicate share attempts
 * - Invalid share configuration
 */
public class InvalidShareException extends RuntimeException {
    
    public InvalidShareException(String message) {
        super(message);
    }
}
