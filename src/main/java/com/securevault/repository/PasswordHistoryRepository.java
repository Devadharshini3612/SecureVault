package com.securevault.repository;

import com.securevault.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PasswordHistoryRepository
 *
 * Repository interface for managing PasswordHistory entities.
 * Provides methods to query password history for reuse prevention and audit trails.
 */
@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    /**
     * Find all password history entries for a specific credential
     * Ordered by version descending (most recent first)
     *
     * @param credentialId the ID of the credential
     * @return List of password history entries
     */
    List<PasswordHistory> findByCredentialIdOrderByVersionDesc(Long credentialId);

    /**
     * Find the last N password history entries for a credential
     * Used for password reuse prevention (e.g., prevent reusing last 5 passwords)
     *
     * @param credentialId the ID of the credential
     * @param limit maximum number of history entries to return
     * @return List of recent password history entries
     */
    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.credentialId = :credentialId " +
           "ORDER BY ph.version DESC LIMIT :limit")
    List<PasswordHistory> findRecentPasswordHistory(@Param("credentialId") Long credentialId, 
                                                    @Param("limit") int limit);

    /**
     * Get the highest version number for a credential
     * Used to determine the next version number when creating new history
     *
     * @param credentialId the ID of the credential
     * @return the highest version number, or null if no history exists
     */
    @Query("SELECT MAX(ph.version) FROM PasswordHistory ph WHERE ph.credentialId = :credentialId")
    Integer findMaxVersionByCredentialId(@Param("credentialId") Long credentialId);

    /**
     * Count the total number of password changes for a credential
     *
     * @param credentialId the ID of the credential
     * @return count of password changes
     */
    Long countByCredentialId(Long credentialId);

    /**
     * Delete all password history for a credential
     * Used when permanently deleting a credential
     *
     * @param credentialId the ID of the credential
     */
    void deleteByCredentialId(Long credentialId);
}
