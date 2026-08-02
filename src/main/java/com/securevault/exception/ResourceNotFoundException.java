package com.securevault.exception;

/**
 * ResourceNotFoundException
 *
 * Custom exception thrown when a requested resource is not found.
 * Examples:
 * - User not found by email
 * - Credential not found by ID
 * - Credential not found for specific user (authorization check)
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
