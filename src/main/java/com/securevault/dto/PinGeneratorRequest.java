package com.securevault.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * PinGeneratorRequest DTO
 *
 * Request object for generating numeric PINs.
 */
public class PinGeneratorRequest {

    /**
     * Desired PIN length
     * Must be between 4 and 12 digits
     */
    @NotNull(message = "PIN length is required")
    @Min(value = 4, message = "PIN length must be at least 4 digits")
    @Max(value = 12, message = "PIN length cannot exceed 12 digits")
    private Integer length = 6;

    // ========== Constructors ==========

    public PinGeneratorRequest() {
    }

    public PinGeneratorRequest(Integer length) {
        this.length = length;
    }

    // ========== Getters and Setters ==========

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }
}
