package com.securevault.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PasswordGeneratorUtil
 *
 * Utility class for generating strong, random passwords.
 * 
 * Features:
 * - Generates cryptographically secure random passwords
 * - Configurable length (minimum 8 characters)
 * - Option to include/exclude character types:
 *   - Uppercase letters (A-Z)
 *   - Lowercase letters (a-z)
 *   - Digits (0-9)
 *   - Special characters (!@#$%^&*()_+-=[]{}|;:',.<>?/)
 * - Guarantees at least one character from each selected category
 * - Uses SecureRandom for cryptographic strength
 *
 * Usage Examples:
 * <pre>
 * // Generate 12-character password with all character types
 * String password = PasswordGeneratorUtil.generatePassword(12, true, true, true, true);
 * // Result: "aB3!xY9@mN2#"
 *
 * // Generate 16-character password without special characters
 * String password = PasswordGeneratorUtil.generatePassword(16, true, true, true, false);
 * // Result: "aB3xY9mN2pQ5tR8z"
 *
 * // Generate with default settings (16 chars, all types)
 * String password = PasswordGeneratorUtil.generateStrongPassword();
 * // Result: "aB3!xY9@mN2#pQ5$"
 * </pre>
 */
public class PasswordGeneratorUtil {

    /**
     * Character sets for password generation
     */
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGIT_CHARS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:',.<>?/";

    /**
     * Cryptographically secure random number generator
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Default password length
     */
    private static final int DEFAULT_LENGTH = 16;

    /**
     * Minimum password length
     */
    private static final int MIN_LENGTH = 8;

    /**
     * Generates a strong password with default settings
     * 
     * Default settings:
     * - Length: 16 characters
     * - Includes uppercase, lowercase, digits, and special characters
     *
     * @return a randomly generated strong password
     */
    public static String generateStrongPassword() {
        return generatePassword(DEFAULT_LENGTH, true, true, true, true);
    }

    /**
     * Generates a password with specified length and default character types
     * 
     * @param length the length of the password (minimum 8)
     * @return a randomly generated password
     * @throws IllegalArgumentException if length is less than minimum
     */
    public static String generatePassword(int length) {
        return generatePassword(length, true, true, true, true);
    }

    /**
     * Generates a password with custom configuration
     *
     * @param length the length of the password (minimum 8)
     * @param includeUppercase include uppercase letters (A-Z)
     * @param includeLowercase include lowercase letters (a-z)
     * @param includeDigits include digits (0-9)
     * @param includeSpecial include special characters (!@#$%^&*()_+-=[]{}|;:',.<>?/)
     * @return a randomly generated password
     * @throws IllegalArgumentException if length is less than minimum or no character types selected
     */
    public static String generatePassword(
            int length,
            boolean includeUppercase,
            boolean includeLowercase,
            boolean includeDigits,
            boolean includeSpecial) {

        // Validate length
        if (length < MIN_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Password length must be at least %d characters", MIN_LENGTH)
            );
        }

        // At least one character type must be selected
        if (!includeUppercase && !includeLowercase && !includeDigits && !includeSpecial) {
            throw new IllegalArgumentException(
                "At least one character type must be selected"
            );
        }

        // Build the character pool based on selected options
        StringBuilder charPool = new StringBuilder();
        List<String> requiredChars = new ArrayList<>();

        if (includeUppercase) {
            charPool.append(UPPERCASE_CHARS);
            requiredChars.add(getRandomChar(UPPERCASE_CHARS));
        }

        if (includeLowercase) {
            charPool.append(LOWERCASE_CHARS);
            requiredChars.add(getRandomChar(LOWERCASE_CHARS));
        }

        if (includeDigits) {
            charPool.append(DIGIT_CHARS);
            requiredChars.add(getRandomChar(DIGIT_CHARS));
        }

        if (includeSpecial) {
            charPool.append(SPECIAL_CHARS);
            requiredChars.add(getRandomChar(SPECIAL_CHARS));
        }

        // Calculate remaining characters needed
        int remainingLength = length - requiredChars.size();

        // Generate the remaining characters randomly from the pool
        List<String> passwordChars = new ArrayList<>(requiredChars);
        for (int i = 0; i < remainingLength; i++) {
            passwordChars.add(getRandomChar(charPool.toString()));
        }

        // Shuffle the characters to avoid predictable patterns
        // (e.g., uppercase always first)
        Collections.shuffle(passwordChars, RANDOM);

        // Build the final password string
        StringBuilder password = new StringBuilder();
        for (String ch : passwordChars) {
            password.append(ch);
        }

        return password.toString();
    }

    /**
     * Generates multiple passwords at once
     *
     * @param count the number of passwords to generate
     * @param length the length of each password
     * @return a list of randomly generated passwords
     */
    public static List<String> generateMultiplePasswords(int count, int length) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }

        List<String> passwords = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            passwords.add(generatePassword(length));
        }
        return passwords;
    }

    /**
     * Generates a PIN (numeric only password)
     *
     * @param length the length of the PIN (minimum 4 digits)
     * @return a randomly generated numeric PIN
     */
    public static String generatePIN(int length) {
        if (length < 4) {
            throw new IllegalArgumentException("PIN length must be at least 4 digits");
        }

        // Build PIN manually to avoid MIN_LENGTH validation
        StringBuilder pin = new StringBuilder();
        for (int i = 0; i < length; i++) {
            pin.append(RANDOM.nextInt(10)); // 0-9
        }
        return pin.toString();
    }

    /**
     * Generates a passphrase using random words and numbers
     * Format: Word1-Word2-Number-Word3
     *
     * @return a randomly generated passphrase
     */
    public static String generatePassphrase() {
        String[] words = {
            "Alpha", "Beta", "Gamma", "Delta", "Echo", "Foxtrot", "Golf", "Hotel",
            "India", "Juliet", "Kilo", "Lima", "Mike", "November", "Oscar", "Papa",
            "Quebec", "Romeo", "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "Xray",
            "Yankee", "Zulu", "Cloud", "Storm", "Thunder", "Lightning", "Rain", "Snow"
        };

        String word1 = words[RANDOM.nextInt(words.length)];
        String word2 = words[RANDOM.nextInt(words.length)];
        String word3 = words[RANDOM.nextInt(words.length)];
        int number = RANDOM.nextInt(9000) + 1000; // 4-digit number

        return String.format("%s-%s-%d-%s", word1, word2, number, word3);
    }

    /**
     * Helper method to get a random character from a string
     *
     * @param chars the string to select from
     * @return a random character as a String
     */
    private static String getRandomChar(String chars) {
        int index = RANDOM.nextInt(chars.length());
        return String.valueOf(chars.charAt(index));
    }

    /**
     * Estimates password strength based on length and character variety
     *
     * @param password the password to evaluate
     * @return a strength score from 0 (weak) to 100 (very strong)
     */
    public static int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Length score (up to 40 points)
        score += Math.min(password.length() * 2, 40);

        // Character variety score (up to 60 points)
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        if (hasUpper) score += 15;
        if (hasLower) score += 15;
        if (hasDigit) score += 15;
        if (hasSpecial) score += 15;

        return Math.min(score, 100);
    }

    /**
     * Test method to demonstrate password generation
     */
    public static void main(String[] args) {
        System.out.println("=== Password Generator Demo ===\n");

        System.out.println("1. Strong password (default 16 chars):");
        System.out.println("   " + generateStrongPassword());
        System.out.println();

        System.out.println("2. Custom length (12 chars):");
        System.out.println("   " + generatePassword(12));
        System.out.println();

        System.out.println("3. No special characters (16 chars):");
        System.out.println("   " + generatePassword(16, true, true, true, false));
        System.out.println();

        System.out.println("4. Only letters (20 chars):");
        System.out.println("   " + generatePassword(20, true, true, false, false));
        System.out.println();

        System.out.println("5. Generate 5 passwords:");
        List<String> passwords = generateMultiplePasswords(5, 12);
        for (int i = 0; i < passwords.size(); i++) {
            System.out.println("   " + (i + 1) + ". " + passwords.get(i));
        }
        System.out.println();

        System.out.println("6. Generate PIN (6 digits):");
        System.out.println("   " + generatePIN(6));
        System.out.println();

        System.out.println("7. Generate Passphrase:");
        System.out.println("   " + generatePassphrase());
        System.out.println();

        System.out.println("8. Password strength scores:");
        String weakPass = "pass123";
        String mediumPass = "Pass123!";
        String strongPass = generateStrongPassword();
        System.out.println("   \"" + weakPass + "\" -> " + calculatePasswordStrength(weakPass) + "/100");
        System.out.println("   \"" + mediumPass + "\" -> " + calculatePasswordStrength(mediumPass) + "/100");
        System.out.println("   \"" + strongPass + "\" -> " + calculatePasswordStrength(strongPass) + "/100");
    }
}
