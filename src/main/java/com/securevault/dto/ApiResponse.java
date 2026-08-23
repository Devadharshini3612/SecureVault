package com.securevault.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Standardized API Response wrapper for consistent response format across all endpoints.
 * 
 * This ensures all API responses follow the same structure:
 * {
 *   "success": true,
 *   "message": "Operation completed successfully",
 *   "data": { ... },
 *   "timestamp": "2026-07-16T12:00:00",
 *   "errors": null
 * }
 * 
 * Benefits:
 * - Consistent client-side parsing
 * - Clear success/failure indication
 * - Standardized error handling
 * - Easy API versioning
 * - Better debugging with timestamps
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates if the operation was successful
     */
    private boolean success;

    /**
     * Human-readable message describing the result
     */
    private String message;

    /**
     * The actual data payload (null for errors)
     */
    private T data;

    /**
     * Timestamp when the response was created
     */
    private LocalDateTime timestamp;

    /**
     * Error details (null for successful responses)
     */
    private Object errors;

    // ========== Constructors ==========

    /**
     * Default constructor
     */
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor for successful responses with data
     */
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor for responses without data (success messages or errors)
     */
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // ========== Static Factory Methods ==========

    /**
     * Create successful response with data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Create successful response without data
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message);
    }

    /**
     * Create error response
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message);
    }

    /**
     * Create error response with error details
     */
    public static <T> ApiResponse<T> error(String message, Object errors) {
        ApiResponse<T> response = new ApiResponse<>(false, message);
        response.setErrors(errors);
        return response;
    }

    /**
     * Create validation error response
     */
    public static <T> ApiResponse<T> validationError(String message, Object validationErrors) {
        ApiResponse<T> response = new ApiResponse<>(false, message);
        response.setErrors(validationErrors);
        return response;
    }

    // ========== Getters and Setters ==========

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Object getErrors() {
        return errors;
    }

    public void setErrors(Object errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                ", errors=" + errors +
                '}';
    }
}