package com.textanalyzer.services;

import com.textanalyzer.exceptions.InvalidFileException;
import com.textanalyzer.models.AnalysisResult;
import com.textanalyzer.models.MultiFileAnalysisResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Service for analyzing multiple files concurrently.
 * Uses ThreadPoolManager for efficient thread management.
 * 
 * Sprint 3: Multi-Threaded Analyzer
 */
public class MultiFileAnalysisService {
    
    private final TextAnalysisService textAnalysisService;
    private final ThreadPoolManager threadPoolManager;
    
    // Progress callback
    private Consumer<ProgressUpdate> progressCallback;
    
    /**
     * Progress update class for UI updates
     */
    public static class ProgressUpdate {
        public final int completed;
        public final int total;
        public final String currentFile;
        public final String status;
        public final String threadName;
        
        public ProgressUpdate(int completed, int total, String currentFile, 
                             String status, String threadName) {
            this.completed = completed;
            this.total = total;
            this.currentFile = currentFile;
            this.status = status;
            this.threadName = threadName;
        }
        
        public double getProgress() {
            return total > 0 ? (double) completed / total : 0;
        }
    }
    
    public MultiFileAnalysisService() {
        this.textAnalysisService = new TextAnalysisService();
        this.threadPoolManager = ThreadPoolManager.getInstance();
    }
    
    /**
     * Sets the progress callback for UI updates
     */
    public void setProgressCallback(Consumer<ProgressUpdate> callback) {
        this.progressCallback = callback;
    }
    
    /**
     * Analyzes multiple files concurrently
     * @param files List of files to analyze
     * @return MultiFileAnalysisResult containing all results
     */
    public MultiFileAnalysisResult analyzeFiles(List<File> files) {
        MultiFileAnalysisResult result = new MultiFileAnalysisResult();
        result.setTotalFilesProcessed(files.size());
        result.setOverallStartTime(System.currentTimeMillis());
        
        if (files.isEmpty()) {
            result.setOverallEndTime(System.currentTimeMillis());
            return result;
        }
        
        // Create list to hold futures
        List<Future<AnalysisResult>> futures = new ArrayList<>();
        
        // Submit all files for concurrent processing
        for (File file : files) {
            Future<AnalysisResult> future = threadPoolManager.submit(() -> {
                String threadName = Thread.currentThread().getName();
                
                // Notify progress - starting file
                notifyProgress(result.getCompletedCount(), files.size(), 
                              file.getName(), "Analyzing...", threadName);
                
                try {
                    // Perform analysis
                    AnalysisResult analysisResult = textAnalysisService.analyzeFile(file);
                    
                    // Notify progress - completed file
                    notifyProgress(result.getCompletedCount() + 1, files.size(),
                                  file.getName(), "Complete", threadName);
                    
                    return analysisResult;
                    
                } catch (InvalidFileException e) {
                    // Record failure but don't throw - allow other files to continue
                    result.addFailure(file.getName(), e.getMessage());
                    notifyProgress(result.getCompletedCount(), files.size(),
                                  file.getName(), "Failed: " + e.getMessage(), threadName);
                    return null;
                }
            });
            
            futures.add(future);
        }
        
        // Collect results from all futures
        for (int i = 0; i < futures.size(); i++) {
            Future<AnalysisResult> future = futures.get(i);
            File file = files.get(i);
            
            try {
                // Wait for result with timeout
                AnalysisResult analysisResult = future.get(60, TimeUnit.SECONDS);
                
                if (analysisResult != null) {
                    result.addResult(analysisResult);
                }
                
            } catch (TimeoutException e) {
                result.addFailure(file.getName(), "Analysis timed out after 60 seconds");
                future.cancel(true);  // Cancel the task
                
            } catch (InterruptedException e) {
                result.addFailure(file.getName(), "Analysis was interrupted");
                Thread.currentThread().interrupt();
                
            } catch (ExecutionException e) {
                String errorMsg = e.getCause() != null 
                    ? e.getCause().getMessage() 
                    : e.getMessage();
                result.addFailure(file.getName(), "Error: " + errorMsg);
            }
        }
        
        // Record completion time and thread info
        result.setOverallEndTime(System.currentTimeMillis());
        result.setThreadsUsed(threadPoolManager.getActiveThreadCount());
        
        System.out.println("Multi-file analysis complete: " + result);
        System.out.println(threadPoolManager.getPoolStats());
        
        return result;
    }
    
    /**
     * Analyzes files with CompletableFuture for better async handling
     * Returns a CompletableFuture that completes when all files are analyzed
     */
    public CompletableFuture<MultiFileAnalysisResult> analyzeFilesAsync(List<File> files) {
        return CompletableFuture.supplyAsync(() -> analyzeFiles(files), 
                                             threadPoolManager.getExecutorService());
    }
    
    /**
     * Notifies progress callback if set
     */
    private void notifyProgress(int completed, int total, String currentFile, 
                                String status, String threadName) {
        if (progressCallback != null) {
            ProgressUpdate update = new ProgressUpdate(
                completed, total, currentFile, status, threadName
            );
            progressCallback.accept(update);
        }
    }
    
    /**
     * Gets the thread pool manager for direct access if needed
     */
    public ThreadPoolManager getThreadPoolManager() {
        return threadPoolManager;
    }
    
    /**
     * Shuts down the service
     */
    public void shutdown() {
        threadPoolManager.shutdown();
    }
}
