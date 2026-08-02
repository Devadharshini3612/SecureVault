package com.securevault.exception;

/**
 * InvalidEmailException
 *
 * Custom exception thrown when an email address is invalid.
 * Examples:
 * - Missing @ symbol: "johngmail.com"
 * - Missing domain: "john@"
 * - Invalid format: "john@@gmail.com"
 */
public class InvalidEmailException extends RuntimeException {
    
    public InvalidEmailException(String message) {
        super(message);
    }
    
    public InvalidEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
