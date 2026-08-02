package com.securevault.dto;

import java.util.List;

/**
 * PasswordStrengthResponse DTO
 *
 * Response object containing password strength analysis results.
 */
public class PasswordStrengthResponse {

    /**
     * Numerical strength score (0-100)
     */
    private int score;

    /**
     * Strength rating (Very Weak, Weak, Moderate, Strong, Very Strong)
     */
    private String strength;

    /**
     * List of feedback messages to improve password strength
     */
    private List<String> feedback;

    // ========== Constructors ==========

    public PasswordStrengthResponse() {
    }

    public PasswordStrengthResponse(int score, String strength, List<String> feedback) {
        this.score = score;
        this.strength = strength;
        this.feedback = feedback;
    }

    // ========== Getters and Setters ==========

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public List<String> getFeedback() {
        return feedback;
    }

    public void setFeedback(List<String> feedback) {
        this.feedback = feedback;
    }
}
