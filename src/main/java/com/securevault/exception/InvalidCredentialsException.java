package com.securevault.exception;

/**
 * Exception thrown when login credentials are invalid.
 * 
 * Used in scenarios like:
 * - Login with wrong password
 * - JWT token validation failure
 * - Authentication failures
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create exception for invalid password during login
     */
    public static InvalidCredentialsException invalidPassword() {
        return new InvalidCredentialsException("Invalid email or password");
    }

    /**
     * Create exception for invalid JWT token
     */
    public static InvalidCredentialsException invalidToken(String reason) {
        return new InvalidCredentialsException("Invalid authentication token: " + reason);
    }

    /**
     * Create exception for expired JWT token
     */
    public static InvalidCredentialsException expiredToken() {
        return new InvalidCredentialsException("Authentication token has expired");
    }
}