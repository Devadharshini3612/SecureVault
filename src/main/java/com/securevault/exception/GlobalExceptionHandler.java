package com.securevault.exception;

import com.securevault.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Global Exception Handler using @ControllerAdvice
 * 
 * Catches all exceptions thrown by controllers and converts them into 
 * standardized ApiResponse format with appropriate HTTP status codes.
 * 
 * Benefits:
 * - Consistent error response format across all endpoints
 * - Centralized exception handling logic
 * - Clean controller code without try-catch blocks
 * - Proper HTTP status codes for different error types
 * - Detailed validation error messages
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle User Not Found exceptions
     * Returns 404 Not Found
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFound(
            UserNotFoundException ex, WebRequest request) {
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle Credential Not Found exceptions
     * Returns 404 Not Found
     */
    @ExceptionHandler(CredentialNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleCredentialNotFound(
            CredentialNotFoundException ex, WebRequest request) {
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle Password Reuse exceptions
     * Returns 409 Conflict
     */
    @ExceptionHandler(PasswordReuseException.class)
    public ResponseEntity<ApiResponse<Object>> handlePasswordReuse(
            PasswordReuseException ex, WebRequest request) {
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handle Duplicate Email exceptions
     * Returns 409 Conflict
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateEmail(
            DuplicateEmailException ex, WebRequest request) {
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handle Invalid Credentials exceptions
     * Returns 401 Unauthorized
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCredentials(
            InvalidCredentialsException ex, WebRequest request) {
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle Unauthorized Access exceptions
     * Returns 403 Forbidden
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorizedAccess(
            UnauthorizedAccessException ex, WebRequest request) {
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle Invalid Share exceptions
     * Returns 400 Bad Request
     */
    @ExceptionHandler(InvalidShareException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidShare(
            InvalidShareException ex, WebRequest request) {
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Custom Validation exceptions
     * Returns 400 Bad Request
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            ValidationException ex, WebRequest request) {
        
        ApiResponse<Object> response;
        
        if (ex.getFieldErrors() != null && !ex.getFieldErrors().isEmpty()) {
            response = ApiResponse.validationError(ex.getMessage(), ex.getFieldErrors());
        } else {
            response = ApiResponse.error(ex.getMessage());
        }
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Bean Validation exceptions (@Valid, @NotBlank, @Email, etc.)
     * Returns 400 Bad Request with detailed field validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        BindingResult bindingResult = ex.getBindingResult();
        Map<String, List<String>> fieldErrors = new HashMap<>();
        
        // Extract field validation errors
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            String fieldName = fieldError.getField();
            String errorMessage = fieldError.getDefaultMessage();
            
            fieldErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
        }
        
        ApiResponse<Object> response = ApiResponse.validationError(
            "Validation failed for one or more fields", 
            fieldErrors
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Illegal Argument exceptions
     * Returns 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Runtime exceptions (catch-all for unexpected errors)
     * Returns 500 Internal Server Error
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        // Log the full stack trace for debugging (in real apps, use proper logging)
        ex.printStackTrace();
        
        ApiResponse<Object> response = ApiResponse.error(
            "An unexpected error occurred. Please try again later."
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle all other exceptions (ultimate catch-all)
     * Returns 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, WebRequest request) {
        
        // Log the full stack trace for debugging (in real apps, use proper logging)
        ex.printStackTrace();
        
        ApiResponse<Object> response = ApiResponse.error(
            "A system error occurred. Please contact support if the problem persists."
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}