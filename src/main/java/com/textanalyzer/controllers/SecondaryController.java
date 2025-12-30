package com.textanalyzer.controllers;

import com.textanalyzer.App;
import com.textanalyzer.models.AnalysisResult;
import com.textanalyzer.models.MultiFileAnalysisResult;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.Map;

/**
 * Secondary controller for displaying analysis results.
 * Sprint 3: Updated to support multi-file concurrent analysis results.
 */
public class SecondaryController {

    // Progress section
    @FXML private VBox progressSection;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    
    // Results section (single file - Sprint 2 compatibility)
    @FXML private VBox resultsSection;
    @FXML private Label fileNameLabel;
    @FXML private Label totalWordsLabel;
    @FXML private Label totalCharactersLabel;
    @FXML private Label processingTimeLabel;
    @FXML private Label richnessLabel;
    @FXML private ListView<String> frequencyListView;
    @FXML private ListView<String> uniqueWordsListView;
    
    // Multi-file results section (Sprint 3)
    @FXML private VBox multiResultsSection;
    @FXML private Label totalFilesLabel;
    @FXML private Label successFilesLabel;
    @FXML private Label failedFilesLabel;
    @FXML private Label totalWordsAllLabel;
    @FXML private Label totalCharsAllLabel;
    @FXML private Label parallelTimeLabel;
    @FXML private Label speedupLabel;
    @FXML private Label threadsUsedLabel;
    @FXML private TableView<FileResultRow> filesTableView;
    @FXML private TableColumn<FileResultRow, String> fileNameColumn;
    @FXML private TableColumn<FileResultRow, String> wordsColumn;
    @FXML private TableColumn<FileResultRow, String> charsColumn;
    @FXML private TableColumn<FileResultRow, String> timeColumn;
    @FXML private TableColumn<FileResultRow, String> statusColumn;
    
    // Error section
    @FXML private VBox errorSection;
    @FXML private Label errorLabel;
    
    // Buttons and status
    @FXML private Button analyzeAnotherBtn;
    @FXML private Label statusLabel;
    @FXML private Label statusIndicator;
    
    // Static data for passing between controllers
    private static AnalysisResult currentResult;
    private static MultiFileAnalysisResult currentMultiResult;
    private static String currentError;

    @FXML
    public void initialize() {
        // Initialize table columns if they exist
        if (fileNameColumn != null) {
            fileNameColumn.setCellValueFactory(cellData -> cellData.getValue().fileNameProperty());
            wordsColumn.setCellValueFactory(cellData -> cellData.getValue().wordsProperty());
            charsColumn.setCellValueFactory(cellData -> cellData.getValue().charsProperty());
            timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
            statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        }
        
        // Show appropriate section based on state
        if (currentError != null) {
            showError(currentError);
            currentError = null;
        } else if (currentMultiResult != null) {
            displayMultiResults(currentMultiResult);
            currentMultiResult = null;
        } else if (currentResult != null) {
            displayResults(currentResult);
            currentResult = null;
        } else {
            showProgress();
        }
    }

    /**
     * Sets single-file analysis result (backward compatibility)
     */
    public static void setAnalysisResult(AnalysisResult result) {
        currentResult = result;
        currentMultiResult = null;
        currentError = null;
    }
    
    /**
     * Sets multi-file analysis result (Sprint 3)
     */
    public static void setMultiFileResult(MultiFileAnalysisResult result) {
        currentMultiResult = result;
        currentResult = null;
        currentError = null;
    }
    
    /**
     * Sets error message
     */
    public static void setError(String error) {
        currentError = error;
        currentResult = null;
        currentMultiResult = null;
    }
    
    /**
     * Shows progress section
     */
    private void showProgress() {
        setVisibility(progressSection, true);
        setVisibility(resultsSection, false);
        setVisibility(multiResultsSection, false);
        setVisibility(errorSection, false);
        updateStatus("Analyzing files...", "status-analyzing");
    }
    
    /**
     * Displays single-file results (Sprint 2 compatibility)
     */
    private void displayResults(AnalysisResult result) {
        setVisibility(progressSection, false);
        setVisibility(resultsSection, true);
        setVisibility(multiResultsSection, false);
        setVisibility(errorSection, false);
        setVisibility(analyzeAnotherBtn, true);
        
        fileNameLabel.setText("File: " + result.getFileName());
        
        // Set statistics
        setStatLabel(totalWordsLabel, String.format("%,d", result.getTotalWords()));
        setStatLabel(totalCharactersLabel, String.format("%,d", result.getTotalCharacters()));
        setStatLabel(processingTimeLabel, result.getProcessingTimeMs() + " ms");
        
        // Vocabulary richness
        int distinctWords = result.getWordFrequencies() != null ? result.getWordFrequencies().size() : 0;
        double richness = result.getTotalWords() > 0 
            ? (distinctWords * 100.0 / result.getTotalWords()) : 0;
        setStatLabel(richnessLabel, String.format("%.1f%%", richness));
        
        // Word frequencies
        populateFrequencyList(result);
        populateUniqueWordsList(result);
        
        updateStatus("Analysis complete!", "status-complete");
    }
    
    /**
     * Displays multi-file results (Sprint 3)
     */
    private void displayMultiResults(MultiFileAnalysisResult result) {
        setVisibility(progressSection, false);
        setVisibility(resultsSection, false);
        setVisibility(multiResultsSection, true);
        setVisibility(errorSection, false);
        setVisibility(analyzeAnotherBtn, true);
        
        // Summary statistics
        setStatLabel(totalFilesLabel, String.valueOf(result.getTotalFilesProcessed()));
        setStatLabel(successFilesLabel, String.valueOf(result.getSuccessCount()));
        setStatLabel(failedFilesLabel, String.valueOf(result.getFailureCount()));
        setStatLabel(totalWordsAllLabel, String.format("%,d", result.getTotalWordsAllFiles()));
        setStatLabel(totalCharsAllLabel, String.format("%,d", result.getTotalCharactersAllFiles()));
        setStatLabel(parallelTimeLabel, result.getOverallProcessingTime() + " ms");
        setStatLabel(speedupLabel, String.format("%.2fx", result.getSpeedupFactor()));
        setStatLabel(threadsUsedLabel, String.valueOf(result.getThreadsUsed()));
        
        // Populate table with individual file results
        if (filesTableView != null) {
            filesTableView.getItems().clear();
            
            // Add successful results
            for (AnalysisResult ar : result.getSuccessfulResults()) {
                filesTableView.getItems().add(new FileResultRow(
                    ar.getFileName(),
                    String.format("%,d", ar.getTotalWords()),
                    String.format("%,d", ar.getTotalCharacters()),
                    ar.getProcessingTimeMs() + " ms",
                    "✓ Success"
                ));
            }
            
            // Add failed files
            for (Map.Entry<String, String> entry : result.getFailedFiles().entrySet()) {
                filesTableView.getItems().add(new FileResultRow(
                    entry.getKey(),
                    "-",
                    "-",
                    "-",
                    "✗ " + entry.getValue()
                ));
            }
        }
        
        String statusMsg = String.format("Analyzed %d files in %d ms (%.2fx speedup)",
            result.getTotalFilesProcessed(),
            result.getOverallProcessingTime(),
            result.getSpeedupFactor());
        updateStatus(statusMsg, "status-complete");
    }
    
    /**
     * Shows error section
     */
    private void showError(String error) {
        setVisibility(progressSection, false);
        setVisibility(resultsSection, false);
        setVisibility(multiResultsSection, false);
        setVisibility(errorSection, true);
        
        errorLabel.setText(error);
        updateStatus("Analysis failed", "status-error");
    }
    
    /**
     * Helper to set visibility and managed properties
     */
    private void setVisibility(javafx.scene.Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }
    
    /**
     * Helper to set stat label with styling
     */
    private void setStatLabel(Label label, String text) {
        if (label != null) {
            label.setText(text);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");
        }
    }
    
    /**
     * Populates frequency list view
     */
    private void populateFrequencyList(AnalysisResult result) {
        frequencyListView.getItems().clear();
        if (result.getWordFrequencies() != null) {
            for (Map.Entry<String, Integer> entry : result.getWordFrequencies().entrySet()) {
                frequencyListView.getItems().add(
                    String.format("%-20s %,d", entry.getKey(), entry.getValue()));
            }
        }
    }
    
    /**
     * Populates unique words list view
     */
    private void populateUniqueWordsList(AnalysisResult result) {
        uniqueWordsListView.getItems().clear();
        if (result.getUniqueWordsList() != null) {
            uniqueWordsListView.getItems().addAll(result.getUniqueWordsList());
        }
    }
    
    /**
     * Updates progress (for external updates)
     */
    public void updateProgress(double progress, String message) {
        Platform.runLater(() -> {
            if (progressBar != null) progressBar.setProgress(progress);
            if (progressLabel != null) progressLabel.setText(message);
        });
    }

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
    
    @FXML
    private void analyzeAnother() throws IOException {
        currentResult = null;
        currentMultiResult = null;
        currentError = null;
        App.setRoot("primary");
    }
    
    private void updateStatus(String message, String styleClass) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
        if (statusIndicator != null) {
            statusIndicator.getStyleClass().removeAll(
                "status-ready", "status-analyzing", "status-complete", "status-error"
            );
            statusIndicator.getStyleClass().add(styleClass);
        }
    }
    
    /**
     * Inner class for table row data
     */
    public static class FileResultRow {
        private final SimpleStringProperty fileName;
        private final SimpleStringProperty words;
        private final SimpleStringProperty chars;
        private final SimpleStringProperty time;
        private final SimpleStringProperty status;
        
        public FileResultRow(String fileName, String words, String chars, 
                            String time, String status) {
            this.fileName = new SimpleStringProperty(fileName);
            this.words = new SimpleStringProperty(words);
            this.chars = new SimpleStringProperty(chars);
            this.time = new SimpleStringProperty(time);
            this.status = new SimpleStringProperty(status);
        }
        
        public StringProperty fileNameProperty() { return fileName; }
        public StringProperty wordsProperty() { return words; }
        public StringProperty charsProperty() { return chars; }
        public StringProperty timeProperty() { return time; }
        public StringProperty statusProperty() { return status; }
    }
}

