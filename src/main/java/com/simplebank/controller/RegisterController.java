package com.simplebank.controller;


import com.simplebank.service.BankService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

// Session 11 & 14 - JavaFX Controller & Event Handling
public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private Button registerButton;
    @FXML private Button cancelButton;
    @FXML private Label messageLabel;

    private BankService bankService;
    private LoginController loginController;

    public void setBankService(BankService bankService) {
        this.bankService = bankService;
    }

    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }

    @FXML
    private void handleRegisterButton(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();

        // Validate input
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                || fullName.isEmpty() || email.isEmpty()) {
            messageLabel.setText("All fields are required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match");
            return;
        }

        // Attempt registration
        boolean success = bankService.registerUser(username, password, fullName, email);

        if (success) {
            showAlert("Success", "Registration successful! Please log in.");
            closeStage();
        } else {
            messageLabel.setText("Username already exists");
        }
    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}