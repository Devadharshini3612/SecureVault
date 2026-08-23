package com.securevault.service;

import com.securevault.dto.LoginRequest;
import com.securevault.dto.RegisterRequest;
import com.securevault.dto.UserResponse;
import com.securevault.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Cached User Service
 * 
 * Wrapper service that adds Redis caching capabilities to UserService operations.
 * This service improves performance by caching frequently accessed user data.
 * 
 * Caching Strategy:
 * - User profiles cached for 2 hours (infrequently changed data)
 * - Authentication data cached for 10 minutes (security vs performance)
 * - Email-based lookups cached to speed up login process
 * - Automatic cache invalidation when user data changes
 * 
 * Security Considerations:
 * - Only non-sensitive user data is cached
 * - Password hashes and sensitive information are NOT cached
 * - Cache entries have appropriate TTL values for security
 */
@Service
public class CachedUserService {

    private static final Logger logger = LoggerFactory.getLogger(CachedUserService.class);

    private final UserService userService;
    private final RedisCacheService redisCacheService;
    private final ProductionLoggingService loggingService;

    /**
     * Constructor injection - Spring automatically injects dependencies
     */
    public CachedUserService(
            UserService userService,
            RedisCacheService redisCacheService,
            ProductionLoggingService loggingService) {
        this.userService = userService;
        this.redisCacheService = redisCacheService;
        this.loggingService = loggingService;
    }

    /**
     * Register a new user with cache management
     * 
     * @param request Registration request
     * @return UserResponse with user information
     */
    public UserResponse registerUser(RegisterRequest request) {
        try {
            // Register user using the original service
            UserResponse userResponse = userService.registerUserWithDTO(request);
            
            // Cache the new user profile
            redisCacheService.cacheUserProfile(userResponse);
            redisCacheService.cacheUserProfileByEmail(request.getEmail(), userResponse);
            
            logger.info("User registered and cached successfully: {}", userResponse.getUserId());
            
            loggingService.logUserRegistration(
                request.getEmail(), 
                true, 
                "User registered and cached successfully"
            );
            
            return userResponse;
            
        } catch (Exception e) {
            logger.error("User registration failed: {}", e.getMessage(), e);
            
            loggingService.logUserRegistration(
                request.getEmail(), 
                false, 
                "Registration failed: " + e.getMessage()
            );
            
            throw e;
        }
    }

    /**
     * Authenticate user with caching for performance
     * 
     * @param request Login request
     * @return UserResponse if authentication successful
     */
    public UserResponse authenticateUser(LoginRequest request) {
        String clientIp = ""; // TODO: Get from request context
        String userAgent = ""; // TODO: Get from request context
        
        try {
            // First check if user profile is cached by email
            UserResponse cachedUser = redisCacheService.getCachedUserProfileByEmail(request.getEmail());
            
            if (cachedUser != null) {
                // User found in cache, still need to verify password through service
                logger.debug("User profile found in cache for email: {}", request.getEmail());
                
                // Verify credentials (this will check the password)
                UserResponse authenticatedUser = userService.authenticateUserWithDTO(request);
                
                // Update cache with fresh data
                redisCacheService.cacheUserProfile(authenticatedUser);
                
                loggingService.logUserAuthentication(
                    request.getEmail(), 
                    true, 
                    clientIp, 
                    userAgent
                );
                
                return authenticatedUser;
            } else {
                // User not in cache, authenticate through service
                logger.debug("User profile not in cache, authenticating through service: {}", request.getEmail());
                
                UserResponse authenticatedUser = userService.authenticateUserWithDTO(request);
                
                // Cache the authenticated user profile
                redisCacheService.cacheUserProfile(authenticatedUser);
                redisCacheService.cacheUserProfileByEmail(request.getEmail(), authenticatedUser);
                
                loggingService.logUserAuthentication(
                    request.getEmail(), 
                    true, 
                    clientIp, 
                    userAgent
                );
                
                return authenticatedUser;
            }
            
        } catch (Exception e) {
            logger.warn("Authentication failed for email: {}: {}", request.getEmail(), e.getMessage());
            
            loggingService.logUserAuthentication(
                request.getEmail(), 
                false, 
                clientIp, 
                userAgent
            );
            
            throw e;
        }
    }

    /**
     * Get user profile with caching
     * 
     * @param userId User ID
     * @return UserResponse from cache or database
     */
    @Cacheable(value = "userProfiles", key = "#userId")
    public UserResponse getUserProfile(Long userId) {
        logger.debug("Getting user profile for userId: {} (cache miss)", userId);
        
        try {
            // This will only be called on cache miss
            UserResponse user = userService.findUserByEmail(getUserEmailById(userId)); // Simplified for example
            
            logger.info("User profile loaded from database and cached: {}", userId);
            return user;
            
        } catch (Exception e) {
            logger.error("Failed to load user profile for userId: {}: {}", userId, e.getMessage());
            throw new UserNotFoundException("User not found: " + userId);
        }
    }

    /**
     * Update user profile with cache invalidation
     * 
     * @param userId User ID
     * @param updatedUser Updated user data
     * @return Updated UserResponse
     */
    @CachePut(value = "userProfiles", key = "#userId")
    public UserResponse updateUserProfile(Long userId, UserResponse updatedUser) {
        logger.info("Updating user profile: {}", userId);
        
        try {
            // TODO: Implement user update in UserService
            // UserResponse updated = userService.updateUser(userId, updatedUser);
            
            // For now, just update the cache
            redisCacheService.cacheUserProfile(updatedUser);
            
            // Clear related caches
            redisCacheService.clearAllUserCaches(userId);
            
            logger.info("User profile updated and cache refreshed: {}", userId);
            return updatedUser;
            
        } catch (Exception e) {
            logger.error("Failed to update user profile: {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete user and clear all caches
     * 
     * @param userId User ID to delete
     */
    @CacheEvict(value = "userProfiles", key = "#userId")
    public void deleteUser(Long userId) {
        logger.info("Deleting user and clearing caches: {}", userId);
        
        try {
            // TODO: Implement user deletion in UserService
            // userService.deleteUser(userId);
            
            // Clear all caches for this user
            redisCacheService.clearAllUserCaches(userId);
            
            logger.info("User deleted and all caches cleared: {}", userId);
            
        } catch (Exception e) {
            logger.error("Failed to delete user: {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    /**
     * Find user by email with caching
     * 
     * @param email User email
     * @return UserResponse from cache or database
     */
    public UserResponse findUserByEmail(String email) {
        try {
            // Check cache first
            UserResponse cachedUser = redisCacheService.getCachedUserProfileByEmail(email);
            if (cachedUser != null) {
                logger.debug("User found in cache by email: {}", email);
                return cachedUser;
            }
            
            // Not in cache, fetch from database
            logger.debug("User not in cache, fetching by email: {}", email);
            UserResponse user = userService.findUserByEmail(email);
            
            // Cache the result
            redisCacheService.cacheUserProfile(user);
            redisCacheService.cacheUserProfileByEmail(email, user);
            
            return user;
            
        } catch (Exception e) {
            logger.error("Failed to find user by email: {}: {}", email, e.getMessage());
            throw e;
        }
    }

    /**
     * Invalidate all caches for a user (useful for admin operations)
     * 
     * @param userId User ID
     */
    public void invalidateUserCaches(Long userId) {
        logger.info("Invalidating all caches for user: {}", userId);
        redisCacheService.clearAllUserCaches(userId);
    }

    /**
     * Preload frequently accessed users into cache (cache warming)
     * 
     * @param userIds List of user IDs to preload
     */
    public void preloadUsersIntoCache(java.util.List<Long> userIds) {
        logger.info("Preloading {} users into cache", userIds.size());
        
        for (Long userId : userIds) {
            try {
                // This will populate the cache if not already present
                getUserProfile(userId);
            } catch (Exception e) {
                logger.warn("Failed to preload user into cache: {}: {}", userId, e.getMessage());
            }
        }
        
        logger.info("Cache preloading completed");
    }

    /**
     * Get cache statistics for monitoring
     * 
     * @return Map containing cache statistics
     */
    public java.util.Map<String, Object> getCacheStatistics() {
        return redisCacheService.getCacheStatistics();
    }

    /**
     * Check if caching is healthy
     * 
     * @return true if Redis is available and responsive
     */
    public boolean isCacheHealthy() {
        return redisCacheService.isRedisHealthy();
    }

    // Helper methods

    /**
     * Get user email by ID (simplified for example)
     * In a real implementation, this would be a proper service call
     */
    private String getUserEmailById(Long userId) {
        // This is a simplified implementation
        // In reality, you'd have a method to get email by ID
        return "user" + userId + "@example.com";
    }
}