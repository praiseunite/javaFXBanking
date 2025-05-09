module com.simplebank {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.simplebank to javafx.fxml;
    exports com.simplebank;

    // If you have FXML files that open other controllers, add:
    opens com.simplebank.controller to javafx.fxml;

    // Export the controller package to javafx.fxml
    exports com.simplebank.controller to javafx.fxml;
}