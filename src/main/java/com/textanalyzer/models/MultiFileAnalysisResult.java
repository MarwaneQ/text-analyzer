package com.textanalyzer.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Model class to hold results from multi-file concurrent analysis.
 * Uses thread-safe collections for concurrent access.
 * 
 * Sprint 3: Multi-Threaded Analyzer
 */
public class MultiFileAnalysisResult {
    
    // Thread-safe collections
    private final List<AnalysisResult> successfulResults;
    private final Map<String, String> failedFiles;  // fileName -> errorMessage
    
    // Aggregate statistics
    private long totalProcessingTimeMs;
    private int totalFilesProcessed;
    private final AtomicInteger completedCount;
    
    // Timing
    private long overallStartTime;
    private long overallEndTime;
    
    // Thread info
    private int threadsUsed;
    
    public MultiFileAnalysisResult() {
        this.successfulResults = new ArrayList<>();
        this.failedFiles = new ConcurrentHashMap<>();
        this.completedCount = new AtomicInteger(0);
        this.totalFilesProcessed = 0;
        this.totalProcessingTimeMs = 0;
        this.threadsUsed = 0;
    }
    
    /**
     * Adds a successful analysis result (thread-safe)
     */
    public synchronized void addResult(AnalysisResult result) {
        successfulResults.add(result);
        completedCount.incrementAndGet();
    }
    
    /**
     * Records a failed file analysis (thread-safe)
     */
    public void addFailure(String fileName, String errorMessage) {
        failedFiles.put(fileName, errorMessage);
        completedCount.incrementAndGet();
    }
    
    /**
     * Gets the number of completed analyses
     */
    public int getCompletedCount() {
        return completedCount.get();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // AGGREGATE STATISTICS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Gets total word count across all files
     */
    public int getTotalWordsAllFiles() {
        return successfulResults.stream()
            .mapToInt(AnalysisResult::getTotalWords)
            .sum();
    }
    
    /**
     * Gets total character count across all files
     */
    public int getTotalCharactersAllFiles() {
        return successfulResults.stream()
            .mapToInt(AnalysisResult::getTotalCharacters)
            .sum();
    }
    
    /**
     * Gets average words per file
     */
    public double getAverageWordsPerFile() {
        if (successfulResults.isEmpty()) return 0;
        return (double) getTotalWordsAllFiles() / successfulResults.size();
    }
    
    /**
     * Gets the file with most words
     */
    public AnalysisResult getLargestFile() {
        return successfulResults.stream()
            .max((a, b) -> Integer.compare(a.getTotalWords(), b.getTotalWords()))
            .orElse(null);
    }
    
    /**
     * Gets the file with least words
     */
    public AnalysisResult getSmallestFile() {
        return successfulResults.stream()
            .min((a, b) -> Integer.compare(a.getTotalWords(), b.getTotalWords()))
            .orElse(null);
    }
    
    /**
     * Gets the fastest processed file
     */
    public AnalysisResult getFastestFile() {
        return successfulResults.stream()
            .min((a, b) -> Long.compare(a.getProcessingTimeMs(), b.getProcessingTimeMs()))
            .orElse(null);
    }
    
    /**
     * Gets the slowest processed file
     */
    public AnalysisResult getSlowestFile() {
        return successfulResults.stream()
            .max((a, b) -> Long.compare(a.getProcessingTimeMs(), b.getProcessingTimeMs()))
            .orElse(null);
    }
    
    /**
     * Calculates overall wall-clock time (parallel execution time)
     */
    public long getOverallProcessingTime() {
        return overallEndTime - overallStartTime;
    }
    
    /**
     * Calculates sum of individual processing times (sequential equivalent)
     */
    public long getSequentialEquivalentTime() {
        return successfulResults.stream()
            .mapToLong(AnalysisResult::getProcessingTimeMs)
            .sum();
    }
    
    /**
     * Calculates speedup factor (sequential time / parallel time)
     */
    public double getSpeedupFactor() {
        long parallel = getOverallProcessingTime();
        long sequential = getSequentialEquivalentTime();
        if (parallel == 0) return 0;
        return (double) sequential / parallel;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // GETTERS AND SETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public List<AnalysisResult> getSuccessfulResults() {
        return new ArrayList<>(successfulResults);  // Return copy for thread safety
    }
    
    public Map<String, String> getFailedFiles() {
        return new ConcurrentHashMap<>(failedFiles);  // Return copy
    }
    
    public int getSuccessCount() {
        return successfulResults.size();
    }
    
    public int getFailureCount() {
        return failedFiles.size();
    }
    
    public long getTotalProcessingTimeMs() {
        return totalProcessingTimeMs;
    }
    
    public void setTotalProcessingTimeMs(long totalProcessingTimeMs) {
        this.totalProcessingTimeMs = totalProcessingTimeMs;
    }
    
    public int getTotalFilesProcessed() {
        return totalFilesProcessed;
    }
    
    public void setTotalFilesProcessed(int totalFilesProcessed) {
        this.totalFilesProcessed = totalFilesProcessed;
    }
    
    public long getOverallStartTime() {
        return overallStartTime;
    }
    
    public void setOverallStartTime(long overallStartTime) {
        this.overallStartTime = overallStartTime;
    }
    
    public long getOverallEndTime() {
        return overallEndTime;
    }
    
    public void setOverallEndTime(long overallEndTime) {
        this.overallEndTime = overallEndTime;
    }
    
    public int getThreadsUsed() {
        return threadsUsed;
    }
    
    public void setThreadsUsed(int threadsUsed) {
        this.threadsUsed = threadsUsed;
    }
    
    @Override
    public String toString() {
        return String.format(
            "MultiFileAnalysisResult{files=%d, success=%d, failed=%d, " +
            "totalWords=%d, parallelTime=%dms, speedup=%.2fx}",
            totalFilesProcessed, getSuccessCount(), getFailureCount(),
            getTotalWordsAllFiles(), getOverallProcessingTime(), getSpeedupFactor()
        );
    }
}
