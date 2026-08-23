package com.securevault.controller;

import com.securevault.dto.ApiResponse;
import com.securevault.dto.DashboardMetrics;
import com.securevault.service.AnalyticsDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AnalyticsDashboardController
 * 
 * REST API for analytics dashboard metrics.
 * Provides a single endpoint that returns all dashboard data in one response.
 */
@RestController
@RequestMapping("/api/dashboard")
public class AnalyticsDashboardController {
    
    private final AnalyticsDashboardService analyticsDashboardService;
    
    public AnalyticsDashboardController(AnalyticsDashboardService analyticsDashboardService) {
        this.analyticsDashboardService = analyticsDashboardService;
    }
    
    /**
     * GET /api/dashboard/metrics/{userId}
     * 
     * Get comprehensive dashboard metrics for a user.
     * Returns all metrics in a single response including:
     * - Total credentials, shared credentials, weak passwords
     * - Failed login count, security alerts, suspicious activities
     * - Recent user activity
     * - Password health score, security score
     * - Trends for credentials and logins
     * - Quick stats for today
     * 
     * @param userId User ID
     * @return Complete dashboard metrics
     */
    @GetMapping("/metrics/{userId}")
    public ResponseEntity<ApiResponse<DashboardMetrics>> getDashboardMetrics(
            @PathVariable Long userId) {
        
        DashboardMetrics metrics = analyticsDashboardService.getDashboardMetrics(userId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Dashboard metrics retrieved successfully", metrics)
        );
    }
    
    /**
     * GET /api/dashboard/metrics/system
     * 
     * Get system-wide dashboard metrics (admin view).
     * Returns aggregated metrics across all users including:
     * - Total system credentials and shares
     * - Total weak passwords
     * - Failed login count for the system
     * - Security alerts
     * - Total and active users
     * 
     * @return System-wide dashboard metrics
     */
    @GetMapping("/metrics/system")
    public ResponseEntity<ApiResponse<DashboardMetrics>> getSystemDashboardMetrics() {
        
        DashboardMetrics metrics = analyticsDashboardService.getSystemDashboardMetrics();
        
        return ResponseEntity.ok(
            ApiResponse.success("System dashboard metrics retrieved successfully", metrics)
        );
    }
}
