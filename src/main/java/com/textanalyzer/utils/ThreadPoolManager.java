package com.textanalyzer.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for managing thread pools for concurrent file analysis.
 * Will be implemented in Sprint 2 with proper thread pool configuration.
 */
public class ThreadPoolManager {
    
    private static ThreadPoolManager instance;
    private ExecutorService executorService;
    private final int poolSize;
    
    private ThreadPoolManager(int poolSize) {
        this.poolSize = poolSize;
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }
    
    /**
     * Gets singleton instance of ThreadPoolManager.
     * @param poolSize Number of threads in the pool
     * @return ThreadPoolManager instance
     */
    public static synchronized ThreadPoolManager getInstance(int poolSize) {
        if (instance == null) {
            instance = new ThreadPoolManager(poolSize);
        }
        return instance;
    }
    
    /**
     * Gets the executor service.
     * @return ExecutorService instance
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }
    
    /**
     * Shuts down the thread pool gracefully.
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
