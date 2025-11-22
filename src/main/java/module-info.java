module com.textanalyzer {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.textanalyzer to javafx.fxml;
    opens com.textanalyzer.controllers to javafx.fxml;
    
    exports com.textanalyzer;
    exports com.textanalyzer.controllers;
    exports com.textanalyzer.services;
    exports com.textanalyzer.models;
    exports com.textanalyzer.utils;
    exports com.textanalyzer.exceptions;
}
