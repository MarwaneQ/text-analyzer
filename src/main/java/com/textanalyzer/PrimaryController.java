package com.textanalyzer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;

public class PrimaryController {

    @FXML private ListView<String> fileListView;
    @FXML private Label statusLabel;

    @FXML
    public void onLoadFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Text Files");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        List<File> files = fileChooser.showOpenMultipleDialog(null);

        if (files != null) {
            fileListView.getItems().clear();
            for (File f : files) {
                fileListView.getItems().add(f.getAbsolutePath());
            }

            statusLabel.setText("Status: Files Loaded Successfully");
        }
    }

    @FXML
    public void onStartAnalysis() {
        statusLabel.setText("Status: Analyzing... (fake)");

// → Later you will replace this with real multithreading logic

        new Thread(() -> {
            try {
                Thread.sleep(1500); // simulate fast "analysis"
            } catch (Exception ignored) {}

            statusLabel.setText("Status: Analysis Complete!");
        }).start();
    }
}
