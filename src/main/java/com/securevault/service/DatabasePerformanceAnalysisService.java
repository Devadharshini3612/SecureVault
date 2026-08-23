package com.securevault.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Database Performance Analysis Service
 * 
 * This service provides tools to analyze and monitor database performance,
 * specifically focusing on credential-related queries and their optimization.
 * 
 * Key features:
 * - Query execution time analysis
 * - Index usage verification
 * - N+1 query detection
 * - Performance bottleneck identification
 * - Database optimization recommendations
 */
@Service
public class DatabasePerformanceAnalysisService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor injection - Spring automatically injects dependencies
     */
    public DatabasePerformanceAnalysisService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Analyzes the current database index usage for credential queries
     * 
     * @return Map containing index usage statistics
     */
    public Map<String, Object> analyzeIndexUsage() {
        String query = """
            SELECT 
                schemaname,
                tablename,
                indexname,
                idx_tup_read,
                idx_tup_fetch,
                CASE 
                    WHEN idx_tup_read > 0 THEN (idx_tup_fetch::float / idx_tup_read::float) * 100
                    ELSE 0 
                END as efficiency_percentage
            FROM pg_stat_user_indexes 
            WHERE schemaname = 'public' 
            AND tablename IN ('credentials', 'users', 'credential_shares')
            ORDER BY idx_tup_read DESC
            """;
            
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
        
        return Map.of(
            "indexStats", results,
            "analysis", analyzeIndexEfficiency(results),
            "recommendations", generateIndexRecommendations(results)
        );
    }

    /**
     * Measures query performance for different credential operations
     * 
     * @param userId Test user ID for performance testing
     * @return Performance metrics for various operations
     */
    public Map<String, Object> measureQueryPerformance(Long userId) {
        Map<String, Object> results = new java.util.HashMap<>();
        
        // Test 1: Basic credential listing performance
        long startTime = System.nanoTime();
        String listQuery = "SELECT * FROM credentials WHERE user_id = ? AND is_deleted = false ORDER BY updated_at DESC";
        List<Map<String, Object>> credentials = jdbcTemplate.queryForList(listQuery, userId);
        long listTime = System.nanoTime() - startTime;
        
        results.put("listCredentials", Map.of(
            "executionTimeMs", listTime / 1_000_000.0,
            "recordCount", credentials.size(),
            "avgTimePerRecord", credentials.size() > 0 ? (listTime / 1_000_000.0) / credentials.size() : 0
        ));
        
        // Test 2: Search query performance
        startTime = System.nanoTime();
        String searchQuery = """
            SELECT * FROM credentials 
            WHERE user_id = ? AND is_deleted = false 
            AND (LOWER(service_name) LIKE LOWER('%gmail%') OR LOWER(username) LIKE LOWER('%gmail%'))
            ORDER BY updated_at DESC
            """;
        List<Map<String, Object>> searchResults = jdbcTemplate.queryForList(searchQuery, userId);
        long searchTime = System.nanoTime() - startTime;
        
        results.put("searchCredentials", Map.of(
            "executionTimeMs", searchTime / 1_000_000.0,
            "recordCount", searchResults.size(),
            "searchTerm", "gmail"
        ));
        
        // Test 3: Category filtering performance
        startTime = System.nanoTime();
        String categoryQuery = """
            SELECT * FROM credentials 
            WHERE user_id = ? AND is_deleted = false AND category = 'PERSONAL'
            ORDER BY service_name ASC
            """;
        List<Map<String, Object>> categoryResults = jdbcTemplate.queryForList(categoryQuery, userId);
        long categoryTime = System.nanoTime() - startTime;
        
        results.put("categoryFilter", Map.of(
            "executionTimeMs", categoryTime / 1_000_000.0,
            "recordCount", categoryResults.size(),
            "category", "PERSONAL"
        ));
        
        // Test 4: Count query performance
        startTime = System.nanoTime();
        String countQuery = "SELECT COUNT(*) FROM credentials WHERE user_id = ? AND is_deleted = false";
        Integer totalCount = jdbcTemplate.queryForObject(countQuery, Integer.class, userId);
        long countTime = System.nanoTime() - startTime;
        
        results.put("countCredentials", Map.of(
            "executionTimeMs", countTime / 1_000_000.0,
            "totalCount", totalCount
        ));
        
        return results;
    }

    /**
     * Detects potential N+1 query problems in the application
     * 
     * @return Analysis of potential N+1 query issues
     */
    public Map<String, Object> detectN1QueryProblems() {
        Map<String, Object> analysis = new java.util.HashMap<>();
        
        // Analyze query patterns from pg_stat_statements if available
        try {
            String queryStatsQuery = """
                SELECT query, calls, total_time, mean_time, rows
                FROM pg_stat_statements 
                WHERE query LIKE '%credentials%' 
                OR query LIKE '%users%'
                ORDER BY calls DESC, total_time DESC
                LIMIT 20
                """;
            
            List<Map<String, Object>> queryStats = jdbcTemplate.queryForList(queryStatsQuery);
            analysis.put("frequentQueries", queryStats);
            
            // Identify potential N+1 patterns
            List<String> n1Indicators = queryStats.stream()
                .map(stat -> (String) stat.get("query"))
                .filter(query -> {
                    // Look for single-row queries called many times
                    return query.contains("WHERE credential_id = ?") || 
                           query.contains("WHERE user_id = ?") && !query.contains("ORDER BY");
                })
                .toList();
            
            analysis.put("potentialN1Queries", n1Indicators);
            analysis.put("n1Risk", n1Indicators.size() > 0 ? "HIGH" : "LOW");
            
        } catch (Exception e) {
            analysis.put("error", "pg_stat_statements extension not available");
            analysis.put("recommendation", "Enable pg_stat_statements for detailed query analysis");
        }
        
        return analysis;
    }

    /**
     * Generates a comprehensive performance report
     * 
     * @param userId Test user ID for performance testing
     * @return Complete performance analysis report
     */
    public Map<String, Object> generatePerformanceReport(Long userId) {
        Map<String, Object> report = new java.util.HashMap<>();
        
        report.put("timestamp", java.time.LocalDateTime.now());
        report.put("userId", userId);
        
        // Database connection info
        try {
            String dbInfo = jdbcTemplate.queryForObject(
                "SELECT version()", String.class);
            report.put("databaseVersion", dbInfo);
        } catch (Exception e) {
            report.put("databaseVersion", "Unknown");
        }
        
        // Index analysis
        report.put("indexAnalysis", analyzeIndexUsage());
        
        // Query performance
        report.put("queryPerformance", measureQueryPerformance(userId));
        
        // N+1 detection
        report.put("n1Analysis", detectN1QueryProblems());
        
        // Table statistics
        report.put("tableStats", getTableStatistics());
        
        // Overall assessment
        report.put("overallAssessment", generateOverallAssessment(report));
        
        return report;
    }

    /**
     * Gets basic statistics about database tables
     */
    private Map<String, Object> getTableStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        try {
            // Get table sizes and row counts
            String tableStatsQuery = """
                SELECT 
                    tablename,
                    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
                    n_tup_ins as inserts,
                    n_tup_upd as updates,
                    n_tup_del as deletes,
                    n_live_tup as live_rows,
                    n_dead_tup as dead_rows
                FROM pg_stat_user_tables 
                WHERE schemaname = 'public'
                AND tablename IN ('credentials', 'users', 'credential_shares')
                """;
            
            List<Map<String, Object>> tableStats = jdbcTemplate.queryForList(tableStatsQuery);
            stats.put("tables", tableStats);
            
        } catch (Exception e) {
            stats.put("error", "Unable to retrieve table statistics: " + e.getMessage());
        }
        
        return stats;
    }

    /**
     * Analyzes index efficiency from usage statistics
     */
    private List<String> analyzeIndexEfficiency(List<Map<String, Object>> indexStats) {
        List<String> insights = new java.util.ArrayList<>();
        
        for (Map<String, Object> stat : indexStats) {
            String indexName = (String) stat.get("indexname");
            Long tupRead = (Long) stat.get("idx_tup_read");
            Double efficiency = (Double) stat.get("efficiency_percentage");
            
            if (tupRead == 0) {
                insights.add("Index '" + indexName + "' is not being used - consider removing");
            } else if (efficiency < 50) {
                insights.add("Index '" + indexName + "' has low efficiency (" + 
                           String.format("%.1f", efficiency) + "%) - investigate query patterns");
            } else if (efficiency > 90) {
                insights.add("Index '" + indexName + "' is highly efficient (" + 
                           String.format("%.1f", efficiency) + "%) - performing well");
            }
        }
        
        return insights;
    }

    /**
     * Generates recommendations based on index analysis
     */
    private List<String> generateIndexRecommendations(List<Map<String, Object>> indexStats) {
        List<String> recommendations = new java.util.ArrayList<>();
        
        // Check if critical indexes exist
        boolean hasUserActiveIndex = indexStats.stream()
            .anyMatch(stat -> "idx_credentials_user_active".equals(stat.get("indexname")));
            
        if (!hasUserActiveIndex) {
            recommendations.add("Create composite index on (user_id, is_deleted, updated_at) for credential listing");
        }
        
        // Add general recommendations
        recommendations.add("Monitor index usage regularly using pg_stat_user_indexes");
        recommendations.add("Consider partial indexes for frequently filtered columns");
        recommendations.add("Review and remove unused indexes to improve write performance");
        
        return recommendations;
    }

    /**
     * Generates an overall assessment of database performance
     */
    private Map<String, Object> generateOverallAssessment(Map<String, Object> report) {
        Map<String, Object> assessment = new java.util.HashMap<>();
        
        // Analyze query performance
        @SuppressWarnings("unchecked")
        Map<String, Object> queryPerf = (Map<String, Object>) report.get("queryPerformance");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> listPerf = (Map<String, Object>) queryPerf.get("listCredentials");
        Double listTime = (Double) listPerf.get("executionTimeMs");
        
        // Performance assessment
        String performanceRating;
        if (listTime < 10) {
            performanceRating = "EXCELLENT";
        } else if (listTime < 50) {
            performanceRating = "GOOD";
        } else if (listTime < 100) {
            performanceRating = "FAIR";
        } else {
            performanceRating = "POOR";
        }
        
        assessment.put("performanceRating", performanceRating);
        assessment.put("primaryMetric", "Credential listing time: " + String.format("%.2f", listTime) + "ms");
        
        // Recommendations based on performance
        List<String> recommendations = new java.util.ArrayList<>();
        if (listTime > 50) {
            recommendations.add("Consider optimizing credential listing queries");
            recommendations.add("Verify database indexes are properly utilized");
        }
        if (listTime > 100) {
            recommendations.add("Urgent: Database performance needs immediate attention");
            recommendations.add("Consider query optimization and server resources");
        }
        
        assessment.put("recommendations", recommendations);
        
        return assessment;
    }
}