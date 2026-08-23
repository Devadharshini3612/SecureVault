package com.securevault.exception;

/**
 * PasswordReuseException
 *
 * Thrown when a user attempts to reuse a recent password.
 * This prevents weak security practices and enforces password rotation.
 *
 * HTTP Status: 409 Conflict
 */
public class PasswordReuseException extends RuntimeException {

    public PasswordReuseException(String message) {
        super(message);
    }

    public PasswordReuseException(String message, Throwable cause) {
        super(message, cause);
    }
}
