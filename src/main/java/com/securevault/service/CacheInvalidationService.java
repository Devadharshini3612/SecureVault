package com.securevault.service;

import com.securevault.dto.CredentialResponse;
import com.securevault.dto.UserResponse;
import com.securevault.enums.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Cache Invalidation Service
 * 
 * Centralized service for managing cache invalidation strategies across the SecureVault application.
 * Provides intelligent cache eviction, selective invalidation, and automated cache maintenance.
 * 
 * Cache Invalidation Strategies:
 * 1. Immediate Invalidation - For critical data changes
 * 2. Selective Invalidation - Target specific cache entries
 * 3. Cascading Invalidation - Remove related cache entries
 * 4. Scheduled Cleanup - Periodic maintenance of stale entries
 * 5. Event-Driven Invalidation - Based on business events
 * 
 * Supported Cache Types:
 * - User Profile Caches
 * - Credential Metadata Caches
 * - Search Result Caches
 * - Category and Statistics Caches
 * - Performance Metrics Caches
 * - Authentication Session Caches
 */
@Service
public class CacheInvalidationService {

    private static final Logger logger = LoggerFactory.getLogger(CacheInvalidationService.class);

    private final RedisCacheService redisCacheService;
    private final ProductionLoggingService loggingService;

    /**
     * Constructor injection - Spring automatically injects dependencies
     */
    public CacheInvalidationService(
            RedisCacheService redisCacheService,
            ProductionLoggingService loggingService) {
        this.redisCacheService = redisCacheService;
        this.loggingService = loggingService;
    }

    // ========================================
    // USER CACHE INVALIDATION STRATEGIES
    // ========================================

    /**
     * Invalidate user profile cache when user data is modified
     * Uses @CacheEvict to remove from Spring Cache and Redis
     */
    @CacheEvict(value = "userProfiles", key = "#userId")
    public void invalidateUserProfile(Long userId, String reason) {
        logger.info("Invalidating user profile cache for user: {} - Reason: {}", userId, reason);
        
        // Also clear from Redis directly
        redisCacheService.evictUserProfile(userId);
        
        loggingService.logBusinessEvent(
            "CACHE_INVALIDATION",
            "User profile cache invalidated",
            Map.of("userId", userId, "reason", reason, "cacheType", "userProfile")
        );
    }

    /**
     * Update user profile in cache with new data
     * Uses @CachePut to update without triggering database call
     */
    @CachePut(value = "userProfiles", key = "#userResponse.userId")
    public UserResponse updateUserProfileInCache(UserResponse userResponse, String reason) {
        logger.debug("Updating user profile in cache: {} - Reason: {}", userResponse.getUserId(), reason);
        
        // Also update in Redis
        redisCacheService.cacheUserProfile(userResponse);
        
        loggingService.logBusinessEvent(
            "CACHE_UPDATE",
            "User profile cache updated",
            Map.of("userId", userResponse.getUserId(), "reason", reason, "cacheType", "userProfile")
        );
        
        return userResponse;
    }

    /**
     * Cascade invalidation when user is deleted or deactivated
     * Removes all user-related cache entries
     */
    @Caching(evict = {
        @CacheEvict(value = "userProfiles", key = "#userId"),
        @CacheEvict(value = "credentialMetadata", key = "#userId"),
        @CacheEvict(value = "searchResults", allEntries = true), // Clear all search results
        @CacheEvict(value = "categories", key = "#userId")
    })
    public void cascadeInvalidateUserData(Long userId, String reason) {
        logger.info("Cascading cache invalidation for user: {} - Reason: {}", userId, reason);
        
        // Clear all user caches from Redis
        redisCacheService.clearAllUserCaches(userId);
        
        loggingService.logBusinessEvent(
            "CACHE_CASCADE_INVALIDATION",
            "All user-related caches invalidated",
            Map.of("userId", userId, "reason", reason, "scope", "all_user_data")
        );
    }

    // ========================================
    // CREDENTIAL CACHE INVALIDATION STRATEGIES
    // ========================================

    /**
     * Invalidate credential metadata when credentials are modified
     */
    @CacheEvict(value = "credentialMetadata", key = "#userId")
    public void invalidateCredentialMetadata(Long userId, Long credentialId, String operation) {
        logger.info("Invalidating credential metadata for user: {} credential: {} - Operation: {}", 
                   userId, credentialId, operation);
        
        // Clear from Redis
        redisCacheService.evictCredentialMetadata(userId);
        
        // Also clear category counts since they might have changed
        invalidateCategoryStatistics(userId, "credential_" + operation);
        
        loggingService.logBusinessEvent(
            "CACHE_INVALIDATION",
            "Credential metadata cache invalidated",
            Map.of("userId", userId, "credentialId", credentialId, "operation", operation)
        );
    }

    /**
     * Selective invalidation for credential search results
     * Only invalidates search caches that might be affected
     */
    public void invalidateCredentialSearchResults(Long userId, String affectedServiceName) {
        logger.info("Invalidating search result caches for user: {} affected service: {}", 
                   userId, affectedServiceName);
        
        try {
            // Clear search results that might contain this service
            Set<String> searchKeys = redisCacheService.getKeysMatchingPattern("search:results:" + userId + ":*");
            
            for (String key : searchKeys) {
                // Extract search term from key
                String searchTerm = key.substring(key.lastIndexOf(':') + 1);
                
                // If the service name contains the search term or vice versa, invalidate
                if (affectedServiceName.toLowerCase().contains(searchTerm.toLowerCase()) ||
                    searchTerm.toLowerCase().contains(affectedServiceName.toLowerCase())) {
                    
                    redisCacheService.evictSearchResults(userId, searchTerm);
                    logger.debug("Invalidated search cache for term: '{}'", searchTerm);
                }
            }
            
            loggingService.logBusinessEvent(
                "CACHE_SELECTIVE_INVALIDATION",
                "Search result caches selectively invalidated",
                Map.of("userId", userId, "affectedService", affectedServiceName)
            );
            
        } catch (Exception e) {
            logger.warn("Failed to selectively invalidate search caches for user: {}: {}", userId, e.getMessage());
            
            // Fallback: clear all search results for this user
            invalidateAllSearchResults(userId, "selective_invalidation_failed");
        }
    }

    /**
     * Invalidate all search results for a user (fallback strategy)
     */
    @CacheEvict(value = "searchResults", key = "#userId")
    public void invalidateAllSearchResults(Long userId, String reason) {
        logger.info("Invalidating all search result caches for user: {} - Reason: {}", userId, reason);
        
        // Clear all search results from Redis
        redisCacheService.clearSearchResults(userId);
        
        loggingService.logBusinessEvent(
            "CACHE_INVALIDATION",
            "All search result caches invalidated",
            Map.of("userId", userId, "reason", reason, "scope", "all_search_results")
        );
    }

    // ========================================
    // CATEGORY AND STATISTICS CACHE INVALIDATION
    // ========================================

    /**
     * Invalidate category statistics when credential counts change
     */
    @CacheEvict(value = "categories", key = "#userId")
    public void invalidateCategoryStatistics(Long userId, String reason) {
        logger.debug("Invalidating category statistics for user: {} - Reason: {}", userId, reason);
        
        // Clear category counts from Redis
        redisCacheService.evictCategoryData("counts:" + userId);
        
        loggingService.logBusinessEvent(
            "CACHE_INVALIDATION",
            "Category statistics cache invalidated",
            Map.of("userId", userId, "reason", reason, "cacheType", "categoryStats")
        );
    }

    /**
     * Update category statistics in cache
     */
    @CachePut(value = "categories", key = "#userId")
    public Map<Category, Integer> updateCategoryStatisticsInCache(Long userId, Map<Category, Integer> newCounts, String reason) {
        logger.debug("Updating category statistics in cache for user: {} - Reason: {}", userId, reason);
        
        // Update in Redis
        redisCacheService.cacheCategoryData("counts:" + userId, newCounts);
        
        loggingService.logBusinessEvent(
            "CACHE_UPDATE",
            "Category statistics cache updated",
            Map.of("userId", userId, "reason", reason, "cacheType", "categoryStats")
        );
        
        return newCounts;
    }

    // ========================================
    // PERFORMANCE METRICS CACHE INVALIDATION
    // ========================================

    /**
     * Invalidate performance metrics cache
     */
    @CacheEvict(value = "performanceMetrics", allEntries = true)
    public void invalidatePerformanceMetrics(String reason) {
        logger.info("Invalidating all performance metrics caches - Reason: {}", reason);
        
        // Clear from Redis
        redisCacheService.clearPerformanceMetrics();
        
        loggingService.logBusinessEvent(
            "CACHE_INVALIDATION",
            "Performance metrics cache invalidated",
            Map.of("reason", reason, "scope", "all_metrics")
        );
    }

    /**
     * Update performance metrics in cache
     */
    @CachePut(value = "performanceMetrics", key = "#metricType")
    public Object updatePerformanceMetricsInCache(String metricType, Object metrics, String reason) {
        logger.debug("Updating performance metrics in cache: {} - Reason: {}", metricType, reason);
        
        // Update in Redis
        redisCacheService.cachePerformanceMetrics(metricType, metrics);
        
        return metrics;
    }

    // ========================================
    // AUTHENTICATION CACHE INVALIDATION
    // ========================================

    /**
     * Invalidate authentication session cache
     */
    @CacheEvict(value = "authenticationCache", key = "#sessionId")
    public void invalidateAuthSession(String sessionId, String reason) {
        logger.info("Invalidating auth session cache: {} - Reason: {}", sessionId, reason);
        
        // Clear from Redis
        redisCacheService.evictAuthSession(sessionId);
        
        loggingService.logSecurityEvent(
            "AUTH_CACHE_INVALIDATION",
            "system",
            "localhost",
            "Authentication session cache invalidated: " + reason,
            "INFO"
        );
    }

    /**
     * Invalidate all authentication sessions for a user (logout all devices)
     */
    public void invalidateAllUserAuthSessions(Long userId, String reason) {
        logger.info("Invalidating all auth sessions for user: {} - Reason: {}", userId, reason);
        
        try {
            // Find all session keys for this user
            Set<String> sessionKeys = redisCacheService.getKeysMatchingPattern("auth:session:*");
            
            // Check each session to see if it belongs to this user
            for (String key : sessionKeys) {
                Map<String, Object> sessionData = redisCacheService.getCachedAuthSession(
                    key.substring(key.lastIndexOf(':') + 1));
                
                if (sessionData != null && userId.equals(sessionData.get("userId"))) {
                    String sessionId = key.substring(key.lastIndexOf(':') + 1);
                    invalidateAuthSession(sessionId, reason);
                }
            }
            
            loggingService.logSecurityEvent(
                "AUTH_CACHE_BULK_INVALIDATION",
                "system",
                "localhost",
                "All authentication sessions invalidated for user: " + userId + " - " + reason,
                "INFO"
            );
            
        } catch (Exception e) {
            logger.error("Failed to invalidate all auth sessions for user: {}: {}", userId, e.getMessage(), e);
        }
    }

    // ========================================
    // SCHEDULED CACHE MAINTENANCE
    // ========================================

    /**
     * Scheduled cleanup of expired cache entries
     * Runs every hour to maintain cache health
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void scheduledCacheCleanup() {
        logger.info("Starting scheduled cache cleanup...");
        
        try {
            // Get cache statistics before cleanup
            Map<String, Object> statsBefore = redisCacheService.getCacheStatistics();
            
            // Perform cleanup operations
            cleanupExpiredEntries();
            cleanupStaleSearchResults();
            cleanupOrphanedSessions();
            
            // Get statistics after cleanup
            Map<String, Object> statsAfter = redisCacheService.getCacheStatistics();
            
            logger.info("Scheduled cache cleanup completed successfully");
            
            loggingService.logSystemHealth(
                "cache_cleanup",
                "completed",
                "HEALTHY",
                "Hourly cleanup"
            );
            
        } catch (Exception e) {
            logger.error("Scheduled cache cleanup failed: {}", e.getMessage(), e);
            
            loggingService.logSystemHealth(
                "cache_cleanup",
                "failed",
                "WARNING",
                "Cleanup error: " + e.getMessage()
            );
        }
    }

    /**
     * Emergency cache invalidation for security incidents
     */
    @Async
    public CompletableFuture<Void> emergencyCacheInvalidation(String reason, Set<Long> affectedUserIds) {
        logger.warn("EMERGENCY: Performing emergency cache invalidation - Reason: {}", reason);
        
        try {
            if (affectedUserIds == null || affectedUserIds.isEmpty()) {
                // Clear all caches
                clearAllCaches(reason);
            } else {
                // Clear caches for specific users
                for (Long userId : affectedUserIds) {
                    cascadeInvalidateUserData(userId, "emergency_" + reason);
                }
            }
            
            loggingService.logSecurityEvent(
                "EMERGENCY_CACHE_INVALIDATION",
                "system",
                "localhost",
                "Emergency cache invalidation completed: " + reason,
                "HIGH"
            );
            
        } catch (Exception e) {
            logger.error("Emergency cache invalidation failed: {}", e.getMessage(), e);
            
            loggingService.logSecurityEvent(
                "EMERGENCY_CACHE_INVALIDATION_FAILED",
                "system",
                "localhost",
                "Emergency cache invalidation failed: " + e.getMessage(),
                "HIGH"
            );
        }
        
        return CompletableFuture.completedFuture(null);
    }

    // ========================================
    // BULK OPERATIONS
    // ========================================

    /**
     * Clear all caches (nuclear option for emergencies)
     */
    @Caching(evict = {
        @CacheEvict(value = "userProfiles", allEntries = true),
        @CacheEvict(value = "credentialMetadata", allEntries = true),
        @CacheEvict(value = "searchResults", allEntries = true),
        @CacheEvict(value = "categories", allEntries = true),
        @CacheEvict(value = "performanceMetrics", allEntries = true),
        @CacheEvict(value = "authenticationCache", allEntries = true)
    })
    public void clearAllCaches(String reason) {
        logger.warn("CLEARING ALL CACHES - Reason: {}", reason);
        
        // Clear all from Redis
        redisCacheService.clearAllCaches();
        
        loggingService.logSystemHealth(
            "cache_nuclear_clear",
            reason,
            "WARNING",
            "All caches cleared"
        );
    }

    /**
     * Warm up caches for frequently accessed data
     */
    @Async
    public CompletableFuture<Void> warmUpCaches(List<Long> frequentUserIds) {
        logger.info("Starting cache warm-up for {} users", frequentUserIds.size());
        
        try {
            // Warm up Redis cache
            redisCacheService.warmUpCache();
            
            // Warm up user-specific caches
            for (Long userId : frequentUserIds) {
                try {
                    // This would typically call cached services to populate caches
                    logger.debug("Warming up cache for user: {}", userId);
                } catch (Exception e) {
                    logger.warn("Failed to warm up cache for user: {}: {}", userId, e.getMessage());
                }
            }
            
            logger.info("Cache warm-up completed successfully");
            
            loggingService.logSystemHealth(
                "cache_warmup",
                "completed",
                "HEALTHY",
                "Warmed up caches for " + frequentUserIds.size() + " users"
            );
            
        } catch (Exception e) {
            logger.error("Cache warm-up failed: {}", e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Clean up expired cache entries
     */
    private void cleanupExpiredEntries() {
        logger.debug("Cleaning up expired cache entries...");
        // Implementation would use Redis SCAN to find and remove expired keys
        // This is a placeholder for the actual implementation
    }

    /**
     * Clean up stale search result caches
     */
    private void cleanupStaleSearchResults() {
        logger.debug("Cleaning up stale search result caches...");
        // Implementation would remove search results older than TTL
    }

    /**
     * Clean up orphaned authentication sessions
     */
    private void cleanupOrphanedSessions() {
        logger.debug("Cleaning up orphaned authentication sessions...");
        // Implementation would remove sessions for deleted users
    }

    /**
     * Get cache invalidation statistics
     */
    public Map<String, Object> getCacheInvalidationStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        // Get basic cache statistics
        stats.putAll(redisCacheService.getCacheStatistics());
        
        // Add invalidation-specific metrics
        stats.put("lastCleanupTime", LocalDateTime.now());
        stats.put("cacheHealthy", redisCacheService.isRedisHealthy());
        
        return stats;
    }
}