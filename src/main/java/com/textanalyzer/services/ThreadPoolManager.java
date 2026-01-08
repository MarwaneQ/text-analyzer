package com.textanalyzer.services;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages thread pool for concurrent file analysis.
 * Uses ExecutorService with a configurable thread pool.
 * 
 * Sprint 3: Multi-Threaded Analyzer
 */
public class ThreadPoolManager {
    
    // Singleton instance
    private static ThreadPoolManager instance;
    
    // Thread pool executor
    private ExecutorService executorService;
    
    // Configuration - Use available processors for optimal concurrency
    private static final int CORE_POOL_SIZE = Math.max(4, Runtime.getRuntime().availableProcessors());
    private static final int MAX_POOL_SIZE = Math.max(8, Runtime.getRuntime().availableProcessors() * 2);
    private static final long KEEP_ALIVE_TIME = 60L;  // Seconds idle threads wait before terminating
    
    // Thread counter for naming
    private final AtomicInteger threadCounter = new AtomicInteger(0);
    
    // Track peak thread usage during execution
    private final AtomicInteger peakThreadCount = new AtomicInteger(0);
    private final AtomicInteger currentActiveThreads = new AtomicInteger(0);
    
    /**
     * Private constructor for singleton pattern
     */
    private ThreadPoolManager() {
        initializeThreadPool();
    }
    
    /**
     * Gets the singleton instance of ThreadPoolManager
     * Thread-safe lazy initialization
     */
    public static synchronized ThreadPoolManager getInstance() {
        if (instance == null) {
            instance = new ThreadPoolManager();
        }
        return instance;
    }
    
    /**
     * Initializes the thread pool with custom configuration
     */
    private void initializeThreadPool() {
        // Create a custom thread factory for naming threads
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("TextAnalyzer-Worker-" + threadCounter.incrementAndGet());
            thread.setDaemon(true);  // Daemon threads die when app closes
            return thread;
        };
        
        // Create ThreadPoolExecutor with SynchronousQueue for immediate thread creation
        // SynchronousQueue has no capacity, forcing new thread creation up to MAX_POOL_SIZE
        executorService = new ThreadPoolExecutor(
            CORE_POOL_SIZE,                          // Core pool size
            MAX_POOL_SIZE,                           // Maximum pool size
            KEEP_ALIVE_TIME,                         // Keep alive time
            TimeUnit.SECONDS,                        // Time unit
            new SynchronousQueue<>(),                // SynchronousQueue forces new thread creation
            threadFactory,                           // Thread factory
            new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
        );
        
        // Pre-start core threads for faster initial response
        ((ThreadPoolExecutor) executorService).prestartAllCoreThreads();
        
        System.out.println("ThreadPoolManager initialized with " + CORE_POOL_SIZE + 
                          " core threads, max " + MAX_POOL_SIZE + " threads");
    }
    
    /**
     * Submits a Callable task for execution
     * Tracks thread usage for statistics
     * @param task The task to execute
     * @return Future representing the pending result
     */
    public <T> Future<T> submit(Callable<T> task) {
        // Wrap task to track thread usage
        Callable<T> wrappedTask = () -> {
            int active = currentActiveThreads.incrementAndGet();
            // Update peak if current is higher
            peakThreadCount.updateAndGet(peak -> Math.max(peak, active));
            try {
                return task.call();
            } finally {
                currentActiveThreads.decrementAndGet();
            }
        };
        return executorService.submit(wrappedTask);
    }
    
    /**
     * Submits a Runnable task for execution
     * @param task The task to execute
     * @return Future representing the task completion
     */
    public Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }
    
    /**
     * Executes a Runnable task (fire and forget)
     * @param task The task to execute
     */
    public void execute(Runnable task) {
        executorService.execute(task);
    }
    
    /**
     * Gets the ExecutorService for advanced usage
     * @return The underlying ExecutorService
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }
    
    /**
     * Gets current pool statistics
     * @return String with pool status information
     */
    public String getPoolStats() {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;
            return String.format(
                "Pool Stats: Active=%d, Completed=%d, Total=%d, Queue=%d",
                tpe.getActiveCount(),
                tpe.getCompletedTaskCount(),
                tpe.getTaskCount(),
                tpe.getQueue().size()
            );
        }
        return "Pool stats not available";
    }
    
    /**
     * Gets the number of active threads
     * @return Active thread count
     */
    public int getActiveThreadCount() {
        if (executorService instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executorService).getActiveCount();
        }
        return 0;
    }
    
    /**
     * Gets the peak number of threads used during execution
     * @return Peak thread count
     */
    public int getPeakThreadCount() {
        return peakThreadCount.get();
    }
    
    /**
     * Resets peak thread counter (call before new analysis batch)
     */
    public void resetPeakThreadCount() {
        peakThreadCount.set(0);
    }
    
    /**
     * Gets the largest pool size reached
     * @return Largest pool size
     */
    public int getLargestPoolSize() {
        if (executorService instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executorService).getLargestPoolSize();
        }
        return 0;
    }
    
    /**
     * Shuts down the thread pool gracefully
     * Waits for currently executing tasks to complete
     */
    public void shutdown() {
        System.out.println("Shutting down ThreadPoolManager...");
        executorService.shutdown();
        try {
            // Wait up to 30 seconds for tasks to complete
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                System.out.println("Forcing shutdown...");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("ThreadPoolManager shut down complete");
    }
    
    /**
     * Restarts the thread pool (useful after shutdown)
     */
    public void restart() {
        if (executorService.isShutdown()) {
            threadCounter.set(0);
            initializeThreadPool();
        }
    }
    
    /**
     * Checks if the pool is running
     * @return true if pool is active
     */
    public boolean isRunning() {
        return !executorService.isShutdown() && !executorService.isTerminated();
    }
}
