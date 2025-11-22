package com.textanalyzer.exceptions;

/**
 * Custom exception for invalid file operations.
 * Thrown when a file cannot be processed or is invalid for analysis.
 */
public class InvalidFileException extends Exception {
    
    public InvalidFileException(String message) {
        super(message);
    }
    
    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
