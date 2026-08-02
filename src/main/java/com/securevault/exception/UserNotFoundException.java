package com.securevault.exception;

/**
 * Exception thrown when a user is not found in the database.
 * 
 * Used in scenarios like:
 * - Login with non-existent email
 * - JWT token references invalid user ID
 * - User profile operations on deleted users
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create exception for user not found by email
     */
    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException("User not found with email: " + email);
    }

    /**
     * Create exception for user not found by ID
     */
    public static UserNotFoundException byId(Long userId) {
        return new UserNotFoundException("User not found with ID: " + userId);
    }
}