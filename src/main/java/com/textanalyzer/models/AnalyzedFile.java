package com.textanalyzer.models;

import java.io.File;

/**
 * POJO class representing a file being analyzed.
 * Contains file metadata and processing status.
 */
public class AnalyzedFile {
    
    private File file;
    private String status; // "PENDING", "PROCESSING", "COMPLETED", "FAILED"
    private AnalysisResult result;
    private String errorMessage;
    private double progress; // 0.0 to 1.0
    
    public AnalyzedFile(File file) {
        this.file = file;
        this.status = "PENDING";
        this.progress = 0.0;
    }
    
    // Getters and Setters
    
    public File getFile() {
        return file;
    }
    
    public void setFile(File file) {
        this.file = file;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public AnalysisResult getResult() {
        return result;
    }
    
    public void setResult(AnalysisResult result) {
        this.result = result;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public double getProgress() {
        return progress;
    }
    
    public void setProgress(double progress) {
        this.progress = progress;
    }
    
    @Override
    public String toString() {
        return "AnalyzedFile{" +
                "file=" + file.getName() +
                ", status='" + status + '\'' +
                ", progress=" + (progress * 100) + "%" +
                '}';
    }
}
