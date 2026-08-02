package com.securevault.dto;

import java.time.LocalDateTime;

/**
 * ErrorResponse DTO
 *
 * Standardized error response structure for all API errors.
 * This ensures consistent error messages across all endpoints.
 *
 * Example JSON Response:
 * {
 *   "timestamp": "2026-07-11T10:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Email format is invalid",
 *   "path": "/api/auth/register"
 * }
 */
public class ErrorResponse {

    /**
     * When the error occurred
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code (400, 404, 409, 500, etc.)
     */
    private int status;

    /**
     * HTTP status message ("Bad Request", "Not Found", etc.)
     */
    private String error;

    /**
     * Detailed error message explaining what went wrong
     */
    private String message;

    /**
     * The API endpoint path where the error occurred
     */
    private String path;

    // ========== Constructors ==========

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // ========== Getters and Setters ==========

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
