package com.textanalyzer.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrimaryController {

    @FXML private ListView<String> fileListView;
    @FXML private Label statusLabel;
    @FXML private Label statusIndicator;
    @FXML private Label fileCountLabel;
    @FXML private Label emptyStateLabel;
    @FXML private Button analyzeBtn;
    
    // Store full paths for processing while displaying only names
    private Map<String, String> fileNameToPathMap = new HashMap<>();

    @FXML
    public void initialize() {
        // Initially disable analyze button
        analyzeBtn.setDisable(true);
        
        // Listen for changes to file list to update empty state
        fileListView.getItems().addListener((javafx.collections.ListChangeListener<String>) c -> {
            updateUIState();
        });
    }

    @FXML
    public void onLoadFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Text Files to Analyze");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(null);

        if (files != null && !files.isEmpty()) {
            fileListView.getItems().clear();
            fileNameToPathMap.clear();
            
            for (File file : files) {
                String fileName = file.getName();
                fileListView.getItems().add(fileName);
                fileNameToPathMap.put(fileName, file.getAbsolutePath());
            }

            updateStatus("Files loaded successfully", "status-ready");
            analyzeBtn.setDisable(false);
        }
    }

    @FXML
    public void onStartAnalysis() {
        if (fileListView.getItems().isEmpty()) {
            updateStatus("No files to analyze", "status-error");
            return;
        }

        updateStatus("Analyzing files...", "status-analyzing");
        analyzeBtn.setDisable(true);

        // Placeholder for multithreading - will be replaced in Sprint 2
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simulate analysis
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> {
                updateStatus("Analysis complete!", "status-complete");
                analyzeBtn.setDisable(false);
            });
        }).start();
    }

    private void updateUIState() {
        int fileCount = fileListView.getItems().size();
        fileCountLabel.setText(fileCount + (fileCount == 1 ? " file" : " files"));
        
        boolean isEmpty = fileCount == 0;
        emptyStateLabel.setVisible(isEmpty);
        emptyStateLabel.setManaged(isEmpty);
        
        if (isEmpty) {
            analyzeBtn.setDisable(true);
        }
    }

    private void updateStatus(String message, String styleClass) {
        statusLabel.setText(message);
        statusIndicator.getStyleClass().removeAll(
            "status-ready", "status-loading", "status-analyzing", 
            "status-complete", "status-error"
        );
        statusIndicator.getStyleClass().add(styleClass);
    }
}
