package com.securevault.exception;

/**
 * Exception thrown when a user tries to access resources they don't own.
 * 
 * Used in scenarios like:
 * - Accessing another user's credentials
 * - Unauthorized credential operations
 * - Missing or invalid JWT token
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create exception for accessing another user's resource
     */
    public static UnauthorizedAccessException accessDenied(String resourceType, Long resourceId) {
        return new UnauthorizedAccessException(
            "Access denied to " + resourceType + " with ID: " + resourceId
        );
    }

    /**
     * Create exception for missing authentication
     */
    public static UnauthorizedAccessException missingAuthentication() {
        return new UnauthorizedAccessException("Authentication required to access this resource");
    }

    /**
     * Create exception for insufficient permissions
     */
    public static UnauthorizedAccessException insufficientPermissions(String operation) {
        return new UnauthorizedAccessException("Insufficient permissions for operation: " + operation);
    }
}