package com.securevault.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * AsyncConfig
 *
 * Configuration for asynchronous task execution using custom thread pool.
 * Enables @Async annotation support and defines thread pool parameters.
 *
 * Thread Pool Configuration:
 * - Core Pool Size: 5 threads (always kept alive)
 * - Max Pool Size: 10 threads (maximum concurrent threads)
 * - Queue Capacity: 50 tasks (pending tasks queue)
 * - Thread Name Prefix: "SecureVault-Async-" (for easy log identification)
 *
 * Use Cases:
 * - Email notifications (async)
 * - Activity logging (non-blocking)
 * - Password strength recalculation (background)
 * - Audit log processing (non-critical path)
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Custom thread pool executor for async operations
     *
     * @return configured ThreadPoolTaskExecutor
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool size: number of threads to keep alive
        executor.setCorePoolSize(5);
        
        // Maximum pool size: maximum number of threads
        executor.setMaxPoolSize(10);
        
        // Queue capacity: number of tasks that can be queued
        executor.setQueueCapacity(50);
        
        // Thread name prefix for debugging
        executor.setThreadNamePrefix("SecureVault-Async-");
        
        // Rejection policy: caller runs if queue is full
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // Initialize the executor
        executor.initialize();
        
        return executor;
    }

    /**
     * Secondary thread pool for low-priority tasks
     * Used for analytics, reporting, and background cleanup
     *
     * @return configured ThreadPoolTaskExecutor for low priority tasks
     */
    @Bean(name = "lowPriorityExecutor")
    public Executor lowPriorityExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("SecureVault-LowPriority-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy());
        executor.initialize();
        
        return executor;
    }
}
