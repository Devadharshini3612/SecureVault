package com.securevault.exception;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when request validation fails.
 * 
 * Used in scenarios like:
 * - Bean validation failures (@NotBlank, @Email, etc.)
 * - Custom business logic validation
 * - Input format validation
 */
public class ValidationException extends RuntimeException {

    private Map<String, List<String>> fieldErrors;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Map<String, List<String>> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create validation exception with field-specific errors
     */
    public static ValidationException withFieldErrors(String message, Map<String, List<String>> fieldErrors) {
        return new ValidationException(message, fieldErrors);
    }

    /**
     * Create simple validation exception
     */
    public static ValidationException simple(String message) {
        return new ValidationException(message);
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, List<String>> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}