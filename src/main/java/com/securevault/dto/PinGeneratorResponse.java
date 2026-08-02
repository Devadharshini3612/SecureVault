package com.securevault.dto;

/**
 * PinGeneratorResponse DTO
 *
 * Response object containing generated PIN.
 */
public class PinGeneratorResponse {

    /**
     * The generated PIN
     */
    private String pin;

    /**
     * Length of the generated PIN
     */
    private int length;

    // ========== Constructors ==========

    public PinGeneratorResponse() {
    }

    public PinGeneratorResponse(String pin, int length) {
        this.pin = pin;
        this.length = length;
    }

    // ========== Getters and Setters ==========

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
