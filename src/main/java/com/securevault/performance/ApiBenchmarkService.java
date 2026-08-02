package com.securevault.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securevault.dto.*;
import com.securevault.enums.Category;
import com.securevault.service.CredentialService;
import com.securevault.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API Benchmark Service
 * 
 * Provides comprehensive performance testing for key API endpoints:
 * - Login performance under load
 * - Credential creation with encryption overhead
 * - Search operations with database queries
 * - Credential listing with large datasets
 * - Concurrent access patterns
 * 
 * Generates detailed performance reports with recommendations.
 */
@Service
public class ApiBenchmarkService {

    @Autowired
    private UserService userService;

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private ObjectMapper objectMapper;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Runs comprehensive benchmark tests for all key endpoints
     * 
     * @return Detailed benchmark results
     */
    public BenchmarkResults runComprehensiveBenchmark() {
        BenchmarkResults results = new BenchmarkResults();
        
        // Setup test data
        TestDataSetup testData = setupTestData();
        
        // Benchmark each endpoint
        results.loginBenchmark = benchmarkLoginEndpoint(testData);
        results.credentialCreationBenchmark = benchmarkCredentialCreation(testData);
        results.credentialListingBenchmark = benchmarkCredentialListing(testData);
        results.credentialSearchBenchmark = benchmarkCredentialSearch(testData);
        results.concurrencyBenchmark = benchmarkConcurrentAccess(testData);
        
        // Generate recommendations
        results.recommendations = generatePerformanceRecommendations(results);
        
        return results;
    }

    /**
     * Benchmarks the login endpoint performance
     */
    private EndpointBenchmark benchmarkLoginEndpoint(TestDataSetup testData) {
        return benchmarkEndpoint("Login API", () -> {
            try {
                // Simulate login operation
                String result = userService.loginUser(testData.testEmail, testData.testPassword);
                return "SUCCESS".equals(result);
            } catch (Exception e) {
                return false;
            }
        }, 100, 5);
    }

    /**
     * Benchmarks credential creation with encryption overhead
     */
    private EndpointBenchmark benchmarkCredentialCreation(TestDataSetup testData) {
        return benchmarkEndpoint("Create Credential API", () -> {
            try {
                CreateCredentialRequest request = new CreateCredentialRequest();
                request.setUserId(testData.testUserId);
                request.setServiceName("BenchmarkService" + System.nanoTime());
                request.setUsername("benchmarkuser");
                request.setPassword("benchmarkpassword123!");
                request.setCategory(Category.PERSONAL);
                
                CredentialResponse response = credentialService.createCredentialWithResponse(request);
                return response != null;
            } catch (Exception e) {
                return false;
            }
        }, 50, 3);
    }

    /**
     * Benchmarks credential listing performance with varying dataset sizes
     */
    private EndpointBenchmark benchmarkCredentialListing(TestDataSetup testData) {
        // Create test credentials for listing benchmark
        createTestCredentials(testData.testUserId, 100);
        
        return benchmarkEndpoint("List Credentials API", () -> {
            try {
                List<CredentialResponse> credentials = credentialService.listCredentials(testData.testUserId);
                return credentials != null;
            } catch (Exception e) {
                return false;
            }
        }, 50, 3);
    }

    /**
     * Benchmarks search functionality with different query patterns
     */
    private EndpointBenchmark benchmarkCredentialSearch(TestDataSetup testData) {
        return benchmarkEndpoint("Search Credentials API", () -> {
            try {
                List<CredentialResponse> results = credentialService.searchCredentials(testData.testUserId, "gmail");
                return results != null;
            } catch (Exception e) {
                return false;
            }
        }, 30, 3);
    }

    /**
     * Benchmarks concurrent access patterns
     */
    private ConcurrencyBenchmark benchmarkConcurrentAccess(TestDataSetup testData) {
        int threadCount = 10;
        int operationsPerThread = 20;
        
        List<Future<List<Long>>> futures = new ArrayList<>();
        
        for (int i = 0; i < threadCount; i++) {
            Future<List<Long>> future = executorService.submit(() -> {
                List<Long> executionTimes = new ArrayList<>();
                
                for (int j = 0; j < operationsPerThread; j++) {
                    long startTime = System.nanoTime();
                    
                    try {
                        // Mix of operations
                        if (j % 3 == 0) {
                            userService.loginUser(testData.testEmail, testData.testPassword);
                        } else if (j % 3 == 1) {
                            credentialService.listCredentials(testData.testUserId);
                        } else {
                            credentialService.searchCredentials(testData.testUserId, "test");
                        }
                    } catch (Exception e) {
                        // Record failures
                    }
                    
                    long endTime = System.nanoTime();
                    executionTimes.add((endTime - startTime) / 1_000_000); // Convert to ms
                }
                
                return executionTimes;
            });
            
            futures.add(future);
        }
        
        // Collect results
        List<Long> allExecutionTimes = new ArrayList<>();
        int completedThreads = 0;
        
        for (Future<List<Long>> future : futures) {
            try {
                List<Long> times = future.get(30, TimeUnit.SECONDS);
                allExecutionTimes.addAll(times);
                completedThreads++;
            } catch (Exception e) {
                // Handle timeout or execution errors
            }
        }
        
        return new ConcurrencyBenchmark(
            threadCount,
            operationsPerThread,
            completedThreads,
            allExecutionTimes
        );
    }

    /**
     * Generic endpoint benchmarking method
     */
    private EndpointBenchmark benchmarkEndpoint(String endpointName, BenchmarkOperation operation, 
                                               int iterations, int warmupRounds) {
        // Warmup
        for (int i = 0; i < warmupRounds; i++) {
            try {
                operation.execute();
            } catch (Exception e) {
                // Ignore warmup failures
            }
        }
        
        // Actual benchmark
        List<Long> executionTimes = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        
        long totalStartTime = System.nanoTime();
        
        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            boolean success = false;
            
            try {
                success = operation.execute();
            } catch (Exception e) {
                success = false;
            }
            
            long endTime = System.nanoTime();
            long executionTime = (endTime - startTime) / 1_000_000; // Convert to ms
            
            executionTimes.add(executionTime);
            
            if (success) {
                successCount++;
            } else {
                failureCount++;
            }
        }
        
        long totalEndTime = System.nanoTime();
        long totalTime = (totalEndTime - totalStartTime) / 1_000_000;
        
        return new EndpointBenchmark(
            endpointName,
            iterations,
            successCount,
            failureCount,
            executionTimes,
            totalTime
        );
    }

    /**
     * Creates test credentials for benchmarking
     */
    @Transactional
    private void createTestCredentials(Long userId, int count) {
        for (int i = 0; i < count; i++) {
            try {
                CreateCredentialRequest request = new CreateCredentialRequest();
                request.setUserId(userId);
                request.setServiceName("TestService" + i);
                request.setUsername("testuser" + i);
                request.setPassword("testpass" + i);
                request.setCategory(i % 2 == 0 ? Category.PERSONAL : Category.WORK);
                
                credentialService.createCredentialWithResponse(request);
            } catch (Exception e) {
                // Continue creating other credentials
            }
        }
    }

    /**
     * Sets up test data for benchmarking
     */
    private TestDataSetup setupTestData() {
        try {
            // Create test user
            RegisterRequest registerRequest = new RegisterRequest(
                "Benchmark User", 
                "benchmark@test.com", 
                "benchmarkpass123"
            );
            
            // Use the enhanced DTO service method
            UserResponse userResponse = userService.registerUserWithDTO(registerRequest);
            
            return new TestDataSetup(
                userResponse.getUserId(),
                registerRequest.getEmail(),
                registerRequest.getPassword()
            );
        } catch (Exception e) {
            // Fallback to existing user or handle error
            return new TestDataSetup(1L, "benchmark@test.com", "benchmarkpass123");
        }
    }

    /**
     * Generates performance recommendations based on benchmark results
     */
    private List<String> generatePerformanceRecommendations(BenchmarkResults results) {
        List<String> recommendations = new ArrayList<>();
        
        // Login performance analysis
        if (results.loginBenchmark.getAverageExecutionTime() > 500) {
            recommendations.add("Login performance is slow (>" + results.loginBenchmark.getAverageExecutionTime() + "ms). Consider optimizing BCrypt rounds or database queries.");
        }
        
        // Credential creation analysis
        if (results.credentialCreationBenchmark.getAverageExecutionTime() > 200) {
            recommendations.add("Credential creation is slow due to encryption overhead. Consider async processing for non-critical operations.");
        }
        
        // Listing performance analysis
        if (results.credentialListingBenchmark.getAverageExecutionTime() > 100) {
            recommendations.add("Credential listing is slow. Verify database indexes and consider pagination for large datasets.");
        }
        
        // Search performance analysis
        if (results.credentialSearchBenchmark.getAverageExecutionTime() > 150) {
            recommendations.add("Search performance needs improvement. Consider full-text search indexes or search engine integration.");
        }
        
        // Concurrency analysis
        double concurrentAvg = results.concurrencyBenchmark.getAverageExecutionTime();
        double singleThreadAvg = (results.loginBenchmark.getAverageExecutionTime() + 
                                 results.credentialListingBenchmark.getAverageExecutionTime()) / 2.0;
        
        if (concurrentAvg > singleThreadAvg * 2) {
            recommendations.add("System shows significant performance degradation under concurrent load. Consider connection pooling and resource optimization.");
        }
        
        // Success rate analysis
        if (results.getOverallSuccessRate() < 95.0) {
            recommendations.add("System reliability is below acceptable threshold (" + String.format("%.1f", results.getOverallSuccessRate()) + "%). Investigate error patterns and implement retry mechanisms.");
        }
        
        return recommendations;
    }

    // Functional interface for benchmark operations
    @FunctionalInterface
    private interface BenchmarkOperation {
        boolean execute() throws Exception;
    }

    // Data classes for benchmark results
    public static class BenchmarkResults {
        public EndpointBenchmark loginBenchmark;
        public EndpointBenchmark credentialCreationBenchmark;
        public EndpointBenchmark credentialListingBenchmark;
        public EndpointBenchmark credentialSearchBenchmark;
        public ConcurrencyBenchmark concurrencyBenchmark;
        public List<String> recommendations;
        public long timestamp = System.currentTimeMillis();

        public double getOverallSuccessRate() {
            double totalOperations = loginBenchmark.successCount + loginBenchmark.failureCount +
                                   credentialCreationBenchmark.successCount + credentialCreationBenchmark.failureCount +
                                   credentialListingBenchmark.successCount + credentialListingBenchmark.failureCount +
                                   credentialSearchBenchmark.successCount + credentialSearchBenchmark.failureCount;
                                   
            double totalSuccesses = loginBenchmark.successCount + 
                                  credentialCreationBenchmark.successCount +
                                  credentialListingBenchmark.successCount +
                                  credentialSearchBenchmark.successCount;
                                  
            return totalOperations > 0 ? (totalSuccesses / totalOperations) * 100.0 : 0.0;
        }
    }

    public static class EndpointBenchmark {
        public final String endpointName;
        public final int totalIterations;
        public final int successCount;
        public final int failureCount;
        public final List<Long> executionTimes;
        public final long totalBenchmarkTime;

        public EndpointBenchmark(String endpointName, int totalIterations, int successCount, 
                                int failureCount, List<Long> executionTimes, long totalBenchmarkTime) {
            this.endpointName = endpointName;
            this.totalIterations = totalIterations;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.executionTimes = new ArrayList<>(executionTimes);
            this.totalBenchmarkTime = totalBenchmarkTime;
        }

        public double getAverageExecutionTime() {
            return executionTimes.isEmpty() ? 0.0 : 
                   executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        }

        public long getMinExecutionTime() {
            return executionTimes.isEmpty() ? 0 : 
                   executionTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        }

        public long getMaxExecutionTime() {
            return executionTimes.isEmpty() ? 0 : 
                   executionTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        }

        public double getSuccessRate() {
            return totalIterations > 0 ? (successCount * 100.0) / totalIterations : 0.0;
        }

        public double getThroughput() {
            return totalBenchmarkTime > 0 ? (totalIterations * 1000.0) / totalBenchmarkTime : 0.0; // ops/sec
        }
    }

    public static class ConcurrencyBenchmark {
        public final int threadCount;
        public final int operationsPerThread;
        public final int completedThreads;
        public final List<Long> allExecutionTimes;

        public ConcurrencyBenchmark(int threadCount, int operationsPerThread, 
                                   int completedThreads, List<Long> allExecutionTimes) {
            this.threadCount = threadCount;
            this.operationsPerThread = operationsPerThread;
            this.completedThreads = completedThreads;
            this.allExecutionTimes = new ArrayList<>(allExecutionTimes);
        }

        public double getAverageExecutionTime() {
            return allExecutionTimes.isEmpty() ? 0.0 :
                   allExecutionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        }

        public double getSuccessRate() {
            int expectedOperations = threadCount * operationsPerThread;
            return expectedOperations > 0 ? (allExecutionTimes.size() * 100.0) / expectedOperations : 0.0;
        }
    }

    private static class TestDataSetup {
        public final Long testUserId;
        public final String testEmail;
        public final String testPassword;

        public TestDataSetup(Long testUserId, String testEmail, String testPassword) {
            this.testUserId = testUserId;
            this.testEmail = testEmail;
            this.testPassword = testPassword;
        }
    }
}