package com.textanalyzer.controllers;

import com.textanalyzer.App;
import com.textanalyzer.exceptions.InvalidFileException;
import com.textanalyzer.models.AnalysisResult;
import com.textanalyzer.models.MultiFileAnalysisResult;
import com.textanalyzer.services.MultiFileAnalysisService;
import com.textanalyzer.services.TextAnalysisService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Primary controller for the main view.
 * Sprint 3: Updated to support multi-threaded concurrent file analysis.
 */
public class PrimaryController {

    @FXML private ListView<String> fileListView;
    @FXML private Label statusLabel;
    @FXML private Label statusIndicator;
    @FXML private Label fileCountLabel;
    @FXML private Label emptyStateLabel;
    @FXML private Button analyzeBtn;
    
    // Store full paths for processing while displaying only names
    private Map<String, String> fileNameToPathMap = new HashMap<>();
    
    // Sprint 3: Multi-file analysis service
    private MultiFileAnalysisService multiFileAnalysisService = new MultiFileAnalysisService();

    @FXML
    public void initialize() {
        // Initially disable analyze button
        analyzeBtn.setDisable(true);
        
        // Set custom cell factory for file list with remove buttons
        fileListView.setCellFactory(listView -> new FileListCell());
        
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

        // Sprint 3: Collect all files for concurrent analysis
        List<File> filesToAnalyze = new ArrayList<>();
        for (String fileName : fileListView.getItems()) {
            String filePath = fileNameToPathMap.get(fileName);
            filesToAnalyze.add(new File(filePath));
        }
        
        int fileCount = filesToAnalyze.size();
        
        // Create a Task for multi-threaded background processing
        Task<MultiFileAnalysisResult> analysisTask = new Task<>() {
            @Override
            protected MultiFileAnalysisResult call() throws Exception {
                updateMessage("Starting concurrent analysis of " + fileCount + " files...");
                updateProgress(0, fileCount);
                
                // Set progress callback to update UI
                multiFileAnalysisService.setProgressCallback(progress -> {
                    Platform.runLater(() -> {
                        updateMessage(String.format("Analyzing: %s [%s] (%d/%d)", 
                            progress.currentFile, 
                            progress.threadName,
                            progress.completed, 
                            progress.total));
                        updateProgress(progress.completed, progress.total);
                    });
                });
                
                // Perform concurrent analysis
                MultiFileAnalysisResult result = multiFileAnalysisService.analyzeFiles(filesToAnalyze);
                
                updateMessage("Analysis complete!");
                updateProgress(fileCount, fileCount);
                
                return result;
            }
            
            @Override
            protected void succeeded() {
                MultiFileAnalysisResult result = getValue();
                SecondaryController.setMultiFileResult(result);
                
                try {
                    App.setRoot("secondary");
                } catch (IOException e) {
                    showError("Failed to load results view: " + e.getMessage());
                }
            }
            
            @Override
            protected void failed() {
                Throwable exception = getException();
                String errorMsg = "Multi-file analysis failed: " + exception.getMessage();
                
                SecondaryController.setError(errorMsg);
                
                try {
                    App.setRoot("secondary");
                } catch (IOException e) {
                    showError("Failed to load error view: " + e.getMessage());
                }
            }
        };

        // Update UI during task execution
        analysisTask.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            updateStatus(newMsg, "status-analyzing");
        });

        // Disable button during analysis
        analyzeBtn.setDisable(true);
        analysisTask.setOnSucceeded(e -> analyzeBtn.setDisable(false));
        analysisTask.setOnFailed(e -> analyzeBtn.setDisable(false));

        // Run task in background thread
        Thread analysisThread = new Thread(analysisTask);
        analysisThread.setDaemon(true);
        analysisThread.start();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Analysis Error");
        alert.setContentText(message);
        alert.showAndWait();
        updateStatus("Error occurred", "status-error");
        analyzeBtn.setDisable(false);
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
    
    private void removeFile(String fileName) {
        fileListView.getItems().remove(fileName);
        fileNameToPathMap.remove(fileName);
        
       
        
        if (fileListView.getItems().isEmpty()) {
            updateStatus("All files removed", "status-ready");
        } else {
            updateStatus("File removed", "status-ready");
        }
    }
    
    // Custom ListCell with remove button
    private class FileListCell extends ListCell<String> {
        private final HBox container;
        private final Label fileNameLabel;
        private final Button removeButton;
        
        public FileListCell() {
            container = new HBox(10);
            container.setAlignment(Pos.CENTER_LEFT);
            
            fileNameLabel = new Label();
            fileNameLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(fileNameLabel, Priority.ALWAYS);
            
            removeButton = new Button();
            removeButton.getStyleClass().add("remove-button");
            removeButton.setText("×");
            removeButton.setOnAction(e -> {
                String fileName = getItem();
                if (fileName != null) {
                    removeFile(fileName);
                }
            });
            
            container.getChildren().addAll(fileNameLabel, removeButton);
        }
        
        @Override
        protected void updateItem(String fileName, boolean empty) {
            super.updateItem(fileName, empty);
            
            if (empty || fileName == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(null); 
                fileNameLabel.setText(fileName);
                setGraphic(container);
            }
        }
    }
}
