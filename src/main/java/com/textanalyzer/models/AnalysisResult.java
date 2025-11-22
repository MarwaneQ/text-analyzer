package com.textanalyzer.models;

import java.util.Map;

/**
 * POJO class representing the analysis result for a text file.
 * Contains statistics such as total words, unique words, and word frequencies.
 */
public class AnalysisResult {
    
    private String fileName;
    private int totalWords;
    private int uniqueWords;
    private Map<String, Integer> wordFrequencies;
    private long processingTimeMs;
    
    public AnalysisResult() {
    }
    
    public AnalysisResult(String fileName, int totalWords, int uniqueWords, 
                          Map<String, Integer> wordFrequencies, long processingTimeMs) {
        this.fileName = fileName;
        this.totalWords = totalWords;
        this.uniqueWords = uniqueWords;
        this.wordFrequencies = wordFrequencies;
        this.processingTimeMs = processingTimeMs;
    }
    
    // Getters and Setters
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public int getTotalWords() {
        return totalWords;
    }
    
    public void setTotalWords(int totalWords) {
        this.totalWords = totalWords;
    }
    
    public int getUniqueWords() {
        return uniqueWords;
    }
    
    public void setUniqueWords(int uniqueWords) {
        this.uniqueWords = uniqueWords;
    }
    
    public Map<String, Integer> getWordFrequencies() {
        return wordFrequencies;
    }
    
    public void setWordFrequencies(Map<String, Integer> wordFrequencies) {
        this.wordFrequencies = wordFrequencies;
    }
    
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    @Override
    public String toString() {
        return "AnalysisResult{" +
                "fileName='" + fileName + '\'' +
                ", totalWords=" + totalWords +
                ", uniqueWords=" + uniqueWords +
                ", processingTimeMs=" + processingTimeMs +
                '}';
    }
}
