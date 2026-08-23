package com.securevault.config;

import com.securevault.service.ProductionLoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.io.File;
import java.util.Arrays;

/**
 * Logging Configuration Class
 * 
 * Configures and initializes logging infrastructure for the SecureVault application.
 * This class ensures proper setup of log directories, validates logging configuration,
 * and provides startup logging information.
 * 
 * Features:
 * - Automatic log directory creation
 * - Logging configuration validation
 * - Startup logging information
 * - Profile-specific logging setup
 * - Integration with ProductionLoggingService
 */
@Configuration
public class LoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);

    @Autowired
    private Environment environment;

    @Autowired
    private ProductionLoggingService productionLoggingService;

    /**
     * Initialize logging configuration when application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeLogging() {
        // Log application startup
        productionLoggingService.logApplicationStartup(
            "LoggingConfig", 
            "Initializing logging infrastructure"
        );

        // Create log directories
        createLogDirectories();

        // Log active profiles
        logActiveProfiles();

        // Log logging configuration
        logLoggingConfiguration();

        // Log system information
        logSystemInformation();

        logger.info("Logging infrastructure initialized successfully");
    }

    /**
     * Creates necessary log directories if they don't exist
     */
    private void createLogDirectories() {
        String logDir = environment.getProperty("LOG_DIR", "./logs");
        
        try {
            // Create main log directory
            File logDirectory = new File(logDir);
            if (!logDirectory.exists()) {
                boolean created = logDirectory.mkdirs();
                if (created) {
                    logger.info("Created log directory: {}", logDirectory.getAbsolutePath());
                } else {
                    logger.warn("Failed to create log directory: {}", logDirectory.getAbsolutePath());
                }
            }

            // Create archive directory for rolled logs
            File archiveDirectory = new File(logDir, "archive");
            if (!archiveDirectory.exists()) {
                boolean created = archiveDirectory.mkdirs();
                if (created) {
                    logger.info("Created log archive directory: {}", archiveDirectory.getAbsolutePath());
                }
            }

            // Log directory information
            productionLoggingService.logSystemHealth(
                "log_directory_space",
                logDirectory.getFreeSpace() / (1024 * 1024), // MB
                logDirectory.getFreeSpace() > 1024 * 1024 * 1024 ? "HEALTHY" : "WARNING", // 1GB threshold
                "1GB"
            );

        } catch (Exception e) {
            logger.error("Failed to create log directories: {}", e.getMessage(), e);
        }
    }

    /**
     * Logs active Spring profiles
     */
    private void logActiveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();

        if (activeProfiles.length > 0) {
            logger.info("Active profiles: {}", Arrays.toString(activeProfiles));
            productionLoggingService.logApplicationStartup(
                "ProfileConfig", 
                "Active profiles: " + Arrays.toString(activeProfiles)
            );
        } else {
            logger.info("No active profiles set, using default profiles: {}", Arrays.toString(defaultProfiles));
            productionLoggingService.logApplicationStartup(
                "ProfileConfig", 
                "Using default profiles: " + Arrays.toString(defaultProfiles)
            );
        }
    }

    /**
     * Logs current logging configuration settings
     */
    private void logLoggingConfiguration() {
        try {
            String logLevel = environment.getProperty("logging.level.com.securevault", "INFO");
            String logDir = environment.getProperty("logging.file.path", "./logs");
            String rootLogLevel = environment.getProperty("logging.level.root", "INFO");

            logger.info("Logging Configuration:");
            logger.info("  Root Level: {}", rootLogLevel);
            logger.info("  SecureVault Level: {}", logLevel);
            logger.info("  Log Directory: {}", logDir);
            logger.info("  SQL Logging: {}", environment.getProperty("logging.level.org.hibernate.SQL", "INFO"));

            productionLoggingService.logApplicationStartup(
                "LoggingConfiguration",
                String.format("Root=%s, App=%s, Directory=%s", rootLogLevel, logLevel, logDir)
            );

        } catch (Exception e) {
            logger.warn("Failed to retrieve logging configuration: {}", e.getMessage());
        }
    }

    /**
     * Logs system information relevant to logging
     */
    private void logSystemInformation() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long maxMemory = runtime.maxMemory();

            logger.info("System Information:");
            logger.info("  Java Version: {}", System.getProperty("java.version"));
            logger.info("  OS: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
            logger.info("  Memory - Total: {}MB, Free: {}MB, Max: {}MB", 
                       totalMemory / (1024 * 1024), 
                       freeMemory / (1024 * 1024), 
                       maxMemory / (1024 * 1024));
            logger.info("  Available Processors: {}", runtime.availableProcessors());
            logger.info("  User Directory: {}", System.getProperty("user.dir"));

            productionLoggingService.logSystemHealth(
                "memory_usage",
                (totalMemory - freeMemory) / (1024 * 1024), // Used memory in MB
                ((totalMemory - freeMemory) * 100.0 / maxMemory) > 80 ? "WARNING" : "HEALTHY",
                "80%"
            );

        } catch (Exception e) {
            logger.warn("Failed to retrieve system information: {}", e.getMessage());
        }
    }

    /**
     * Validates logging configuration and reports any issues
     */
    public void validateLoggingConfiguration() {
        try {
            logger.debug("Validating logging configuration...");

            // Test each log level
            logger.trace("TRACE level logging test");
            logger.debug("DEBUG level logging test");
            logger.info("INFO level logging test");
            logger.warn("WARN level logging test");
            logger.error("ERROR level logging test - THIS IS A TEST");

            // Test structured logging
            productionLoggingService.logApplicationStartup("ValidationTest", "Testing structured logging");

            logger.info("Logging configuration validation completed successfully");

        } catch (Exception e) {
            logger.error("Logging configuration validation failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Logs performance benchmark for logging infrastructure
     */
    public void benchmarkLoggingPerformance() {
        int iterations = 1000;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            logger.debug("Benchmark iteration {}: testing logging performance", i);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        productionLoggingService.logSystemHealth(
            "logging_performance",
            duration,
            duration < 1000 ? "EXCELLENT" : (duration < 5000 ? "GOOD" : "POOR"),
            "1000ms for " + iterations + " iterations"
        );

        logger.info("Logging performance benchmark: {} iterations in {}ms (avg: {}ms per log)", 
                   iterations, duration, (double) duration / iterations);
    }
}