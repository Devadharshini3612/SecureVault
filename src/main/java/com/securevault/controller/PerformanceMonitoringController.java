package com.securevault.controller;

import com.securevault.dto.ApiResponse;
import com.securevault.performance.ApiBenchmarkService;
import com.securevault.performance.ApiPerformanceMonitor;
import com.securevault.service.DatabasePerformanceAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Performance Monitoring Controller
 * 
 * Provides endpoints for monitoring and analyzing API performance:
 * - Real-time performance metrics
 * - Benchmark execution
 * - Database performance analysis
 * - System health monitoring
 * 
 * Security: Only accessible by administrators or during development
 */
@RestController
@RequestMapping("/api/performance")
public class PerformanceMonitoringController {

    @Autowired
    private ApiPerformanceMonitor performanceMonitor;

    @Autowired
    private ApiBenchmarkService benchmarkService;

    @Autowired
    private DatabasePerformanceAnalysisService dbAnalysisService;

    /**
     * Get real-time performance metrics for all endpoints
     * 
     * GET /api/performance/metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<ApiPerformanceMonitor.PerformanceSummary>> getPerformanceMetrics() {
        ApiPerformanceMonitor.PerformanceSummary summary = performanceMonitor.getPerformanceSummary();
        
        return ResponseEntity.ok(
            ApiResponse.success("Performance metrics retrieved successfully", summary)
        );
    }

    /**
     * Get detailed metrics for a specific endpoint
     * 
     * GET /api/performance/metrics/{endpointName}
     */
    @GetMapping("/metrics/{endpointName}")
    public ResponseEntity<ApiResponse<ApiPerformanceMonitor.PerformanceMetrics>> getEndpointMetrics(
            @PathVariable String endpointName) {
        
        ApiPerformanceMonitor.PerformanceMetrics metrics = performanceMonitor.getEndpointMetrics(endpointName);
        
        if (metrics == null) {
            return ResponseEntity.ok(
                ApiResponse.error("No performance data found for endpoint: " + endpointName)
            );
        }
        
        return ResponseEntity.ok(
            ApiResponse.success("Endpoint metrics retrieved successfully", metrics)
        );
    }

    /**
     * Run comprehensive API benchmark tests
     * 
     * POST /api/performance/benchmark
     * 
     * Warning: This may take several minutes and will create test data
     */
    @PostMapping("/benchmark")
    public ResponseEntity<ApiResponse<ApiBenchmarkService.BenchmarkResults>> runBenchmark() {
        try {
            ApiBenchmarkService.BenchmarkResults results = benchmarkService.runComprehensiveBenchmark();
            
            return ResponseEntity.ok(
                ApiResponse.success("Benchmark completed successfully", results)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Benchmark failed: " + e.getMessage()));
        }
    }

    /**
     * Get database performance analysis
     * 
     * GET /api/performance/database?userId={userId}
     */
    @GetMapping("/database")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDatabasePerformanceAnalysis(
            @RequestParam(defaultValue = "1") Long userId) {
        
        try {
            Map<String, Object> analysis = dbAnalysisService.generatePerformanceReport(userId);
            
            return ResponseEntity.ok(
                ApiResponse.success("Database performance analysis completed", analysis)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Database analysis failed: " + e.getMessage()));
        }
    }

    /**
     * Get system health summary
     * 
     * GET /api/performance/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
        Map<String, Object> health = new java.util.HashMap<>();
        
        // Performance summary
        ApiPerformanceMonitor.PerformanceSummary perfSummary = performanceMonitor.getPerformanceSummary();
        health.put("averageResponseTime", perfSummary.getOverallAverageResponseTime());
        health.put("successRate", perfSummary.getOverallSuccessRate());
        
        // System metrics
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        health.put("memoryUsage", Map.of(
            "used", usedMemory / (1024 * 1024), // MB
            "free", freeMemory / (1024 * 1024), // MB
            "total", totalMemory / (1024 * 1024), // MB
            "max", maxMemory / (1024 * 1024), // MB
            "usagePercentage", (usedMemory * 100.0) / maxMemory
        ));
        
        // System status assessment
        String status = assessSystemStatus(perfSummary, usedMemory, maxMemory);
        health.put("overallStatus", status);
        
        // Recommendations
        health.put("recommendations", generateHealthRecommendations(perfSummary, usedMemory, maxMemory));
        
        return ResponseEntity.ok(
            ApiResponse.success("System health retrieved successfully", health)
        );
    }

    /**
     * Reset performance metrics (useful for testing)
     * 
     * DELETE /api/performance/metrics
     */
    @DeleteMapping("/metrics")
    public ResponseEntity<ApiResponse<String>> resetMetrics() {
        performanceMonitor.resetMetrics();
        
        return ResponseEntity.ok(
            ApiResponse.success("Performance metrics reset successfully", "All metrics cleared")
        );
    }

    /**
     * Get top slowest endpoints
     * 
     * GET /api/performance/slowest?limit=5
     */
    @GetMapping("/slowest")
    public ResponseEntity<ApiResponse<java.util.List<ApiPerformanceMonitor.EndpointSummary>>> getSlowestEndpoints(
            @RequestParam(defaultValue = "5") int limit) {
        
        ApiPerformanceMonitor.PerformanceSummary summary = performanceMonitor.getPerformanceSummary();
        java.util.List<ApiPerformanceMonitor.EndpointSummary> slowest = summary.getSlowestEndpoints(limit);
        
        return ResponseEntity.ok(
            ApiResponse.success("Slowest endpoints retrieved successfully", slowest)
        );
    }

    /**
     * Get most frequently used endpoints
     * 
     * GET /api/performance/most-used?limit=5
     */
    @GetMapping("/most-used")
    public ResponseEntity<ApiResponse<java.util.List<ApiPerformanceMonitor.EndpointSummary>>> getMostUsedEndpoints(
            @RequestParam(defaultValue = "5") int limit) {
        
        ApiPerformanceMonitor.PerformanceSummary summary = performanceMonitor.getPerformanceSummary();
        java.util.List<ApiPerformanceMonitor.EndpointSummary> mostUsed = summary.getMostUsedEndpoints(limit);
        
        return ResponseEntity.ok(
            ApiResponse.success("Most used endpoints retrieved successfully", mostUsed)
        );
    }

    // Helper methods

    /**
     * Assesses overall system status based on performance and memory metrics
     */
    private String assessSystemStatus(ApiPerformanceMonitor.PerformanceSummary perfSummary, long usedMemory, long maxMemory) {
        double avgResponseTime = perfSummary.getOverallAverageResponseTime();
        double successRate = perfSummary.getOverallSuccessRate();
        double memoryUsage = (usedMemory * 100.0) / maxMemory;
        
        if (avgResponseTime > 1000 || successRate < 90 || memoryUsage > 90) {
            return "CRITICAL";
        } else if (avgResponseTime > 500 || successRate < 95 || memoryUsage > 75) {
            return "WARNING";
        } else if (avgResponseTime > 200 || successRate < 98 || memoryUsage > 60) {
            return "FAIR";
        } else {
            return "HEALTHY";
        }
    }

    /**
     * Generates health recommendations based on system metrics
     */
    private java.util.List<String> generateHealthRecommendations(ApiPerformanceMonitor.PerformanceSummary perfSummary, 
                                                                long usedMemory, long maxMemory) {
        java.util.List<String> recommendations = new java.util.ArrayList<>();
        
        double avgResponseTime = perfSummary.getOverallAverageResponseTime();
        double successRate = perfSummary.getOverallSuccessRate();
        double memoryUsage = (usedMemory * 100.0) / maxMemory;
        
        if (avgResponseTime > 500) {
            recommendations.add("Response times are elevated. Consider database query optimization.");
        }
        
        if (successRate < 95) {
            recommendations.add("Success rate is below acceptable threshold. Investigate error patterns.");
        }
        
        if (memoryUsage > 75) {
            recommendations.add("Memory usage is high. Consider increasing heap size or optimizing memory usage.");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("System is performing well. Continue monitoring for optimal performance.");
        }
        
        return recommendations;
    }
}