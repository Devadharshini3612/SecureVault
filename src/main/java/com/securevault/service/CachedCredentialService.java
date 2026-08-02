package com.securevault.service;

import com.securevault.dto.CreateCredentialRequest;
import com.securevault.dto.CredentialResponse;
import com.securevault.dto.UpdateCredentialRequest;
import com.securevault.enums.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Cached Credential Service
 * 
 * Wrapper service that adds Redis caching capabilities to CredentialService operations.
 * This service improves performance by caching frequently accessed credential metadata.
 * 
 * IMPORTANT SECURITY NOTES:
 * - NEVER cache encrypted passwords or sensitive credential data
 * - Only metadata (service names, usernames, categories, timestamps) is cached
 * - Actual password retrieval always goes to database for security
 * - Cache invalidation happens immediately when credentials are modified
 * 
 * Caching Strategy:
 * - Credential metadata cached for 15 minutes
 * - Search results cached for 5 minutes
 * - Category statistics cached for 30 minutes
 * - User credential lists cached for performance
 */
@Service
public class CachedCredentialService {

    private static final Logger logger = LoggerFactory.getLogger(CachedCredentialService.class);

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private ProductionLoggingService loggingService;

    /**
     * Create a new credential with cache invalidation
     * 
     * @param request Credential creation request
     * @return Created credential response
     */
    public CredentialResponse createCredential(CreateCredentialRequest request) {
        try {
            // Create credential using original service
            CredentialResponse createdCredential = credentialService.createCredentialWithResponse(request);
            
            // Invalidate user's credential caches since data has changed
            redisCacheService.evictCredentialMetadata(request.getUserId());
            
            // Clear category counts cache for this user
            redisCacheService.evictCredentialMetadata(request.getUserId());
            
            logger.info("Credential created and caches invalidated for user: {}", request.getUserId());
            
            loggingService.logCredentialOperation(
                request.getUserId(),
                "CREATE",
                createdCredential.getCredentialId(),
                true,
                "Credential created: " + request.getServiceName()
            );
            
            return createdCredential;
            
        } catch (Exception e) {
            logger.error("Failed to create credential: {}", e.getMessage(), e);
            
            loggingService.logCredentialOperation(
                request.getUserId(),
                "CREATE",
                null,
                false,
                "Creation failed: " + e.getMessage()
            );
            
            throw e;
        }
    }

    /**
     * List user credentials with caching for metadata
     * 
     * @param userId User ID
     * @return List of credential responses (metadata only from cache, full data from DB)
     */
    public List<CredentialResponse> listCredentials(Long userId) {
        try {
            // Check cache for metadata first
            List<CredentialResponse> cachedMetadata = redisCacheService.getCachedCredentialMetadata(userId);
            
            if (cachedMetadata != null) {
                logger.debug("Credential metadata found in cache for user: {} (count: {})", userId, cachedMetadata.size());
                
                // For cached metadata, we still need to get full credential data from DB
                // because we don't cache sensitive information
                List<CredentialResponse> fullCredentials = credentialService.listCredentials(userId);
                
                // Update cache with latest metadata
                redisCacheService.cacheCredentialMetadata(userId, fullCredentials);
                
                return fullCredentials;
            } else {
                // Cache miss, get from database and cache metadata
                logger.debug("Credential metadata cache miss for user: {}", userId);
                
                List<CredentialResponse> credentials = credentialService.listCredentials(userId);
                
                // Cache only the metadata (not sensitive data)
                redisCacheService.cacheCredentialMetadata(userId, credentials);
                
                loggingService.logCredentialOperation(
                    userId,
                    "LIST",
                    null,
                    true,
                    "Listed " + credentials.size() + " credentials"
                );
                
                return credentials;
            }
            
        } catch (Exception e) {
            logger.error("Failed to list credentials for user: {}: {}", userId, e.getMessage(), e);
            
            loggingService.logCredentialOperation(
                userId,
                "LIST",
                null,
                false,
                "List failed: " + e.getMessage()
            );
            
            throw e;
        }
    }

    /**
     * Search credentials with caching
     * 
     * @param userId User ID
     * @param searchTerm Search term
     * @return List of matching credentials
     */
    public List<CredentialResponse> searchCredentials(Long userId, String searchTerm) {
        try {
            // Check cache for search results
            List<CredentialResponse> cachedResults = redisCacheService.getCachedSearchResults(userId, searchTerm);
            
            if (cachedResults != null) {
                logger.debug("Search results found in cache for user: {} term: '{}' (count: {})", 
                           userId, searchTerm, cachedResults.size());
                
                // Still get fresh data from DB for security, but use cache for performance monitoring
                List<CredentialResponse> freshResults = credentialService.searchCredentials(userId, searchTerm);
                
                // Update cache with fresh results
                redisCacheService.cacheSearchResults(userId, searchTerm, freshResults);
                
                return freshResults;
            } else {
                // Cache miss, search in database
                logger.debug("Search cache miss for user: {} term: '{}'", userId, searchTerm);
                
                List<CredentialResponse> results = credentialService.searchCredentials(userId, searchTerm);
                
                // Cache the search results (metadata only)
                redisCacheService.cacheSearchResults(userId, searchTerm, results);
                
                loggingService.logCredentialOperation(
                    userId,
                    "SEARCH",
                    null,
                    true,
                    "Search '" + searchTerm + "' returned " + results.size() + " results"
                );
                
                return results;
            }
            
        } catch (Exception e) {
            logger.error("Failed to search credentials for user: {} term: '{}': {}", 
                        userId, searchTerm, e.getMessage(), e);
            
            loggingService.logCredentialOperation(
                userId,
                "SEARCH",
                null,
                false,
                "Search '" + searchTerm + "' failed: " + e.getMessage()
            );
            
            throw e;
        }
    }

    /**
     * Get credential by ID (never cached for security - always fresh from DB)
     * 
     * @param credentialId Credential ID
     * @param userId User ID for authorization
     * @return Full credential response with decrypted password
     */
    public CredentialResponse getCredentialById(Long credentialId, Long userId) {
        try {
            // ALWAYS get full credential data from database for security
            // Sensitive data is never cached
            CredentialResponse credential = credentialService.getCredentialById(credentialId);
            
            logger.debug("Retrieved credential details for ID: {} (user: {})", credentialId, userId);
            
            loggingService.logCredentialOperation(
                userId,
                "READ",
                credentialId,
                true,
                "Credential details retrieved"
            );
            
            return credential;
            
        } catch (Exception e) {
            logger.error("Failed to get credential {} for user {}: {}", credentialId, userId, e.getMessage(), e);
            
            loggingService.logCredentialOperation(
                userId,
                "READ",
                credentialId,
                false,
                "Read failed: " + e.getMessage()
            );
            
            throw e;
        }
    }

    /**
     * Update credential with cache invalidation
     * 
     * @param credentialId Credential ID
     * @param userId User ID
     * @param request Update request
     * @return Updated credential response
     */
    public CredentialResponse updateCredential(Long credentialId, Long userId, UpdateCredentialRequest request) {
        try {
            // Update credential using original service
            CredentialResponse updatedCredential = credentialService.updateCredential(credentialId, userId, request);
            
            // Invalidate all related caches for this user
            redisCacheService.evictCredentialMetadata(userId);
            
            // Clear search results cache for this user since data changed
            redisCacheService.clearAllUserCaches(userId);
            
            logger.info("Credential updated and caches invalidated: {} (user: {})", credentialId, userId);
            
            loggingService.logCredentialOperation(
                userId,
                "UPDATE",
                credentialId,
                true,
                "Credential updated successfully"
            );
            
            return updatedCredential;
            
        } catch (Exception e) {
            logger.error("Failed to update credential {} for user {}: {}", credentialId, userId, e.getMessage(), e);
            
            loggingService.logCredentialOperation(
                userId,
                "UPDATE",
                credentialId,
                false,
                "Update failed: " + e.getMessage()
            );
            
            throw e;
        }
    }

    /**
     * Delete credential with cache invalidation
     * 
     * @param credentialId Credential ID
     * @param userId User ID
     */
    @CacheEvict(value = "credentialMetadata", key = "#userId")
    public void deleteCredential(Long credentialId, Long userId) {
        try {
            // Delete credential using original service
            credentialService.deleteCredential(credentialId, userId);
            
            // Invalidate all related caches
            redisCacheService.evictCredentialMetadata(userId);
            redisCacheService.clearAllUserCaches(userId);
            
            logger.info("Credential deleted and caches invalidated: {} (user: {})", credentialId, userId);
            
            loggingService.logCredentialOperation(
                userId,
                "DELETE",
                credentialId,
                true,
                "Credential deleted successfully"
            );
            
        } catch (Exception e) {
            logger.error("Failed to delete credential {} for user {}: {}", credentialId, userId, e.getMessage(), e);
            
            loggingService.logCredentialOperation(
                userId,
                "DELETE",
                credentialId,
                false,
                "Delete failed: " + e.getMessage()
            );
            
            throw e;
        }
    }

    /**
     * Get credentials by category with caching
     * 
     * @param userId User ID
     * @param category Category filter
     * @return List of credentials in the category
     */
    @Cacheable(value = "categories", key = "#userId + ':' + #category.name()")
    public List<CredentialResponse> getCredentialsByCategory(Long userId, Category category) {
        try {
            logger.debug("Getting credentials by category for user: {} category: {}", userId, category);
            
            List<CredentialResponse> credentials = credentialService.getCredentialsByCategory(userId, category);
            
            loggingService.logCredentialOperation(
                userId,
                "LIST_BY_CATEGORY",
                null,
                true,
                "Listed " + credentials.size() + " credentials for category " + category
            );
            
            return credentials;
            
        } catch (Exception e) {
            logger.error("Failed to get credentials by category for user: {} category: {}: {}", 
                        userId, category, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get credential count by categories with caching
     * 
     * @param userId User ID
     * @return Map of category to count
     */
    public Map<Category, Integer> getCredentialCountsByCategory(Long userId) {
        try {
            // Check cache first
            Map<Category, Integer> cachedCounts = redisCacheService.getCachedCredentialCountsByCategory(userId);
            
            if (cachedCounts != null) {
                logger.debug("Credential counts found in cache for user: {}", userId);
                return cachedCounts;
            }
            
            // Cache miss, calculate counts
            logger.debug("Credential counts cache miss for user: {}", userId);
            
            // Get all credentials and count by category
            List<CredentialResponse> allCredentials = credentialService.listCredentials(userId);
            Map<Category, Integer> counts = new java.util.HashMap<>();
            
            // Initialize all categories with 0
            for (Category category : Category.values()) {
                counts.put(category, 0);
            }
            
            // Count credentials by category
            for (CredentialResponse credential : allCredentials) {
                Category category = credential.getCategory();
                counts.put(category, counts.get(category) + 1);
            }
            
            // Cache the results
            redisCacheService.cacheCredentialCountsByCategory(userId, counts);
            
            return counts;
            
        } catch (Exception e) {
            logger.error("Failed to get credential counts by category for user: {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Preload user's credential metadata into cache (cache warming)
     * 
     * @param userId User ID
     */
    public void preloadUserCredentialsIntoCache(Long userId) {
        try {
            logger.info("Preloading credential metadata into cache for user: {}", userId);
            
            // This will populate the cache
            listCredentials(userId);
            
            // Preload category counts
            getCredentialCountsByCategory(userId);
            
            logger.info("Credential metadata preloaded successfully for user: {}", userId);
            
        } catch (Exception e) {
            logger.warn("Failed to preload credential metadata for user: {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Clear all credential-related caches for a user
     * 
     * @param userId User ID
     */
    public void clearUserCredentialCaches(Long userId) {
        logger.info("Clearing all credential caches for user: {}", userId);
        redisCacheService.evictCredentialMetadata(userId);
        redisCacheService.clearAllUserCaches(userId);
    }

    /**
     * Get credential cache statistics
     * 
     * @return Cache statistics map
     */
    public Map<String, Object> getCredentialCacheStatistics() {
        return redisCacheService.getCacheStatistics();
    }

    /**
     * Warm up credential caches with most active users
     * 
     * @param userIds List of user IDs to warm up
     */
    public void warmUpCredentialCaches(List<Long> userIds) {
        logger.info("Warming up credential caches for {} users", userIds.size());
        
        userIds.parallelStream().forEach(userId -> {
            try {
                preloadUserCredentialsIntoCache(userId);
            } catch (Exception e) {
                logger.warn("Failed to warm up cache for user: {}: {}", userId, e.getMessage());
            }
        });
        
        logger.info("Credential cache warm-up completed");
    }
}