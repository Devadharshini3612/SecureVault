package com.securevault.service;

import com.securevault.dto.CredentialResponse;
import com.securevault.dto.UserResponse;
import com.securevault.enums.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis Cache Service for SecureVault
 * 
 * Provides centralized caching operations for:
 * - User profile caching for faster authentication
 * - Credential metadata caching (NO sensitive data)
 * - Category and enumeration caching
 * - Search results caching for performance
 * - Performance metrics caching
 * 
 * SECURITY NOTES:
 * - NEVER cache encrypted passwords or sensitive credential data
 * - Only cache metadata and non-sensitive information
 * - All cached data has appropriate TTL values
 * - Cache keys are namespaced to prevent collisions
 */
@Service
public class RedisCacheService {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, Object> userRedisTemplate;
    private final RedisTemplate<String, Object> metricsRedisTemplate;

    /**
     * Constructor injection - Spring automatically injects dependencies
     */
    public RedisCacheService(
            RedisTemplate<String, Object> redisTemplate,
            @Qualifier("userRedisTemplate") RedisTemplate<String, Object> userRedisTemplate,
            @Qualifier("metricsRedisTemplate") RedisTemplate<String, Object> metricsRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.userRedisTemplate = userRedisTemplate;
        this.metricsRedisTemplate = metricsRedisTemplate;
    }

    // Cache key prefixes for organization
    private static final String USER_PROFILE_PREFIX = "user:profile:";
    private static final String CREDENTIAL_METADATA_PREFIX = "credential:metadata:";
    private static final String SEARCH_RESULTS_PREFIX = "search:results:";
    private static final String CATEGORY_DATA_PREFIX = "category:data:";
    private static final String PERFORMANCE_METRICS_PREFIX = "performance:metrics:";
    private static final String AUTH_SESSION_PREFIX = "auth:session:";

    // ========================================
    // USER PROFILE CACHING
    // ========================================

    /**
     * Cache user profile data for faster authentication and authorization
     * TTL: 2 hours (user data changes infrequently)
     */
    @Cacheable(value = "userProfiles", key = "#userId")
    public UserResponse getCachedUserProfile(Long userId) {
        logger.debug("Cache MISS for user profile: {}", userId);
        return null; // This will be called only on cache miss
    }

    /**
     * Update user profile in cache
     */
    @CachePut(value = "userProfiles", key = "#userResponse.userId")
    public UserResponse cacheUserProfile(UserResponse userResponse) {
        logger.debug("Caching user profile for user: {}", userResponse.getUserId());
        return userResponse;
    }

    /**
     * Remove user profile from cache (used when user data is updated)
     */
    @CacheEvict(value = "userProfiles", key = "#userId")
    public void evictUserProfile(Long userId) {
        logger.debug("Evicting user profile from cache: {}", userId);
    }

    /**
     * Cache user profile by email for login optimization
     */
    public void cacheUserProfileByEmail(String email, UserResponse userResponse) {
        String key = USER_PROFILE_PREFIX + "email:" + email;
        userRedisTemplate.opsForValue().set(key, userResponse, Duration.ofHours(2));
        logger.debug("Cached user profile by email: {}", email);
    }

    /**
     * Get cached user profile by email
     */
    public UserResponse getCachedUserProfileByEmail(String email) {
        String key = USER_PROFILE_PREFIX + "email:" + email;
        UserResponse userResponse = (UserResponse) userRedisTemplate.opsForValue().get(key);
        
        if (userResponse != null) {
            logger.debug("Cache HIT for user profile by email: {}", email);
        } else {
            logger.debug("Cache MISS for user profile by email: {}", email);
        }
        
        return userResponse;
    }

    // ========================================
    // CREDENTIAL METADATA CACHING
    // ========================================

    /**
     * Cache credential metadata (NO sensitive data like passwords)
     * Only caches: ID, service name, username, category, timestamps
     */
    public void cacheCredentialMetadata(Long userId, List<CredentialResponse> credentials) {
        String key = CREDENTIAL_METADATA_PREFIX + userId;
        
        // Remove sensitive data before caching
        List<CredentialResponse> safeCredentials = credentials.stream()
                .map(this::createSafeCredentialMetadata)
                .toList();
        
        redisTemplate.opsForValue().set(key, safeCredentials, Duration.ofMinutes(15));
        logger.debug("Cached credential metadata for user: {} (count: {})", userId, credentials.size());
    }

    /**
     * Get cached credential metadata
     */
    @SuppressWarnings("unchecked")
    public List<CredentialResponse> getCachedCredentialMetadata(Long userId) {
        String key = CREDENTIAL_METADATA_PREFIX + userId;
        List<CredentialResponse> credentials = (List<CredentialResponse>) redisTemplate.opsForValue().get(key);
        
        if (credentials != null) {
            logger.debug("Cache HIT for credential metadata user: {} (count: {})", userId, credentials.size());
        } else {
            logger.debug("Cache MISS for credential metadata user: {}", userId);
        }
        
        return credentials;
    }

    /**
     * Remove credential metadata from cache
     */
    public void evictCredentialMetadata(Long userId) {
        String key = CREDENTIAL_METADATA_PREFIX + userId;
        redisTemplate.delete(key);
        logger.debug("Evicted credential metadata for user: {}", userId);
    }

    // ========================================
    // SEARCH RESULTS CACHING
    // ========================================

    /**
     * Cache search results for performance optimization
     * TTL: 5 minutes (balance between performance and data freshness)
     */
    public void cacheSearchResults(Long userId, String searchTerm, List<CredentialResponse> results) {
        String key = SEARCH_RESULTS_PREFIX + userId + ":" + searchTerm.toLowerCase();
        
        // Remove sensitive data before caching
        List<CredentialResponse> safeResults = results.stream()
                .map(this::createSafeCredentialMetadata)
                .toList();
        
        redisTemplate.opsForValue().set(key, safeResults, Duration.ofMinutes(5));
        logger.debug("Cached search results for user: {} term: '{}' (count: {})", userId, searchTerm, results.size());
    }

    /**
     * Get cached search results
     */
    @SuppressWarnings("unchecked")
    public List<CredentialResponse> getCachedSearchResults(Long userId, String searchTerm) {
        String key = SEARCH_RESULTS_PREFIX + userId + ":" + searchTerm.toLowerCase();
        List<CredentialResponse> results = (List<CredentialResponse>) redisTemplate.opsForValue().get(key);
        
        if (results != null) {
            logger.debug("Cache HIT for search results user: {} term: '{}' (count: {})", userId, searchTerm, results.size());
        } else {
            logger.debug("Cache MISS for search results user: {} term: '{}'", userId, searchTerm);
        }
        
        return results;
    }

    // ========================================
    // CATEGORY DATA CACHING
    // ========================================

    /**
     * Cache category statistics and enumeration data
     * TTL: 6 hours (relatively static data)
     */
    public void cacheCategoryData(String dataType, Object data) {
        String key = CATEGORY_DATA_PREFIX + dataType;
        redisTemplate.opsForValue().set(key, data, Duration.ofHours(6));
        logger.debug("Cached category data: {}", dataType);
    }

    /**
     * Get cached category data
     */
    public Object getCachedCategoryData(String dataType) {
        String key = CATEGORY_DATA_PREFIX + dataType;
        Object data = redisTemplate.opsForValue().get(key);
        
        if (data != null) {
            logger.debug("Cache HIT for category data: {}", dataType);
        } else {
            logger.debug("Cache MISS for category data: {}", dataType);
        }
        
        return data;
    }

    /**
     * Cache user's credential count by category
     */
    public void cacheCredentialCountsByCategory(Long userId, Map<Category, Integer> counts) {
        String key = CATEGORY_DATA_PREFIX + "counts:" + userId;
        redisTemplate.opsForValue().set(key, counts, Duration.ofMinutes(30));
        logger.debug("Cached credential counts by category for user: {}", userId);
    }

    /**
     * Get cached credential counts by category
     */
    @SuppressWarnings("unchecked")
    public Map<Category, Integer> getCachedCredentialCountsByCategory(Long userId) {
        String key = CATEGORY_DATA_PREFIX + "counts:" + userId;
        Map<Category, Integer> counts = (Map<Category, Integer>) redisTemplate.opsForValue().get(key);
        
        if (counts != null) {
            logger.debug("Cache HIT for credential counts user: {}", userId);
        } else {
            logger.debug("Cache MISS for credential counts user: {}", userId);
        }
        
        return counts;
    }

    // ========================================
    // PERFORMANCE METRICS CACHING
    // ========================================

    /**
     * Cache performance metrics for monitoring dashboard
     * TTL: 2 minutes (near real-time data)
     */
    public void cachePerformanceMetrics(String metricType, Object metrics) {
        String key = PERFORMANCE_METRICS_PREFIX + metricType;
        metricsRedisTemplate.opsForValue().set(key, metrics, Duration.ofMinutes(2));
        logger.debug("Cached performance metrics: {}", metricType);
    }

    /**
     * Get cached performance metrics
     */
    public Object getCachedPerformanceMetrics(String metricType) {
        String key = PERFORMANCE_METRICS_PREFIX + metricType;
        Object metrics = metricsRedisTemplate.opsForValue().get(key);
        
        if (metrics != null) {
            logger.debug("Cache HIT for performance metrics: {}", metricType);
        } else {
            logger.debug("Cache MISS for performance metrics: {}", metricType);
        }
        
        return metrics;
    }

    // ========================================
    // AUTHENTICATION SESSION CACHING
    // ========================================

    /**
     * Cache authentication session data for JWT validation
     * TTL: 10 minutes (security vs performance balance)
     */
    public void cacheAuthSession(String sessionId, Map<String, Object> sessionData) {
        String key = AUTH_SESSION_PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, sessionData, Duration.ofMinutes(10));
        logger.debug("Cached auth session: {}", sessionId);
    }

    /**
     * Get cached authentication session
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCachedAuthSession(String sessionId) {
        String key = AUTH_SESSION_PREFIX + sessionId;
        Map<String, Object> sessionData = (Map<String, Object>) redisTemplate.opsForValue().get(key);
        
        if (sessionData != null) {
            logger.debug("Cache HIT for auth session: {}", sessionId);
        } else {
            logger.debug("Cache MISS for auth session: {}", sessionId);
        }
        
        return sessionData;
    }

    /**
     * Remove authentication session from cache
     */
    public void evictAuthSession(String sessionId) {
        String key = AUTH_SESSION_PREFIX + sessionId;
        redisTemplate.delete(key);
        logger.debug("Evicted auth session: {}", sessionId);
    }

    // ========================================
    // CACHE MANAGEMENT UTILITIES
    // ========================================

    /**
     * Clear all caches for a specific user (used when user data changes significantly)
     */
    public void clearAllUserCaches(Long userId) {
        evictUserProfile(userId);
        evictCredentialMetadata(userId);
        
        // Clear search results for this user
        Set<String> searchKeys = redisTemplate.keys(SEARCH_RESULTS_PREFIX + userId + ":*");
        if (searchKeys != null && !searchKeys.isEmpty()) {
            redisTemplate.delete(searchKeys);
        }
        
        // Clear category counts
        redisTemplate.delete(CATEGORY_DATA_PREFIX + "counts:" + userId);
        
        logger.info("Cleared all cache entries for user: {}", userId);
    }

    /**
     * Get cache statistics for monitoring
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        try {
            // Count keys by prefix
            stats.put("userProfileCount", countKeysByPattern(USER_PROFILE_PREFIX + "*"));
            stats.put("credentialMetadataCount", countKeysByPattern(CREDENTIAL_METADATA_PREFIX + "*"));
            stats.put("searchResultsCount", countKeysByPattern(SEARCH_RESULTS_PREFIX + "*"));
            stats.put("categoryDataCount", countKeysByPattern(CATEGORY_DATA_PREFIX + "*"));
            stats.put("performanceMetricsCount", countKeysByPattern(PERFORMANCE_METRICS_PREFIX + "*"));
            stats.put("authSessionCount", countKeysByPattern(AUTH_SESSION_PREFIX + "*"));
            
            // Redis server info
            stats.put("redisInfo", redisTemplate.getConnectionFactory().getConnection().info());
            
        } catch (Exception e) {
            logger.error("Failed to retrieve cache statistics: {}", e.getMessage());
            stats.put("error", "Failed to retrieve statistics: " + e.getMessage());
        }
        
        return stats;
    }

    /**
     * Warm up cache with frequently accessed data
     */
    public void warmUpCache() {
        logger.info("Starting cache warm-up process...");
        
        try {
            // Cache category enum data
            for (Category category : Category.values()) {
                cacheCategoryData("enum:" + category.name(), category);
            }
            
            logger.info("Cache warm-up completed successfully");
        } catch (Exception e) {
            logger.error("Cache warm-up failed: {}", e.getMessage(), e);
        }
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Creates a safe credential metadata object with sensitive data removed
     */
    private CredentialResponse createSafeCredentialMetadata(CredentialResponse credential) {
        CredentialResponse safe = new CredentialResponse();
        safe.setCredentialId(credential.getCredentialId());
        safe.setServiceName(credential.getServiceName());
        safe.setUsername(credential.getUsername());
        safe.setCategory(credential.getCategory());
        safe.setCreatedAt(credential.getCreatedAt());
        safe.setUpdatedAt(credential.getUpdatedAt());
        // NOTE: Deliberately NOT setting password/encrypted data
        return safe;
    }

    /**
     * Counts keys matching a pattern
     */
    private long countKeysByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        return keys != null ? keys.size() : 0;
    }

    /**
     * Check if Redis is available and healthy
     */
    public boolean isRedisHealthy() {
        try {
            redisTemplate.opsForValue().set("health:check", "ok", Duration.ofSeconds(10));
            String result = (String) redisTemplate.opsForValue().get("health:check");
            return "ok".equals(result);
        } catch (Exception e) {
            logger.warn("Redis health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get keys matching a pattern (for selective invalidation)
     */
    public Set<String> getKeysMatchingPattern(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            logger.error("Failed to get keys matching pattern '{}': {}", pattern, e.getMessage());
            return new java.util.HashSet<>();
        }
    }

    /**
     * Clear search results for a specific user
     */
    public void clearSearchResults(Long userId) {
        try {
            Set<String> searchKeys = redisTemplate.keys(SEARCH_RESULTS_PREFIX + userId + ":*");
            if (searchKeys != null && !searchKeys.isEmpty()) {
                redisTemplate.delete(searchKeys);
                logger.debug("Cleared {} search result caches for user: {}", searchKeys.size(), userId);
            }
        } catch (Exception e) {
            logger.error("Failed to clear search results for user: {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Evict specific search results
     */
    public void evictSearchResults(Long userId, String searchTerm) {
        String key = SEARCH_RESULTS_PREFIX + userId + ":" + searchTerm.toLowerCase();
        redisTemplate.delete(key);
        logger.debug("Evicted search results for user: {} term: '{}'", userId, searchTerm);
    }

    /**
     * Clear performance metrics cache
     */
    public void clearPerformanceMetrics() {
        try {
            Set<String> metricsKeys = metricsRedisTemplate.keys(PERFORMANCE_METRICS_PREFIX + "*");
            if (metricsKeys != null && !metricsKeys.isEmpty()) {
                metricsRedisTemplate.delete(metricsKeys);
                logger.debug("Cleared {} performance metrics caches", metricsKeys.size());
            }
        } catch (Exception e) {
            logger.error("Failed to clear performance metrics: {}", e.getMessage());
        }
    }

    /**
     * Evict category data
     */
    public void evictCategoryData(String dataType) {
        String key = CATEGORY_DATA_PREFIX + dataType;
        redisTemplate.delete(key);
        logger.debug("Evicted category data: {}", dataType);
    }

    /**
     * Clear all caches (nuclear option)
     */
    public void clearAllCaches() {
        try {
            // Get all keys and delete them
            Set<String> allKeys = redisTemplate.keys("securevault:*");
            if (allKeys != null && !allKeys.isEmpty()) {
                redisTemplate.delete(allKeys);
                logger.warn("Cleared ALL cache entries ({} keys)", allKeys.size());
            }
        } catch (Exception e) {
            logger.error("Failed to clear all caches: {}", e.getMessage());
        }
    }
}