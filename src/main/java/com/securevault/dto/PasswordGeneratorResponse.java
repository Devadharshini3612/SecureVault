package com.securevault.dto;

/**
 * PasswordGeneratorResponse DTO
 *
 * Response object containing generated password and its metadata.
 */
public class PasswordGeneratorResponse {

    /**
     * The generated password
     */
    private String password;

    /**
     * Length of the generated password
     */
    private int length;

    /**
     * Strength score (0-100)
     */
    private int strengthScore;

    /**
     * Strength rating (Very Weak, Weak, Moderate, Strong, Very Strong)
     */
    private String strengthRating;

    // ========== Constructors ==========

    public PasswordGeneratorResponse() {
    }

    public PasswordGeneratorResponse(String password, int length, int strengthScore, String strengthRating) {
        this.password = password;
        this.length = length;
        this.strengthScore = strengthScore;
        this.strengthRating = strengthRating;
    }

    // ========== Getters and Setters ==========

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getStrengthScore() {
        return strengthScore;
    }

    public void setStrengthScore(int strengthScore) {
        this.strengthScore = strengthScore;
    }

    public String getStrengthRating() {
        return strengthRating;
    }

    public void setStrengthRating(String strengthRating) {
        this.strengthRating = strengthRating;
    }
}
