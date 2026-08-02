package com.securevault.repository;

import com.securevault.entity.Credential;
import com.securevault.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

/**
 * CredentialRepository
 *
 * @Repository - marks this as a Spring Data JPA repository.
 * Spring automatically provides implementations for standard CRUD operations.
 *
 * This repository handles database operations for the Credential entity.
 * It extends JpaRepository which provides methods like:
 * - save()
 * - findById()
 * - findAll()
 * - deleteById()
 * - existsById()
 *
 * We add custom query methods for credential-specific operations.
 */
@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long>, JpaSpecificationExecutor<Credential> {

    /**
     * Find all credentials belonging to a specific user
     *
     * This allows users to list all their stored credentials.
     * Spring Data JPA automatically implements this based on the method name.
     *
     * Query generated:
     * SELECT * FROM credentials WHERE user_id = ?
     *
     * @param userId the ID of the user
     * @return List of credentials belonging to the user (empty list if none found)
     */
    List<Credential> findByUserId(Long userId);

    /**
     * Find a specific credential by ID and user ID
     *
     * This ensures that users can only access their own credentials.
     * Security check: even if someone knows a credential ID, they can't
     * access it unless they own it.
     *
     * Query generated:
     * SELECT * FROM credentials WHERE credential_id = ? AND user_id = ?
     *
     * @param credentialId the ID of the credential
     * @param userId the ID of the user
     * @return Optional containing the credential if found, empty otherwise
     */
    Optional<Credential> findByCredentialIdAndUserId(Long credentialId, Long userId);

    /**
     * Delete a credential by ID and user ID
     *
     * This ensures users can only delete their own credentials.
     * Security check: prevents unauthorized deletion.
     *
     * Query generated:
     * DELETE FROM credentials WHERE credential_id = ? AND user_id = ?
     *
     * @param credentialId the ID of the credential to delete
     * @param userId the ID of the user
     * @return the number of records deleted (0 or 1)
     */
    int deleteByCredentialIdAndUserId(Long credentialId, Long userId);

    /**
     * Find credentials by category for a specific user
     * 
     * Query generated:
     * SELECT * FROM credentials WHERE user_id = ? AND category = ?
     * 
     * @param userId the ID of the user
     * @param category the category to filter by
     * @return List of credentials in the specified category
     */
    List<Credential> findByUserIdAndCategory(Long userId, Category category);

    /**
     * Search credentials by service name containing the search term (case-insensitive)
     * 
     * @param userId the ID of the user
     * @param searchTerm the term to search for in service name
     * @return List of matching credentials
     */
    @Query("SELECT c FROM Credential c WHERE c.userId = :userId AND LOWER(c.serviceName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Credential> searchByServiceName(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);

    /**
     * Search credentials by username containing the search term (case-insensitive)
     * 
     * @param userId the ID of the user
     * @param searchTerm the term to search for in username
     * @return List of matching credentials
     */
    @Query("SELECT c FROM Credential c WHERE c.userId = :userId AND LOWER(c.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Credential> searchByUsername(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);

    /**
     * Universal search across service name and username (case-insensitive)
     * 
     * @param userId the ID of the user
     * @param searchTerm the term to search for
     * @return List of matching credentials
     */
    @Query("SELECT c FROM Credential c WHERE c.userId = :userId AND " +
           "(LOWER(c.serviceName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Credential> searchCredentials(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);
}
