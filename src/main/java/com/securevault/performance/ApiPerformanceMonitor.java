package com.securevault.performance;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * API Performance Monitor using AOP
 * 
 * Automatically tracks performance metrics for all API endpoints:
 * - Response times (min, max, average)
 * - Request counts
 * - Error rates
 * - Throughput metrics
 * - Database query counts
 * 
 * Uses Aspect-Oriented Programming to intercept controller methods
 * without modifying existing code.
 */
@Aspect
@Component
public class ApiPerformanceMonitor {

    private final ConcurrentHashMap<String, PerformanceMetrics> endpointMetrics = new ConcurrentHashMap<>();
    private final ThreadLocal<Long> queryCountTracker = new ThreadLocal<>();

    /**
     * Intercepts all controller methods to measure performance
     */
    @Around("execution(* com.securevault.controller.*.*(..))")
    public Object measureApiPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String endpoint = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        long startTime = System.nanoTime();
        
        // Initialize query count tracking
        queryCountTracker.set(0L);
        
        Object result;
        boolean success = true;
        String errorType = null;
        
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            success = false;
            errorType = e.getClass().getSimpleName();
            throw e;
        } finally {
            long endTime = System.nanoTime();
            long executionTime = endTime - startTime;
            long queryCount = queryCountTracker.get();
            
            // Update metrics
            updateMetrics(endpoint, executionTime, success, errorType, queryCount);
            
            // Clear thread local
            queryCountTracker.remove();
        }
        
        return result;
    }

    /**
     * Updates performance metrics for an endpoint
     */
    private void updateMetrics(String endpoint, long executionTimeNanos, boolean success, 
                              String errorType, long queryCount) {
        endpointMetrics.compute(endpoint, (key, metrics) -> {
            if (metrics == null) {
                metrics = new PerformanceMetrics(endpoint);
            }
            
            double executionTimeMs = executionTimeNanos / 1_000_000.0;
            
            // Update timing metrics
            metrics.totalRequests.incrementAndGet();
            metrics.totalExecutionTime.updateAndGet(current -> current + executionTimeMs);
            
            // Update min/max times
            updateMinTime(metrics.minExecutionTime, executionTimeMs);
            updateMaxTime(metrics.maxExecutionTime, executionTimeMs);
            
            // Update success/error metrics
            if (success) {
                metrics.successfulRequests.incrementAndGet();
            } else {
                metrics.failedRequests.incrementAndGet();
                metrics.lastErrorType.set(errorType);
            }
            
            // Update query metrics
            metrics.totalQueries.addAndGet(queryCount);
            
            // Update last access time
            metrics.lastAccessTime.set(System.currentTimeMillis());
            
            return metrics;
        });
    }

    /**
     * Updates minimum execution time atomically
     */
    private void updateMinTime(AtomicReference<Double> minTime, double currentTime) {
        minTime.updateAndGet(current -> {
            if (current == null || currentTime < current) {
                return currentTime;
            }
            return current;
        });
    }

    /**
     * Updates maximum execution time atomically
     */
    private void updateMaxTime(AtomicReference<Double> maxTime, double currentTime) {
        maxTime.updateAndGet(current -> {
            if (current == null || currentTime > current) {
                return currentTime;
            }
            return current;
        });
    }

    /**
     * Increments query count for current thread
     */
    public void incrementQueryCount() {
        Long current = queryCountTracker.get();
        if (current != null) {
            queryCountTracker.set(current + 1);
        }
    }

    /**
     * Gets performance metrics for a specific endpoint
     */
    public PerformanceMetrics getEndpointMetrics(String endpoint) {
        return endpointMetrics.get(endpoint);
    }

    /**
     * Gets performance metrics for all endpoints
     */
    public ConcurrentHashMap<String, PerformanceMetrics> getAllMetrics() {
        return new ConcurrentHashMap<>(endpointMetrics);
    }

    /**
     * Resets all performance metrics
     */
    public void resetMetrics() {
        endpointMetrics.clear();
    }

    /**
     * Gets performance summary for all endpoints
     */
    public PerformanceSummary getPerformanceSummary() {
        PerformanceSummary summary = new PerformanceSummary();
        
        for (PerformanceMetrics metrics : endpointMetrics.values()) {
            long totalRequests = metrics.totalRequests.get();
            if (totalRequests > 0) {
                double avgTime = metrics.totalExecutionTime.get() / totalRequests;
                double successRate = (metrics.successfulRequests.get() * 100.0) / totalRequests;
                double avgQueriesPerRequest = metrics.totalQueries.get() / (double) totalRequests;
                
                EndpointSummary endpointSummary = new EndpointSummary(
                    metrics.endpointName,
                    totalRequests,
                    avgTime,
                    metrics.minExecutionTime.get(),
                    metrics.maxExecutionTime.get(),
                    successRate,
                    avgQueriesPerRequest,
                    metrics.lastErrorType.get()
                );
                
                summary.addEndpointSummary(endpointSummary);
            }
        }
        
        return summary;
    }

    /**
     * Performance metrics for a single endpoint
     */
    public static class PerformanceMetrics {
        public final String endpointName;
        public final AtomicLong totalRequests = new AtomicLong(0);
        public final AtomicLong successfulRequests = new AtomicLong(0);
        public final AtomicLong failedRequests = new AtomicLong(0);
        public final AtomicReference<Double> totalExecutionTime = new AtomicReference<>(0.0);
        public final AtomicReference<Double> minExecutionTime = new AtomicReference<>(null);
        public final AtomicReference<Double> maxExecutionTime = new AtomicReference<>(null);
        public final AtomicLong totalQueries = new AtomicLong(0);
        public final AtomicReference<String> lastErrorType = new AtomicReference<>(null);
        public final AtomicLong lastAccessTime = new AtomicLong(System.currentTimeMillis());

        public PerformanceMetrics(String endpointName) {
            this.endpointName = endpointName;
        }

        public double getAverageExecutionTime() {
            long requests = totalRequests.get();
            return requests > 0 ? totalExecutionTime.get() / requests : 0.0;
        }

        public double getSuccessRate() {
            long requests = totalRequests.get();
            return requests > 0 ? (successfulRequests.get() * 100.0) / requests : 0.0;
        }

        public double getAverageQueriesPerRequest() {
            long requests = totalRequests.get();
            return requests > 0 ? totalQueries.get() / (double) requests : 0.0;
        }
    }

    /**
     * Summary of endpoint performance
     */
    public static class EndpointSummary {
        public final String endpointName;
        public final long totalRequests;
        public final double averageTimeMs;
        public final Double minTimeMs;
        public final Double maxTimeMs;
        public final double successRate;
        public final double averageQueriesPerRequest;
        public final String lastErrorType;

        public EndpointSummary(String endpointName, long totalRequests, double averageTimeMs,
                              Double minTimeMs, Double maxTimeMs, double successRate,
                              double averageQueriesPerRequest, String lastErrorType) {
            this.endpointName = endpointName;
            this.totalRequests = totalRequests;
            this.averageTimeMs = averageTimeMs;
            this.minTimeMs = minTimeMs;
            this.maxTimeMs = maxTimeMs;
            this.successRate = successRate;
            this.averageQueriesPerRequest = averageQueriesPerRequest;
            this.lastErrorType = lastErrorType;
        }
    }

    /**
     * Overall performance summary
     */
    public static class PerformanceSummary {
        private final java.util.List<EndpointSummary> endpointSummaries = new java.util.ArrayList<>();

        public void addEndpointSummary(EndpointSummary summary) {
            endpointSummaries.add(summary);
        }

        public java.util.List<EndpointSummary> getEndpointSummaries() {
            return new java.util.ArrayList<>(endpointSummaries);
        }

        public java.util.List<EndpointSummary> getSlowestEndpoints(int limit) {
            return endpointSummaries.stream()
                    .sorted((a, b) -> Double.compare(b.averageTimeMs, a.averageTimeMs))
                    .limit(limit)
                    .toList();
        }

        public java.util.List<EndpointSummary> getMostUsedEndpoints(int limit) {
            return endpointSummaries.stream()
                    .sorted((a, b) -> Long.compare(b.totalRequests, a.totalRequests))
                    .limit(limit)
                    .toList();
        }

        public double getOverallAverageResponseTime() {
            return endpointSummaries.stream()
                    .mapToDouble(s -> s.averageTimeMs)
                    .average()
                    .orElse(0.0);
        }

        public double getOverallSuccessRate() {
            double totalRequests = endpointSummaries.stream()
                    .mapToLong(s -> s.totalRequests)
                    .sum();
            
            if (totalRequests == 0) return 0.0;
            
            double totalSuccesses = endpointSummaries.stream()
                    .mapToDouble(s -> s.totalRequests * (s.successRate / 100.0))
                    .sum();
                    
            return (totalSuccesses / totalRequests) * 100.0;
        }
    }
}