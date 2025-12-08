package com.textanalyzer.services;

import com.textanalyzer.exceptions.InvalidFileException;
import com.textanalyzer.models.AnalysisResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for analyzing text files.
 * Implements single-threaded analysis for Sprint 2.
 */
public class TextAnalysisService {
    
    /**
     * Analyzes a single text file synchronously.
     * @param file File to analyze
     * @return AnalysisResult containing statistics
     * @throws InvalidFileException if file cannot be read
     */
    public AnalysisResult analyzeFile(File file) throws InvalidFileException {
        long startTime = System.currentTimeMillis();
        
        if (file == null || !file.exists()) {
            throw new InvalidFileException("File does not exist: " + 
                (file != null ? file.getName() : "null"));
        }
        
        if (!file.canRead()) {
            throw new InvalidFileException("Cannot read file: " + file.getName());
        }
        
        try {
            String content = readFileContent(file);
            
            int totalWords = countWords(content);
            int totalCharacters = countCharacters(content);
            int uniqueWords = countUniqueWords(content);
            List<String> uniqueWordsList = getUniqueWordsList(content);
            Map<String, Integer> frequencies = calculateWordFrequencies(content);
            
            // Get top 10 most frequent words
            Map<String, Integer> topFrequencies = getTopNFrequencies(frequencies, 10);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            return new AnalysisResult(
                file.getName(),
                totalWords,
                totalCharacters,
                uniqueWords,
                uniqueWordsList,
                topFrequencies,
                processingTime
            );
            
        } catch (IOException e) {
            throw new InvalidFileException("Error reading file: " + file.getName(), e);
        }
    }
    
    /**
     * Reads entire file content into a string.
     * @param file File to read
     * @return File content as string
     * @throws IOException if file cannot be read
     */
    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(" ");
            }
        }
        return content.toString();
    }
    
    /**
     * Calculates total word count in content.
     * Only counts words with 3 or more characters to filter out noise.
     * @param content Text content
     * @return Total word count
     */
    public int countWords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        
        // Split by whitespace and count valid words (3+ chars)
        String[] words = content.trim().split("\\s+");
        int count = 0;
        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "");
            if (cleanWord.length() >= 3) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Counts total characters in content (excluding whitespace).
     * @param content Text content
     * @return Total character count
     */
    public int countCharacters(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        
        // Count all non-whitespace characters
        return content.replaceAll("\\s+", "").length();
    }
    
    /**
     * Counts unique words in content (words with the lowest frequency).
     * Returns count of words that appear with the minimum frequency.
     * @param content Text content
     * @return Number of words with minimum frequency
     */
    public int countUniqueWords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        
        // Calculate word frequencies first
        Map<String, Integer> frequencies = calculateWordFrequencies(content);
        
        if (frequencies.isEmpty()) {
            return 0;
        }
        
        // Find the minimum frequency
        int minFrequency = frequencies.values().stream()
            .min(Integer::compareTo)
            .orElse(0);
        
        // Count words that have the minimum frequency
        int uniqueCount = 0;
        for (Integer frequency : frequencies.values()) {
            if (frequency == minFrequency) {
                uniqueCount++;
            }
        }
        
        return uniqueCount;
    }
    
    /**
     * Gets list of unique words (words with the lowest frequency).
     * Returns only ONE word - the rarest word in the text.
     * @param content Text content
     * @return List containing the single rarest word
     */
    public List<String> getUniqueWordsList(String content) {
        List<String> uniqueWords = new ArrayList<>();
        
        if (content == null || content.trim().isEmpty()) {
            return uniqueWords;
        }
        
        // Calculate word frequencies
        Map<String, Integer> frequencies = calculateWordFrequencies(content);
        
        if (frequencies.isEmpty()) {
            return uniqueWords;
        }
        
        // Find the minimum frequency
        int minFrequency = frequencies.values().stream()
            .min(Integer::compareTo)
            .orElse(0);
        
        // Get just ONE word with the minimum frequency (alphabetically first)
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            if (entry.getValue() == minFrequency) {
                uniqueWords.add(entry.getKey());
                break; // Only add one word
            }
        }
        
        return uniqueWords;
    }
    
    /**
     * Calculates word frequency map (case-insensitive).
     * Only counts words with 3 or more characters.
     * @param content Text content
     * @return Map of word to frequency count
     */
    public Map<String, Integer> calculateWordFrequencies(String content) {
        Map<String, Integer> frequencies = new HashMap<>();
        
        if (content == null || content.trim().isEmpty()) {
            return frequencies;
        }
        
        String[] words = content.toLowerCase().split("\\s+");
        
        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "");
            // Only count words with 3+ characters
            if (cleanWord.length() >= 3) {
                frequencies.put(cleanWord, frequencies.getOrDefault(cleanWord, 0) + 1);
            }
        }
        
        return frequencies;
    }
    
    /**
     * Gets top N most frequent words.
     * @param frequencies Word frequency map
     * @param n Number of top words to return
     * @return Map of top N words and their frequencies
     */
    private Map<String, Integer> getTopNFrequencies(Map<String, Integer> frequencies, int n) {
        return frequencies.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(n)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
}
