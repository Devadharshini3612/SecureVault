package com.securevault.controller;

import com.securevault.dto.ApiResponse;
import com.securevault.service.CacheInvalidationService;
import com.securevault.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cache Management Controller
 * 
 * Provides administrative endpoints for cache management and monitoring.
 * These endpoints are typically used by system administrators or monitoring tools.
 * 
 * Security: All endpoints require admin privileges or are restricted to development environments.
 * 
 * Available Operations:
 * - Cache statistics and health monitoring
 * - Selective cache invalidation
 * - Emergency cache clearing
 * - Cache warm-up operations
 * - Redis connection monitoring
 */
@RestController
@RequestMapping("/api/cache")
public class CacheManagementController {

    private static final Logger logger = LoggerFactory.getLogger(CacheManagementController.class);

    @Autowired
    private CacheInvalidationService cacheInvalidationService;

    @Autowired
    private RedisCacheService redisCacheService;

    /**
     * Get cache statistics and health information
     * 
     * GET /api/cache/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCacheStatistics() {
        try {
            Map<String, Object> stats = cacheInvalidationService.getCacheInvalidationStatistics();
            
            return ResponseEntity.ok(
                ApiResponse.success("Cache statistics retrieved successfully", stats)
            );
        } catch (Exception e) {
            logger.error("Failed to retrieve cache statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve cache statistics: " + e.getMessage()));
        }
    }

    /**
     * Check Redis health status
     * 
     * GET /api/cache/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCacheHealth() {
        Map<String, Object> health = new java.util.HashMap<>();
        
        try {
            boolean isHealthy = redisCacheService.isRedisHealthy();
            health.put("redis", isHealthy ? "UP" : "DOWN");
            health.put("timestamp", java.time.LocalDateTime.now());
            
            if (isHealthy) {
                health.put("status", "HEALTHY");
                health.put("message", "Redis is responding normally");
                
                return ResponseEntity.ok(
                    ApiResponse.success("Cache is healthy", health)
                );
            } else {
                health.put("status", "UNHEALTHY");
                health.put("message", "Redis is not responding");
                
                return ResponseEntity.status(503)
                        .body(ApiResponse.error("Cache is unhealthy", health));
            }
            
        } catch (Exception e) {
            health.put("status", "ERROR");
            health.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Cache health check failed", health));
        }
    }

    /**
     * Invalidate cache for a specific user
     * 
     * DELETE /api/cache/user/{userId}
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<String>> invalidateUserCache(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "manual_admin_request") String reason) {
        
        try {
            cacheInvalidationService.cascadeInvalidateUserData(userId, reason);
            
            logger.info("Admin invalidated cache for user: {} - Reason: {}", userId, reason);
            
            return ResponseEntity.ok(
                ApiResponse.success("User cache invalidated successfully", 
                                  "All cache entries for user " + userId + " have been cleared")
            );
        } catch (Exception e) {
            logger.error("Failed to invalidate user cache for user: {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to invalidate user cache: " + e.getMessage()));
        }
    }

    /**
     * Invalidate all search result caches for a user
     * 
     * DELETE /api/cache/user/{userId}/search
     */
    @DeleteMapping("/user/{userId}/search")
    public ResponseEntity<ApiResponse<String>> invalidateUserSearchCache(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "admin_search_cache_clear") String reason) {
        
        try {
            cacheInvalidationService.invalidateAllSearchResults(userId, reason);
            
            return ResponseEntity.ok(
                ApiResponse.success("Search cache invalidated successfully", 
                                  "All search result caches for user " + userId + " have been cleared")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to invalidate search cache: " + e.getMessage()));
        }
    }

    /**
     * Invalidate authentication sessions for a user
     * 
     * DELETE /api/cache/user/{userId}/auth
     */
    @DeleteMapping("/user/{userId}/auth")
    public ResponseEntity<ApiResponse<String>> invalidateUserAuthSessions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "admin_session_invalidation") String reason) {
        
        try {
            cacheInvalidationService.invalidateAllUserAuthSessions(userId, reason);
            
            logger.info("Admin invalidated auth sessions for user: {} - Reason: {}", userId, reason);
            
            return ResponseEntity.ok(
                ApiResponse.success("Authentication sessions invalidated successfully", 
                                  "All auth sessions for user " + userId + " have been cleared")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to invalidate auth sessions: " + e.getMessage()));
        }
    }

    /**
     * Invalidate performance metrics cache
     * 
     * DELETE /api/cache/performance
     */
    @DeleteMapping("/performance")
    public ResponseEntity<ApiResponse<String>> invalidatePerformanceCache(
            @RequestParam(defaultValue = "admin_metrics_refresh") String reason) {
        
        try {
            cacheInvalidationService.invalidatePerformanceMetrics(reason);
            
            return ResponseEntity.ok(
                ApiResponse.success("Performance metrics cache invalidated successfully", 
                                  "All performance metrics have been cleared from cache")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to invalidate performance cache: " + e.getMessage()));
        }
    }

    /**
     * Emergency cache clear (nuclear option)
     * 
     * DELETE /api/cache/emergency
     */
    @DeleteMapping("/emergency")
    public ResponseEntity<ApiResponse<String>> emergencyCacheClear(
            @RequestParam String reason,
            @RequestParam(required = false) Set<Long> affectedUserIds) {
        
        try {
            logger.warn("EMERGENCY CACHE CLEAR initiated - Reason: {}", reason);
            
            cacheInvalidationService.emergencyCacheInvalidation(reason, affectedUserIds);
            
            String message = affectedUserIds != null && !affectedUserIds.isEmpty() 
                ? "Emergency cache clear completed for " + affectedUserIds.size() + " users"
                : "Emergency cache clear completed for ALL users";
            
            return ResponseEntity.ok(
                ApiResponse.success("Emergency cache clear initiated", message)
            );
        } catch (Exception e) {
            logger.error("Emergency cache clear failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Emergency cache clear failed: " + e.getMessage()));
        }
    }

    /**
     * Warm up caches for frequently accessed users
     * 
     * POST /api/cache/warmup
     */
    @PostMapping("/warmup")
    public ResponseEntity<ApiResponse<String>> warmUpCaches(
            @RequestBody(required = false) List<Long> userIds) {
        
        try {
            if (userIds == null) {
                userIds = List.of(); // Empty list for general warm-up
            }
            
            cacheInvalidationService.warmUpCaches(userIds);
            
            return ResponseEntity.ok(
                ApiResponse.success("Cache warm-up initiated", 
                                  "Cache warm-up started for " + userIds.size() + " users")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Cache warm-up failed: " + e.getMessage()));
        }
    }

    /**
     * Get cache keys matching a pattern (for debugging)
     * 
     * GET /api/cache/keys?pattern={pattern}
     */
    @GetMapping("/keys")
    public ResponseEntity<ApiResponse<Set<String>>> getCacheKeys(
            @RequestParam String pattern) {
        
        try {
            Set<String> keys = redisCacheService.getKeysMatchingPattern(pattern);
            
            return ResponseEntity.ok(
                ApiResponse.success("Cache keys retrieved successfully", keys)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve cache keys: " + e.getMessage()));
        }
    }

    /**
     * Test cache connectivity and performance
     * 
     * GET /api/cache/test
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testCachePerformance() {
        Map<String, Object> testResults = new java.util.HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Test write performance
            String testKey = "test:performance:" + System.currentTimeMillis();
            String testValue = "test_value_" + System.currentTimeMillis();
            
            redisCacheService.cachePerformanceMetrics(testKey, testValue);
            long writeTime = System.currentTimeMillis() - startTime;
            
            // Test read performance
            startTime = System.currentTimeMillis();
            Object cachedValue = redisCacheService.getCachedPerformanceMetrics(testKey);
            long readTime = System.currentTimeMillis() - startTime;
            
            // Clean up test data
            redisCacheService.evictCategoryData(testKey);
            
            testResults.put("writeTimeMs", writeTime);
            testResults.put("readTimeMs", readTime);
            testResults.put("totalTimeMs", writeTime + readTime);
            testResults.put("success", testValue.equals(cachedValue));
            testResults.put("timestamp", java.time.LocalDateTime.now());
            
            String status = (writeTime + readTime < 100) ? "EXCELLENT" : 
                           (writeTime + readTime < 500) ? "GOOD" : "SLOW";
            testResults.put("performance", status);
            
            return ResponseEntity.ok(
                ApiResponse.success("Cache performance test completed", testResults)
            );
            
        } catch (Exception e) {
            testResults.put("error", e.getMessage());
            testResults.put("success", false);
            
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Cache performance test failed", testResults));
        }
    }

    /**
     * Get cache configuration information
     * 
     * GET /api/cache/config
     */
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCacheConfiguration() {
        Map<String, Object> config = new java.util.HashMap<>();
        
        try {
            config.put("cacheType", "Redis");
            config.put("defaultTTL", "30 minutes");
            config.put("userProfileTTL", "2 hours");
            config.put("credentialMetadataTTL", "15 minutes");
            config.put("searchResultsTTL", "5 minutes");
            config.put("categoriesTTL", "6 hours");
            config.put("performanceMetricsTTL", "2 minutes");
            config.put("authSessionTTL", "10 minutes");
            
            config.put("redisHealth", redisCacheService.isRedisHealthy());
            config.put("asyncEnabled", true);
            config.put("compressionEnabled", true);
            config.put("securityEnabled", true);
            
            return ResponseEntity.ok(
                ApiResponse.success("Cache configuration retrieved successfully", config)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve cache configuration: " + e.getMessage()));
        }
    }
}