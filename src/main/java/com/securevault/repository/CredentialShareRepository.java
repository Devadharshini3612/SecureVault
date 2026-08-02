package com.securevault.repository;

import com.securevault.entity.CredentialShare;
import com.securevault.enums.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CredentialShareRepository
 *
 * Data access layer for credential sharing operations.
 */
@Repository
public interface CredentialShareRepository extends JpaRepository<CredentialShare, Long> {

    /**
     * Find all active shares for a specific credential
     */
    List<CredentialShare> findByCredentialIdAndActiveTrue(Long credentialId);

    /**
     * Find all credentials shared BY a user (as owner)
     */
    List<CredentialShare> findByOwnerIdAndActiveTrue(Long ownerId);

    /**
     * Find all credentials shared WITH a user (as recipient)
     */
    List<CredentialShare> findBySharedWithUserIdAndActiveTrue(Long sharedWithUserId);

    /**
     * Find specific share between owner and recipient for a credential
     */
    Optional<CredentialShare> findByCredentialIdAndSharedWithUserIdAndActiveTrue(
        Long credentialId, 
        Long sharedWithUserId
    );

    /**
     * Check if a credential is already shared with a specific user
     */
    @Query("SELECT COUNT(cs) > 0 FROM CredentialShare cs " +
           "WHERE cs.credentialId = :credentialId " +
           "AND cs.sharedWithUserId = :sharedWithUserId " +
           "AND cs.active = true")
    boolean existsActiveShareForCredentialAndUser(
        @Param("credentialId") Long credentialId,
        @Param("sharedWithUserId") Long sharedWithUserId
    );

    /**
     * Find active share by share ID and owner ID (for authorization)
     */
    Optional<CredentialShare> findByShareIdAndOwnerIdAndActiveTrue(Long shareId, Long ownerId);

    /**
     * Get permission for a specific user and credential
     */
    @Query("SELECT cs.permission FROM CredentialShare cs " +
           "WHERE cs.credentialId = :credentialId " +
           "AND cs.sharedWithUserId = :userId " +
           "AND cs.active = true")
    Optional<Permission> findPermissionForUserAndCredential(
        @Param("credentialId") Long credentialId,
        @Param("userId") Long userId
    );

    /**
     * Check if user has access to a credential (either owner or shared)
     */
    @Query("SELECT CASE WHEN COUNT(cs) > 0 THEN true ELSE false END FROM CredentialShare cs " +
           "WHERE cs.credentialId = :credentialId " +
           "AND cs.sharedWithUserId = :userId " +
           "AND cs.active = true")
    boolean hasAccessToCredential(
        @Param("credentialId") Long credentialId,
        @Param("userId") Long userId
    );

    /**
     * Revoke all shares for a specific credential (when credential is deleted)
     */
    @Query("UPDATE CredentialShare cs SET cs.active = false, cs.revokedAt = CURRENT_TIMESTAMP " +
           "WHERE cs.credentialId = :credentialId AND cs.active = true")
    void revokeAllSharesForCredential(@Param("credentialId") Long credentialId);

    /**
     * Count active shares for a credential
     */
    long countByCredentialIdAndActiveTrue(Long credentialId);
}
