package com.simplebank.controller;

import com.simplebank.model.User;
import com.simplebank.service.BankService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

// Session 11 - JavaFX Controller class, Session 14 - Event handling
public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label messageLabel;

    private BankService bankService;
    private Stage primaryStage;

    public void setBankService(BankService bankService) {
        this.bankService = bankService;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // Session 14 - JavaFX Event Handling
    @FXML
    private void handleLoginButton(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password are required");
            return;
        }

        Optional<User> user = bankService.loginUser(username, password);
        if (user.isPresent()) {
            openDashboard(user.get());
        } else {
            messageLabel.setText("Invalid username or password");
            passwordField.clear();
        }
    }

    @FXML
    private void handleRegisterButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Register.fxml"));
            Parent root = loader.load();

            RegisterController controller = loader.getController();
            controller.setBankService(bankService);
            controller.setLoginController(this);

            Scene scene = new Scene(root, 600, 450);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage regStage = new Stage();
            regStage.setTitle("Register New Account");
            regStage.setScene(scene);
            regStage.show();

        } catch (IOException e) {
            showAlert("Error", "Could not load registration screen: " + e.getMessage());
        }
    }

    private void openDashboard(User user) {
        try {
            // Session 11 - JavaFX navigation
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.initialize(bankService, user);

            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            primaryStage.setTitle("Banking Dashboard - " + user.getFullName());
            primaryStage.setScene(scene);

        } catch (IOException e) {
            showAlert("Error", "Could not load dashboard: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
