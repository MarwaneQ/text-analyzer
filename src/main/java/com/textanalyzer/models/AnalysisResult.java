package com.textanalyzer.models;

import java.util.List;
import java.util.Map;

/**
 * POJO class representing the analysis result for a text file.
 * Contains statistics such as total words, unique words, and word frequencies.
 */
public class AnalysisResult {
    
    private String fileName;
    private int totalWords;
    private int totalCharacters;
    private int uniqueWords;
    private List<String> uniqueWordsList;  // List of words with lowest frequency
    private Map<String, Integer> wordFrequencies;
    private long processingTimeMs;
    
    public AnalysisResult() {
    }
    
    public AnalysisResult(String fileName, int totalWords, int totalCharacters,
                          int uniqueWords, List<String> uniqueWordsList,
                          Map<String, Integer> wordFrequencies, long processingTimeMs) {
        this.fileName = fileName;
        this.totalWords = totalWords;
        this.totalCharacters = totalCharacters;
        this.uniqueWords = uniqueWords;
        this.uniqueWordsList = uniqueWordsList;
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
    
    public int getTotalCharacters() {
        return totalCharacters;
    }
    
    public void setTotalCharacters(int totalCharacters) {
        this.totalCharacters = totalCharacters;
    }
    
    public int getUniqueWords() {
        return uniqueWords;
    }
    
    public void setUniqueWords(int uniqueWords) {
        this.uniqueWords = uniqueWords;
    }
    
    public List<String> getUniqueWordsList() {
        return uniqueWordsList;
    }
    
    public void setUniqueWordsList(List<String> uniqueWordsList) {
        this.uniqueWordsList = uniqueWordsList;
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
