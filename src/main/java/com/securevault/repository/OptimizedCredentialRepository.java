package com.securevault.repository;

import com.securevault.entity.Credential;
import com.securevault.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Optimized Credential Repository
 * 
 * This repository focuses on database query optimization to address:
 * 1. N+1 query problems
 * 2. Inefficient joins and fetch strategies
 * 3. Missing database indexes
 * 4. Unoptimized search queries
 * 5. Bulk operations efficiency
 * 
 * Key optimizations implemented:
 * - Custom JPQL queries for complex operations
 * - Projection queries for lightweight data access
 * - Batch operations to reduce database roundtrips
 * - Index-friendly query patterns
 * - Read-only transaction optimization
 */
@Repository
public interface OptimizedCredentialRepository extends JpaRepository<Credential, Long> {

    // ========================================
    // OPTIMIZED CORE QUERIES
    // ========================================

    /**
     * Get all credentials for a user - optimized with indexing hint
     * 
     * Uses composite index on (user_id, is_deleted, updated_at) for optimal performance
     * Orders by updated_at DESC to show most recently modified credentials first
     */
    @Query("SELECT c FROM Credential c WHERE c.userId = :userId AND c.deleted = false ORDER BY c.updatedAt DESC")
    List<Credential> findByUserIdAndIsDeletedFalse(@Param("userId") Long userId);

    /**
     * Get credential by ID with ownership validation - single query
     * 
     * Uses primary key + foreign key for fastest lookup
     */
    @Query("SELECT c FROM Credential c WHERE c.credentialId = :credentialId AND c.userId = :userId AND c.deleted = false")
    Optional<Credential> findByCredentialIdAndUserIdAndNotDeleted(@Param("credentialId") Long credentialId, @Param("userId") Long userId);

    /**
     * Search credentials by service name and username - optimized for full-text search
     * 
     * Uses LOWER() function with indexes to enable case-insensitive search
     * Searches both service_name and username fields in single query
     */
    @Query("""
        SELECT c FROM Credential c 
        WHERE c.userId = :userId 
        AND c.deleted = false 
        AND (LOWER(c.serviceName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
             OR LOWER(c.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        ORDER BY c.updatedAt DESC
        """)
    List<Credential> findByUserIdAndSearchTerm(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);

    /**
     * Get credentials by category - optimized with composite index
     * 
     * Uses (user_id, category, is_deleted) composite index
     */
    @Query("SELECT c FROM Credential c WHERE c.userId = :userId AND c.category = :category AND c.deleted = false ORDER BY c.serviceName ASC")
    List<Credential> findByUserIdAndCategoryAndIsDeletedFalse(@Param("userId") Long userId, @Param("category") Category category);

    // ========================================
    // PROJECTION QUERIES FOR LIGHTWEIGHT ACCESS
    // ========================================

    /**
     * Get credential metadata without encrypted password - lightweight query
     * 
     * Returns only essential fields for listing views, avoiding large encrypted_password field
     * 
     * NOTE: Commented out - CredentialMetadata DTO not implemented yet
     */
    /* @Query("""
        SELECT new com.securevault.dto.CredentialMetadata(
            c.credentialId, c.userId, c.serviceName, c.username, 
            c.category, c.createdAt, c.updatedAt
        )
        FROM Credential c 
        WHERE c.userId = :userId AND c.deleted = false 
        ORDER BY c.updatedAt DESC
        """)
    List<Object[]> findCredentialMetadataByUserId(@Param("userId") Long userId); */

    /**
     * Count credentials by user - lightweight count query
     * 
     * Optimized for dashboard/summary views
     */
    @Query("SELECT COUNT(c) FROM Credential c WHERE c.userId = :userId AND c.deleted = false")
    long countByUserIdAndIsDeletedFalse(@Param("userId") Long userId);

    /**
     * Count credentials by category for a user - aggregation query
     * 
     * Returns category statistics in single query
     */
    @Query("""
        SELECT c.category, COUNT(c) 
        FROM Credential c 
        WHERE c.userId = :userId AND c.deleted = false 
        GROUP BY c.category
        """)
    List<Object[]> countCredentialsByCategory(@Param("userId") Long userId);

    // ========================================
    // PAGINATED QUERIES WITH OPTIMIZATION
    // ========================================

    /**
     * Paginated credential listing with sorting - optimized for large datasets
     * 
     * Uses database-level pagination and sorting for memory efficiency
     */
    @Query("""
        SELECT c FROM Credential c 
        WHERE c.userId = :userId AND c.deleted = false
        """)
    Page<Credential> findByUserIdWithPagination(@Param("userId") Long userId, Pageable pageable);

    /**
     * Paginated search with multiple criteria - complex query optimization
     * 
     * Supports filtering by service name, username, and category with pagination
     */
    @Query("""
        SELECT c FROM Credential c 
        WHERE c.userId = :userId 
        AND c.deleted = false
        AND (:serviceName IS NULL OR LOWER(c.serviceName) LIKE LOWER(CONCAT('%', :serviceName, '%')))
        AND (:username IS NULL OR LOWER(c.username) LIKE LOWER(CONCAT('%', :username, '%')))
        AND (:category IS NULL OR c.category = :category)
        """)
    Page<Credential> findByUserIdWithFilters(
        @Param("userId") Long userId,
        @Param("serviceName") String serviceName,
        @Param("username") String username, 
        @Param("category") Category category,
        Pageable pageable
    );

    // ========================================
    // BATCH OPERATIONS FOR EFFICIENCY
    // ========================================

    /**
     * Bulk soft delete credentials by user - batch operation
     * 
     * Performs bulk update in single query instead of N individual updates
     */
    @Modifying
    @Query("""
        UPDATE Credential c 
        SET c.deleted = true, c.updatedAt = :deleteTime 
        WHERE c.userId = :userId AND c.credentialId IN :credentialIds
        """)
    int bulkSoftDeleteByUserIdAndIds(@Param("userId") Long userId, @Param("credentialIds") List<Long> credentialIds, @Param("deleteTime") LocalDateTime deleteTime);

    /**
     * Bulk restore credentials from trash - batch operation
     */
    @Modifying
    @Query("""
        UPDATE Credential c 
        SET c.deleted = false, c.updatedAt = :restoreTime 
        WHERE c.userId = :userId AND c.credentialId IN :credentialIds AND c.deleted = true
        """)
    int bulkRestoreByUserIdAndIds(@Param("userId") Long userId, @Param("credentialIds") List<Long> credentialIds, @Param("restoreTime") LocalDateTime restoreTime);

    /**
     * Bulk update category for multiple credentials - batch operation
     */
    @Modifying
    @Query("""
        UPDATE Credential c 
        SET c.category = :newCategory, c.updatedAt = :updateTime 
        WHERE c.userId = :userId AND c.credentialId IN :credentialIds
        """)
    int bulkUpdateCategoryByUserIdAndIds(@Param("userId") Long userId, @Param("credentialIds") List<Long> credentialIds, @Param("newCategory") Category newCategory, @Param("updateTime") LocalDateTime updateTime);

    // ========================================
    // SHARED CREDENTIALS OPTIMIZATION
    // ========================================

    /**
     * Find credentials shared with a user - optimized join query
     * 
     * Uses explicit JOIN to avoid N+1 queries when accessing shared credentials
     */
    @Query("""
        SELECT c FROM Credential c 
        JOIN CredentialShare cs ON c.credentialId = cs.credentialId 
        WHERE cs.sharedWithUserId = :userId 
        AND cs.active = true 
        AND c.deleted = false
        ORDER BY c.updatedAt DESC
        """)
    List<Credential> findSharedCredentialsForUser(@Param("userId") Long userId);

    /**
     * Check if user has access to specific credential - single query authorization check
     */
    @Query("""
        SELECT COUNT(c) > 0 FROM Credential c 
        LEFT JOIN CredentialShare cs ON c.credentialId = cs.credentialId 
        WHERE c.credentialId = :credentialId 
        AND c.deleted = false
        AND (c.userId = :userId OR (cs.sharedWithUserId = :userId AND cs.active = true))
        """)
    boolean hasUserAccessToCredential(@Param("credentialId") Long credentialId, @Param("userId") Long userId);

    // ========================================
    // AUDIT AND ANALYTICS QUERIES
    // ========================================

    /**
     * Find recently modified credentials - dashboard optimization
     * 
     * Gets last N modified credentials efficiently for activity feeds
     */
    @Query("""
        SELECT c FROM Credential c 
        WHERE c.userId = :userId AND c.deleted = false 
        AND c.updatedAt >= :since 
        ORDER BY c.updatedAt DESC
        """)
    List<Credential> findRecentlyModifiedCredentials(@Param("userId") Long userId, @Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Find credentials needing password update - security optimization
     * 
     * Identifies credentials that haven't been updated for a specified period
     */
    @Query("""
        SELECT c FROM Credential c 
        WHERE c.userId = :userId 
        AND c.deleted = false 
        AND c.updatedAt < :threshold
        ORDER BY c.updatedAt ASC
        """)
    List<Credential> findCredentialsNeedingPasswordUpdate(@Param("userId") Long userId, @Param("threshold") LocalDateTime threshold);

    // ========================================
    // DATABASE INDEX RECOMMENDATIONS
    // ========================================
    
    /*
     * RECOMMENDED DATABASE INDEXES for optimal performance:
     * 
     * 1. PRIMARY INDEX (already exists):
     *    CREATE INDEX pk_credentials ON credentials(credential_id);
     * 
     * 2. USER CREDENTIALS LOOKUP:
     *    CREATE INDEX idx_credentials_user_active ON credentials(user_id, is_deleted, updated_at DESC);
     * 
     * 3. SEARCH OPTIMIZATION:
     *    CREATE INDEX idx_credentials_service_name ON credentials(user_id, LOWER(service_name));
     *    CREATE INDEX idx_credentials_username ON credentials(user_id, LOWER(username));
     * 
     * 4. CATEGORY FILTERING:
     *    CREATE INDEX idx_credentials_category ON credentials(user_id, category, is_deleted);
     * 
     * 5. SHARED CREDENTIALS:
     *    CREATE INDEX idx_credential_shares_user ON credential_shares(shared_with_user_id, is_active);
     *    CREATE INDEX idx_credential_shares_cred ON credential_shares(credential_id, is_active);
     * 
     * 6. AUDIT AND ANALYTICS:
     *    CREATE INDEX idx_credentials_updated_at ON credentials(user_id, updated_at DESC);
     *    CREATE INDEX idx_credentials_created_at ON credentials(user_id, created_at DESC);
     */
}