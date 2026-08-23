package com.securevault.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES Encryption Utility
 *
 * This class provides AES-256-GCM encryption and decryption for securing passwords.
 *
 * AES (Advanced Encryption Standard):
 * - Industry-standard symmetric encryption algorithm
 * - 256-bit key size for maximum security
 * - GCM (Galois/Counter Mode) provides both encryption and authentication
 *
 * How it works:
 * 1. Encrypt: plaintext password → AES-256-GCM → encrypted bytes → Base64 string
 * 2. Decrypt: Base64 string → encrypted bytes → AES-256-GCM → plaintext password
 *
 * PRODUCTION READY:
 * - The encryption key is now injected from environment variables
 * - Configured via application.properties and environment variables
 * - No hardcoded secrets in the code
 *
 * @author SecureVault Team
 */
@Component
public class AESUtil {

    /**
     * AES algorithm with GCM mode and no padding
     */
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    /**
     * GCM tag length in bits (128 bits = 16 bytes)
     * Used for authentication
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * Initialization Vector (IV) length in bytes
     * GCM mode requires a 12-byte IV
     */
    private static final int IV_LENGTH = 12;

    /**
     * Secret key for AES-256 encryption
     * Injected from environment variable or application.properties
     * Must be a Base64-encoded 256-bit (32-byte) key
     */
    @Value("${aes.encryption.key}")
    private String secretKeyBase64;

    /**
     * Static instance for backward compatibility with static method calls
     */
    private static AESUtil instance;

    /**
     * Constructor that sets the static instance for backward compatibility
     */
    public AESUtil() {
        instance = this;
    }

    /**
     * Get the secret key as a SecretKey object
     *
     * @return SecretKey for AES encryption
     */
    private SecretKey getSecretKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKeyBase64);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    /**
     * Encrypts a plaintext password using AES-256-GCM
     *
     * Process:
     * 1. Generate a random 12-byte IV (Initialization Vector)
     * 2. Configure AES cipher in GCM mode with the IV
     * 3. Encrypt the plaintext password
     * 4. Combine IV + encrypted data
     * 5. Encode the result as Base64 for storage
     *
     * The IV is prepended to the encrypted data so it can be extracted during decryption.
     * IV doesn't need to be secret, but it MUST be unique for each encryption.
     *
     * @param plaintext the plaintext password to encrypt
     * @return Base64-encoded string containing IV + encrypted data
     * @throws Exception if encryption fails
     */
    public String encryptInstance(String plaintext) throws Exception {
        
        // Step 1: Generate a random IV
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // Step 2: Initialize cipher for encryption
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), gcmParameterSpec);

        // Step 3: Encrypt the plaintext
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes("UTF-8"));

        // Step 4: Combine IV + encrypted data
        // Format: [IV (12 bytes)][Encrypted Data][GCM Tag (16 bytes)]
        byte[] combined = new byte[IV_LENGTH + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
        System.arraycopy(encryptedBytes, 0, combined, IV_LENGTH, encryptedBytes.length);

        // Step 5: Encode as Base64 for storage in database
        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Static method for backward compatibility
     * Delegates to instance method
     */
    public static String encrypt(String plaintext) throws Exception {
        if (instance == null) {
            throw new IllegalStateException("AESUtil not initialized by Spring. Use @Autowired AESUtil instead.");
        }
        return instance.encryptInstance(plaintext);
    }

    /**
     * Decrypts an AES-256-GCM encrypted password
     *
     * Process:
     * 1. Decode the Base64 string to get IV + encrypted data
     * 2. Extract the IV from the first 12 bytes
     * 3. Extract the encrypted data from remaining bytes
     * 4. Configure AES cipher in GCM mode with the IV
     * 5. Decrypt the data
     * 6. Return the plaintext password
     *
     * @param encryptedText Base64-encoded string containing IV + encrypted data
     * @return the decrypted plaintext password
     * @throws Exception if decryption fails (wrong key, corrupted data, etc.)
     */
    public String decryptInstance(String encryptedText) throws Exception {
        
        // Step 1: Decode Base64 to get raw bytes
        byte[] combined = Base64.getDecoder().decode(encryptedText);

        // Step 2: Extract IV (first 12 bytes)
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

        // Step 3: Extract encrypted data (remaining bytes)
        byte[] encryptedBytes = new byte[combined.length - IV_LENGTH];
        System.arraycopy(combined, IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

        // Step 4: Initialize cipher for decryption
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), gcmParameterSpec);

        // Step 5: Decrypt the data
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // Step 6: Convert bytes to string and return
        return new String(decryptedBytes, "UTF-8");
    }

    /**
     * Static method for backward compatibility
     * Delegates to instance method
     */
    public static String decrypt(String encryptedText) throws Exception {
        if (instance == null) {
            throw new IllegalStateException("AESUtil not initialized by Spring. Use @Autowired AESUtil instead.");
        }
        return instance.decryptInstance(encryptedText);
    }

    /**
     * Generates a new random 256-bit AES key
     *
     * Use this method to generate a secure key for production use.
     * Store the generated key securely in environment variables.
     *
     * Example usage:
     * <pre>
     * String newKey = AESUtil.generateKey();
     * System.out.println("New AES Key (store this securely): " + newKey);
     * </pre>
     *
     * @return Base64-encoded 256-bit AES key
     * @throws Exception if key generation fails
     */
    public static String generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256, new SecureRandom());
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * Test method to verify encryption and decryption
     *
     * This demonstrates that:
     * 1. Same plaintext produces DIFFERENT encrypted values (due to random IV)
     * 2. Decryption correctly retrieves the original plaintext
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            String plaintext = "MySecretPassword123!";
            
            System.out.println("=== AES-256-GCM Encryption Test ===");
            System.out.println("Plaintext: " + plaintext);
            System.out.println();

            // Encrypt twice to show different results
            String encrypted1 = encrypt(plaintext);
            String encrypted2 = encrypt(plaintext);

            System.out.println("Encrypted 1: " + encrypted1);
            System.out.println("Encrypted 2: " + encrypted2);
            System.out.println("Same plaintext, different encrypted values: " + !encrypted1.equals(encrypted2));
            System.out.println();

            // Decrypt to verify
            String decrypted1 = decrypt(encrypted1);
            String decrypted2 = decrypt(encrypted2);

            System.out.println("Decrypted 1: " + decrypted1);
            System.out.println("Decrypted 2: " + decrypted2);
            System.out.println("Decryption successful: " + (plaintext.equals(decrypted1) && plaintext.equals(decrypted2)));

        } catch (Exception e) {
            System.err.println("Encryption/Decryption failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
