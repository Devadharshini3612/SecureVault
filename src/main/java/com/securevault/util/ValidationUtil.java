package com.securevault.util;

import com.securevault.exception.InvalidEmailException;
import com.securevault.exception.WeakPasswordException;

import java.util.regex.Pattern;

/**
 * ValidationUtil
 *
 * Utility class for validating user inputs like email addresses and passwords.
 * 
 * Email Validation:
 * - Must contain exactly one @ symbol
 * - Must have characters before and after @
 * - Must have a domain with at least one dot
 * - Examples: john@gmail.com ✓, john@@gmail.com ✗, john@gmail ✗
 *
 * Password Strength Requirements:
 * - Minimum 8 characters
 * - At least one uppercase letter (A-Z)
 * - At least one lowercase letter (a-z)
 * - At least one digit (0-9)
 * - At least one special character (!@#$%^&*()_+-=[]{}|;:',.<>?/)
 *
 * Usage:
 * <pre>
 * ValidationUtil.validateEmail("john@gmail.com");        // OK
 * ValidationUtil.validateEmail("invalid-email");         // Throws InvalidEmailException
 * ValidationUtil.validatePassword("Test@123");           // OK
 * ValidationUtil.validatePassword("weak");               // Throws WeakPasswordException
 * </pre>
 */
public class ValidationUtil {

    /**
     * Email validation pattern
     * 
     * Pattern breakdown:
     * ^              - Start of string
     * [A-Za-z0-9+_.-] - Username can contain letters, numbers, +, _, ., -
     * +              - One or more characters
     * @              - Must have exactly one @ symbol
     * [A-Za-z0-9.-]  - Domain can contain letters, numbers, ., -
     * +              - One or more characters
     * \.             - Must have a dot
     * [A-Za-z]{2,}   - Top-level domain (com, net, org, etc.) - at least 2 letters
     * $              - End of string
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Password validation patterns
     */
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    /**
     * Minimum password length
     */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Validates an email address
     *
     * @param email the email address to validate
     * @throws InvalidEmailException if email format is invalid
     */
    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidEmailException("Email cannot be empty");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailException(
                "Invalid email format. Email must be in format: username@domain.com"
            );
        }
    }

    /**
     * Validates password strength
     *
     * Password Requirements:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character
     *
     * @param password the password to validate
     * @throws WeakPasswordException if password doesn't meet strength requirements
     */
    public static void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new WeakPasswordException("Password cannot be empty");
        }

        // Check minimum length
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new WeakPasswordException(
                String.format("Password must be at least %d characters long", MIN_PASSWORD_LENGTH)
            );
        }

        // Check for uppercase letter
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            throw new WeakPasswordException(
                "Password must contain at least one uppercase letter (A-Z)"
            );
        }

        // Check for lowercase letter
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            throw new WeakPasswordException(
                "Password must contain at least one lowercase letter (a-z)"
            );
        }

        // Check for digit
        if (!DIGIT_PATTERN.matcher(password).find()) {
            throw new WeakPasswordException(
                "Password must contain at least one digit (0-9)"
            );
        }

        // Check for special character
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            throw new WeakPasswordException(
                "Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:',.<>?/)"
            );
        }
    }

    /**
     * Validates that a string is not null or empty
     *
     * @param value the string to validate
     * @param fieldName the name of the field (for error message)
     * @throws IllegalArgumentException if string is null or empty
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    /**
     * Validates that a Long ID is not null and is positive
     *
     * @param id the ID to validate
     * @param fieldName the name of the field (for error message)
     * @throws IllegalArgumentException if ID is null or not positive
     */
    public static void validateId(Long id, String fieldName) {
        if (id == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        if (id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive number");
        }
    }

    /**
     * Checks if an email is valid without throwing an exception
     *
     * @param email the email to check
     * @return true if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Checks password strength and returns a descriptive message
     *
     * @param password the password to check
     * @return "Strong" if password meets all requirements, 
     *         or a message describing what's missing
     */
    public static String getPasswordStrength(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "Password is empty";
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password is too short (minimum " + MIN_PASSWORD_LENGTH + " characters)";
        }

        boolean hasUpper = UPPERCASE_PATTERN.matcher(password).find();
        boolean hasLower = LOWERCASE_PATTERN.matcher(password).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();
        boolean hasSpecial = SPECIAL_CHAR_PATTERN.matcher(password).find();

        if (!hasUpper) return "Missing uppercase letter";
        if (!hasLower) return "Missing lowercase letter";
        if (!hasDigit) return "Missing digit";
        if (!hasSpecial) return "Missing special character";

        return "Strong";
    }
}
