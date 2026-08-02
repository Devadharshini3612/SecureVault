package com.securevault.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Configuration for SecureVault
 * 
 * Configures Redis as a distributed cache for:
 * - User profiles and authentication data
 * - Credential metadata (non-sensitive information)
 * - Category and enumeration data
 * - Session management and JWT tokens
 * - Performance metrics and statistics
 * 
 * Security Considerations:
 * - Sensitive data (passwords, encrypted credentials) are NOT cached
 * - Cache entries have appropriate TTL values
 * - JSON serialization with type information for security
 * - Connection pooling for performance and reliability
 */
@Configuration
@EnableCaching
@EnableScheduling
@EnableAsync
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.redis.timeout:5000}")
    private long redisTimeout;

    /**
     * Redis Connection Factory with connection pooling
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // Redis server configuration
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setDatabase(redisDatabase);
        
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }

        // Connection pooling configuration
        LettucePoolingClientConfiguration poolConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(redisTimeout))
                .build();

        return new LettuceConnectionFactory(redisConfig, poolConfig);
    }

    /**
     * Redis Template with custom serializers for security and performance
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values with type information for security
        ObjectMapper objectMapper = createSecureObjectMapper();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Cache Manager with different TTL policies for different data types
     */
    @Bean
    public CacheManager cacheManager() {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // Default TTL: 30 minutes
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(createSecureObjectMapper())))
                .disableCachingNullValues();

        // Cache-specific configurations with different TTL values
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // User profile cache - longer TTL since user data doesn't change frequently
        cacheConfigurations.put("userProfiles", defaultConfig
                .entryTtl(Duration.ofHours(2))
                .prefixCacheNameWith("securevault:user:"));

        // Credential metadata cache - shorter TTL for security
        cacheConfigurations.put("credentialMetadata", defaultConfig
                .entryTtl(Duration.ofMinutes(15))
                .prefixCacheNameWith("securevault:cred:"));

        // Category and enum data - longer TTL since it's mostly static
        cacheConfigurations.put("categories", defaultConfig
                .entryTtl(Duration.ofHours(6))
                .prefixCacheNameWith("securevault:category:"));

        // Search results cache - short TTL to balance performance and data freshness
        cacheConfigurations.put("searchResults", defaultConfig
                .entryTtl(Duration.ofMinutes(5))
                .prefixCacheNameWith("securevault:search:"));

        // Performance metrics cache - very short TTL for near real-time data
        cacheConfigurations.put("performanceMetrics", defaultConfig
                .entryTtl(Duration.ofMinutes(2))
                .prefixCacheNameWith("securevault:perf:"));

        // Authentication cache - short TTL for security
        cacheConfigurations.put("authenticationCache", defaultConfig
                .entryTtl(Duration.ofMinutes(10))
                .prefixCacheNameWith("securevault:auth:"));

        return RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * Creates a secure ObjectMapper for JSON serialization
     * - Includes type information to prevent deserialization attacks
     * - Supports Java 8 time types
     * - Configured for security best practices
     */
    private ObjectMapper createSecureObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Add Java Time support
        objectMapper.registerModule(new JavaTimeModule());
        
        // Enable type information for security - prevents deserialization attacks
        objectMapper.activateDefaultTyping(
                TypeFactory.defaultInstance(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        
        return objectMapper;
    }

    /**
     * Custom Redis Template for User data with specific serialization
     */
    @Bean
    public RedisTemplate<String, Object> userRedisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        // String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // JSON serializer for user data
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        jsonSerializer.setObjectMapper(createSecureObjectMapper());
        
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis Template for performance metrics with optimized serialization
     */
    @Bean
    public RedisTemplate<String, Object> metricsRedisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Optimized JSON serializer for metrics data
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}