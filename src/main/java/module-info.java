module com.textanalyzer {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.textanalyzer to javafx.fxml;
    exports com.textanalyzer;
}
