package com.textanalyzer.controllers;

import com.textanalyzer.App;
import com.textanalyzer.models.AnalysisResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.Map;

public class SecondaryController {

    @FXML private VBox progressSection;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    
    @FXML private VBox resultsSection;
    @FXML private Label fileNameLabel;
    @FXML private Label totalWordsLabel;
    @FXML private Label totalCharactersLabel;
    @FXML private Label processingTimeLabel;
    @FXML private Label richnessLabel;
    @FXML private ListView<String> frequencyListView;
    @FXML private ListView<String> uniqueWordsListView;
    
    @FXML private VBox errorSection;
    @FXML private Label errorLabel;
    
    @FXML private Button analyzeAnotherBtn;
    @FXML private Label statusLabel;
    @FXML private Label statusIndicator;
    
    private static AnalysisResult currentResult;
    private static String currentError;

    @FXML
    public void initialize() {
        // Show appropriate section based on state
        if (currentError != null) {
            showError(currentError);
            currentError = null;
        } else if (currentResult != null) {
            displayResults(currentResult);
            currentResult = null;
        } else {
            // Show progress by default
            showProgress();
        }
    }

    /**
     * Sets the analysis result to display (called before switching to this view)
     */
    public static void setAnalysisResult(AnalysisResult result) {
        currentResult = result;
        currentError = null;
    }
    
    /**
     * Sets the error message to display (called before switching to this view)
     */
    public static void setError(String error) {
        currentError = error;
        currentResult = null;
    }
    
    /**
     * Shows the progress section
     */
    private void showProgress() {
        progressSection.setVisible(true);
        progressSection.setManaged(true);
        resultsSection.setVisible(false);
        resultsSection.setManaged(false);
        errorSection.setVisible(false);
        errorSection.setManaged(false);
        
        updateStatus("Analyzing file...", "status-analyzing");
    }
    
    /**
     * Displays analysis results
     */
    private void displayResults(AnalysisResult result) {
        // Hide progress, show results
        progressSection.setVisible(false);
        progressSection.setManaged(false);
        resultsSection.setVisible(true);
        resultsSection.setManaged(true);
        errorSection.setVisible(false);
        errorSection.setManaged(false);
        analyzeAnotherBtn.setVisible(true);
        analyzeAnotherBtn.setManaged(true);
        
        // Set file name
        fileNameLabel.setText("File: " + result.getFileName());
        
        // Set statistics with white text color
        totalWordsLabel.setText(String.format("%,d", result.getTotalWords()));
        totalWordsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");
        
        totalCharactersLabel.setText(String.format("%,d", result.getTotalCharacters()));
        totalCharactersLabel.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");
        
        processingTimeLabel.setText(result.getProcessingTimeMs() + " ms");
        processingTimeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");
        
        // Calculate vocabulary richness (distinct words / total words * 100)
        int distinctWords = result.getWordFrequencies() != null ? result.getWordFrequencies().size() : 0;
        double richness = result.getTotalWords() > 0 
            ? (distinctWords * 100.0 / result.getTotalWords()) 
            : 0;
        richnessLabel.setText(String.format("%.1f%%", richness));
        richnessLabel.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");
        
        // Display word frequencies
        frequencyListView.getItems().clear();
        if (result.getWordFrequencies() != null && !result.getWordFrequencies().isEmpty()) {
            System.out.println("DEBUG: Adding " + result.getWordFrequencies().size() + " words to frequency list");
            for (Map.Entry<String, Integer> entry : result.getWordFrequencies().entrySet()) {
                String item = String.format("%-20s %,d", entry.getKey(), entry.getValue());
                System.out.println("DEBUG: Adding item: " + item);
                frequencyListView.getItems().add(item);
            }
            System.out.println("DEBUG: ListView now has " + frequencyListView.getItems().size() + " items");
        } else {
            System.out.println("DEBUG: No word frequencies found or map is null/empty");
        }
        
        // Display unique words (lowest frequency)
        uniqueWordsListView.getItems().clear();
        if (result.getUniqueWordsList() != null && !result.getUniqueWordsList().isEmpty()) {
            System.out.println("DEBUG: Adding " + result.getUniqueWordsList().size() + " unique words to list");
            for (String word : result.getUniqueWordsList()) {
                uniqueWordsListView.getItems().add(word);
            }
            System.out.println("DEBUG: Unique words ListView now has " + uniqueWordsListView.getItems().size() + " items");
        } else {
            System.out.println("DEBUG: No unique words found or list is null/empty");
        }
        
        updateStatus("Analysis complete!", "status-complete");
    }
    
    /**
     * Shows error message
     */
    private void showError(String error) {
        progressSection.setVisible(false);
        progressSection.setManaged(false);
        resultsSection.setVisible(false);
        resultsSection.setManaged(false);
        errorSection.setVisible(true);
        errorSection.setManaged(true);
        
        errorLabel.setText(error);
        updateStatus("Analysis failed", "status-error");
    }
    
    /**
     * Updates progress bar and label
     */
    public void updateProgress(double progress, String message) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            progressLabel.setText(message);
        });
    }

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
    
    @FXML
    private void analyzeAnother() throws IOException {
        currentResult = null;
        currentError = null;
        App.setRoot("primary");
    }
    
    private void updateStatus(String message, String styleClass) {
        statusLabel.setText(message);
        statusIndicator.getStyleClass().removeAll(
            "status-ready", "status-analyzing", "status-complete", "status-error"
        );
        statusIndicator.getStyleClass().add(styleClass);
    }
}

