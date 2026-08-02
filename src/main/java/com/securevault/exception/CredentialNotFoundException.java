package com.securevault.exception;

/**
 * Exception thrown when a credential is not found in the database.
 * 
 * Used in scenarios like:
 * - Reading non-existent credential
 * - Updating credential that doesn't exist
 * - Deleting credential that doesn't exist
 * - Accessing credential that belongs to another user
 */
public class CredentialNotFoundException extends RuntimeException {

    public CredentialNotFoundException(String message) {
        super(message);
    }

    public CredentialNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create exception for credential not found by ID
     */
    public static CredentialNotFoundException byId(Long credentialId) {
        return new CredentialNotFoundException("Credential not found with ID: " + credentialId);
    }

    /**
     * Create exception for credential not found by ID and user
     */
    public static CredentialNotFoundException byIdAndUser(Long credentialId, Long userId) {
        return new CredentialNotFoundException(
            "Credential with ID " + credentialId + " not found for user " + userId
        );
    }
}