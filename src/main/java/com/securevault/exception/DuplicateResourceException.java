package com.securevault.exception;

/**
 * DuplicateResourceException
 *
 * Custom exception thrown when attempting to create a resource that already exists.
 * Examples:
 * - Email already registered
 * - Username already taken
 */
public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
