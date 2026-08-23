package com.securevault.service;

import com.securevault.entity.PasswordHistory;
import com.securevault.exception.PasswordReuseException;
import com.securevault.repository.PasswordHistoryRepository;
import com.securevault.util.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PasswordHistoryService
 *
 * Service layer for managing password history and reuse prevention.
 * Ensures users cannot reuse recent passwords for security.
 *
 * Configuration:
 * - Tracks last 5 passwords by default
 * - Prevents reuse of any of these 5 passwords
 * - All stored passwords remain encrypted
 */
@Service
public class PasswordHistoryService {

    private static final int PASSWORD_HISTORY_LIMIT = 5;

    private final PasswordHistoryRepository passwordHistoryRepository;

    /**
     * Constructor injection - Spring automatically injects dependencies
     */
    public PasswordHistoryService(PasswordHistoryRepository passwordHistoryRepository) {
        this.passwordHistoryRepository = passwordHistoryRepository;
    }

    /**
     * Save a password to history when it's being changed
     * Automatically determines the next version number
     *
     * @param credentialId the ID of the credential
     * @param encryptedPassword the encrypted password to save
     * @return the created PasswordHistory entry
     */
    @Transactional
    public PasswordHistory savePasswordHistory(Long credentialId, String encryptedPassword) {
        // Get the current max version
        Integer maxVersion = passwordHistoryRepository.findMaxVersionByCredentialId(credentialId);
        int nextVersion = (maxVersion == null) ? 1 : maxVersion + 1;

        // Create and save new history entry
        PasswordHistory history = new PasswordHistory(credentialId, encryptedPassword, nextVersion);
        return passwordHistoryRepository.save(history);
    }

    /**
     * Save a password to history with a reason for the change
     *
     * @param credentialId the ID of the credential
     * @param encryptedPassword the encrypted password to save
     * @param reason why the password was changed
     * @return the created PasswordHistory entry
     */
    @Transactional
    public PasswordHistory savePasswordHistory(Long credentialId, String encryptedPassword, String reason) {
        Integer maxVersion = passwordHistoryRepository.findMaxVersionByCredentialId(credentialId);
        int nextVersion = (maxVersion == null) ? 1 : maxVersion + 1;

        PasswordHistory history = new PasswordHistory(credentialId, encryptedPassword, nextVersion, reason);
        return passwordHistoryRepository.save(history);
    }

    /**
     * Check if a password has been used recently (password reuse prevention)
     *
     * @param credentialId the ID of the credential
     * @param newPlaintextPassword the new password to check (plaintext)
     * @return true if password was used recently, false otherwise
     * @throws Exception if decryption fails
     */
    @Transactional(readOnly = true)
    public boolean isPasswordReused(Long credentialId, String newPlaintextPassword) throws Exception {
        // Get last N passwords from history
        List<PasswordHistory> recentHistory = passwordHistoryRepository
                .findRecentPasswordHistory(credentialId, PASSWORD_HISTORY_LIMIT);

        // Check if new password matches any recent password
        for (PasswordHistory history : recentHistory) {
            String historicalPassword = AESUtil.decrypt(history.getEncryptedPassword());
            
            if (newPlaintextPassword.equals(historicalPassword)) {
                return true; // Password was used recently
            }
        }

        return false; // Password is new and can be used
    }

    /**
     * Validate that a password is not being reused
     * Throws exception if password was used recently
     *
     * @param credentialId the ID of the credential
     * @param newPlaintextPassword the new password to validate
     * @throws PasswordReuseException if password was used recently
     * @throws Exception if decryption fails
     */
    @Transactional(readOnly = true)
    public void validatePasswordNotReused(Long credentialId, String newPlaintextPassword) throws Exception {
        if (isPasswordReused(credentialId, newPlaintextPassword)) {
            throw new PasswordReuseException(
                "This password was used recently. Please choose a different password. " +
                "You cannot reuse your last " + PASSWORD_HISTORY_LIMIT + " passwords."
            );
        }
    }

    /**
     * Get complete password history for a credential
     *
     * @param credentialId the ID of the credential
     * @return List of all password history entries, most recent first
     */
    @Transactional(readOnly = true)
    public List<PasswordHistory> getPasswordHistory(Long credentialId) {
        return passwordHistoryRepository.findByCredentialIdOrderByVersionDesc(credentialId);
    }

    /**
     * Get recent password history (last N entries)
     *
     * @param credentialId the ID of the credential
     * @param limit maximum number of entries to return
     * @return List of recent password history entries
     */
    @Transactional(readOnly = true)
    public List<PasswordHistory> getRecentPasswordHistory(Long credentialId, int limit) {
        return passwordHistoryRepository.findRecentPasswordHistory(credentialId, limit);
    }

    /**
     * Count total password changes for a credential
     *
     * @param credentialId the ID of the credential
     * @return count of password changes
     */
    @Transactional(readOnly = true)
    public long countPasswordChanges(Long credentialId) {
        Long count = passwordHistoryRepository.countByCredentialId(credentialId);
        return (count != null) ? count : 0;
    }

    /**
     * Delete all password history for a credential
     * Used when permanently deleting a credential
     *
     * @param credentialId the ID of the credential
     */
    @Transactional
    public void deletePasswordHistory(Long credentialId) {
        passwordHistoryRepository.deleteByCredentialId(credentialId);
    }
}
