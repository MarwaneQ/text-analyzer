package com.textanalyzer.services;

import com.textanalyzer.models.AnalysisResult;
import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for analyzing text files concurrently.
 * Will be implemented in Sprint 2 with multithreading support.
 */
public class TextAnalysisService {
    
    /**
     * Analyzes a single text file asynchronously.
     * @param file File to analyze
     * @return CompletableFuture containing the analysis result
     */
    public CompletableFuture<AnalysisResult> analyzeFile(File file) {
        // TODO: Implement concurrent analysis logic in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet - Sprint 2");
    }
    
    /**
     * Calculates total word count in a file.
     * @param content File content as string
     * @return Total word count
     */
    public int countWords(String content) {
        // TODO: Implement in Sprint 3
        throw new UnsupportedOperationException("Not implemented yet - Sprint 3");
    }
    
    /**
     * Counts unique words in a file.
     * @param content File content as string
     * @return Number of unique words
     */
    public int countUniqueWords(String content) {
        // TODO: Implement in Sprint 3
        throw new UnsupportedOperationException("Not implemented yet - Sprint 3");
    }
}
