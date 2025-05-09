package com.simplebank;


import com.simplebank.controller.LoginController;
import com.simplebank.service.BankService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Session 11 - JavaFX Application class
public class BankingApp extends Application {
    private BankService bankService;

    @Override
    public void init() {
        // Initialize services before UI loads
        bankService = new BankService();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Session 11 - JavaFX FXML loading
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();

        // Get controller and inject service
        LoginController controller = loader.getController();
        controller.setBankService(bankService);
        controller.setPrimaryStage(primaryStage);

        // Session 12 - JavaFX CSS styling
        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setTitle("Simple Banking App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Proper cleanup when application closes
        if (bankService != null) {
            bankService.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}