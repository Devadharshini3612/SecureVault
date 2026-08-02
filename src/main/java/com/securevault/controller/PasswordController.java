package com.securevault.controller;

import com.securevault.dto.ApiResponse;
import com.securevault.dto.PasswordGeneratorRequest;
import com.securevault.dto.PasswordGeneratorResponse;
import com.securevault.dto.PasswordStrengthRequest;
import com.securevault.dto.PasswordStrengthResponse;
import com.securevault.dto.PinGeneratorRequest;
import com.securevault.dto.PinGeneratorResponse;
import com.securevault.exception.ValidationException;
import com.securevault.mapper.DtoEntityMapper;
import com.securevault.util.PasswordGeneratorUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * PasswordController - Production-Ready Password Utility API
 * 
 * Fully refactored controller using:
 * - DTO pattern with validation annotations (@Valid)
 * - Standardized ApiResponse wrapper for all responses
 * - DtoEntityMapper for clean response construction
 * - Custom exceptions handled by GlobalExceptionHandler
 * - Consistent HTTP status codes and error messages
 * 
 * REST API for password generation and strength analysis utilities.
 * Provides endpoints to generate strong, random passwords and analyze password strength.
 */
@RestController
@RequestMapping("/api/password")
@Validated
public class PasswordController {

    @Autowired
    private DtoEntityMapper mapper;

    /**
     * Generate a password with custom configuration
     * 
     * POST /api/password/generate
     * Body: PasswordGeneratorRequest
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<PasswordGeneratorResponse>> generatePassword(
            @Valid @RequestBody PasswordGeneratorRequest request) {
        
        String password = PasswordGeneratorUtil.generatePassword(
            request.getLength(),
            request.isIncludeUppercase(),
            request.isIncludeLowercase(),
            request.isIncludeDigits(),
            request.isIncludeSpecial()
        );
        
        int strength = PasswordGeneratorUtil.calculatePasswordStrength(password);
        
        PasswordGeneratorResponse response = new PasswordGeneratorResponse(
            password,
            password.length(),
            strength,
            getRating(strength)
        );
        
        return ResponseEntity.ok(ApiResponse.success("Password generated successfully", response));
    }

    /**
     * Analyze password strength
     * 
     * POST /api/password/strength
     * Body: PasswordStrengthRequest
     */
    @PostMapping("/strength")
    public ResponseEntity<ApiResponse<PasswordStrengthResponse>> analyzePasswordStrength(
            @Valid @RequestBody PasswordStrengthRequest request) {
        
        String password = request.getPassword();
        int score = PasswordGeneratorUtil.calculatePasswordStrength(password);
        String strength = getRating(score);
        
        // Generate feedback based on password characteristics
        java.util.List<String> feedback = generateFeedback(password, score);
        
        PasswordStrengthResponse response = new PasswordStrengthResponse(score, strength, feedback);
        
        return ResponseEntity.ok(ApiResponse.success("Password strength analyzed successfully", response));
    }

    /**
     * Generate a PIN
     * 
     * POST /api/password/generate/pin
     * Body: PinGeneratorRequest
     */
    @PostMapping("/generate/pin")
    public ResponseEntity<ApiResponse<PinGeneratorResponse>> generatePIN(
            @Valid @RequestBody PinGeneratorRequest request) {
        
        String pin = PasswordGeneratorUtil.generatePIN(request.getLength());
        
        PinGeneratorResponse response = new PinGeneratorResponse(pin, pin.length());
        
        return ResponseEntity.ok(ApiResponse.success("PIN generated successfully", response));
    }

    private String getRating(int strength) {
        if (strength >= 80) return "Very Strong";
        if (strength >= 60) return "Strong";
        if (strength >= 40) return "Moderate";
        if (strength >= 20) return "Weak";
        return "Very Weak";
    }

    private java.util.List<String> generateFeedback(String password, int score) {
        java.util.List<String> feedback = new java.util.ArrayList<>();
        
        if (password.length() < 12) {
            feedback.add("Increase length to at least 12 characters");
        } else if (password.length() < 16) {
            feedback.add("Consider increasing length to 16+ characters for better security");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            feedback.add("Add uppercase letters (A-Z)");
        }
        
        if (!password.matches(".*[a-z].*")) {
            feedback.add("Add lowercase letters (a-z)");
        }
        
        if (!password.matches(".*\\d.*")) {
            feedback.add("Add numbers (0-9)");
        }
        
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            feedback.add("Add special characters (!@#$%^&*)");
        }
        
        // Check for consecutive repeated characters
        if (password.matches(".*(.)\\1{2,}.*")) {
            feedback.add("Avoid consecutive repeated characters");
        }
        
        // Check for sequential patterns
        if (password.matches(".*(012|123|234|345|456|567|678|789|abc|bcd|cde|def|efg|fgh|ghi|hij|ijk).*")) {
            feedback.add("Avoid sequential patterns (123, abc, etc.)");
        }
        
        if (score >= 80 && feedback.isEmpty()) {
            feedback.add("Excellent password strength!");
        }
        
        return feedback;
    }
}
