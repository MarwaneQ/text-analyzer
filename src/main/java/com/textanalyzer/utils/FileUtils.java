package com.textanalyzer.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Utility class for file operations.
 * Provides helper methods for reading and validating text files.
 */
public class FileUtils {
    
    /**
     * Reads the entire content of a text file.
     * @param file File to read
     * @return File content as string
     * @throws IOException if file cannot be read
     */
    public static String readFileContent(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist: " + file);
        }
        return Files.readString(file.toPath());
    }
    
    /**
     * Validates if a file is a text file based on extension.
     * @param file File to validate
     * @return true if file has .txt extension, false otherwise
     */
    public static boolean isTextFile(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String name = file.getName().toLowerCase();
        return name.endsWith(".txt");
    }
    
    /**
     * Gets the size of a file in bytes.
     * @param file File to check
     * @return File size in bytes
     */
    public static long getFileSize(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        return file.length();
    }
    
    /**
     * Formats file size in human-readable format.
     * @param bytes Size in bytes
     * @return Formatted string (e.g., "1.5 KB", "2.3 MB")
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
