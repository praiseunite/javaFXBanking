package com.simplebank.controller;


import com.simplebank.model.Account;
import com.simplebank.model.Transaction;
import com.simplebank.model.User;
import com.simplebank.service.BankService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Session 11-14 - JavaFX Controller, UI Controls, and Event Handling
public class DashboardController {
    @FXML private Label welcomeLabel;
    @FXML private TableView<Account> accountsTable;
    @FXML private TableColumn<Account, String> accountNumberColumn;
    @FXML private TableColumn<Account, String> accountTypeColumn;
    @FXML private TableColumn<Account, BigDecimal> balanceColumn;
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, BigDecimal> amountColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    @FXML private Button newAccountButton;
    @FXML private Button depositButton;
    @FXML private Button withdrawButton;
    @FXML private PieChart accountsChart; // Session 13 - Charts

    private BankService bankService;
    private User currentUser;
    private ScheduledExecutorService scheduledExecutorService;

    public void initialize(BankService bankService, User user) {
        this.bankService = bankService;
        this.currentUser = user;

        welcomeLabel.setText("Welcome, " + user.getFullName() + "!");

        setupTableColumns();
        loadUserAccounts();

        // Session 4 & 5 - Schedule periodic refresh using threads
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(
                this::refreshData, 10, 10, TimeUnit.SECONDS);

        // Setup selection listener for accounts table
        accountsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        loadTransactions(newSelection);
                    }
                });
    }

    private void setupTableColumns() {
        // Account table columns
        accountNumberColumn.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        accountTypeColumn.setCellValueFactory(new PropertyValueFactory<>("accountType"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));

        // Transaction table columns
        dateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDate = cellData.getValue().getTimestamp().format(formatter);
            return new javafx.beans.property.SimpleStringProperty(formattedDate);
        });

        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void loadUserAccounts() {
        List<Account> accounts = currentUser.getAccounts();
        if (accounts == null) {
            accounts = new ArrayList<>(); // Handle null case
        }
        ObservableList<Account> accountList = FXCollections.observableArrayList(accounts);
        accountsTable.setItems(accountList);

        // Update chart - Session 13 - JavaFX Charts
        updateAccountsChart(accounts);
    }

    private void loadTransactions(Account account) {
        List<Transaction> transactions = account.getTransactions();
        ObservableList<Transaction> transactionList = FXCollections.observableArrayList(transactions);
        transactionsTable.setItems(transactionList);
    }

    // Session 13 - JavaFX Charts
    private void updateAccountsChart(List<Account> accounts) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Account account : accounts) {
            pieChartData.add(new PieChart.Data(
                    account.getAccountType() + " " + account.getAccountNumber(),
                    account.getBalance().doubleValue()
            ));
        }

        accountsChart.setData(pieChartData);
    }

    private void refreshData() {
        // This will be called periodically to refresh data
        if (currentUser != null) {
            // Refresh from the latest user data
            bankService.getUser(currentUser.getUsername()).ifPresent(user -> {
                currentUser = user;
                javafx.application.Platform.runLater(this::loadUserAccounts);
            });
        }
    }

    @FXML
    private void handleNewAccount(ActionEvent event) {
        // Show dialog for account type selection
        ChoiceDialog<String> dialog = new ChoiceDialog<>("SAVINGS", "SAVINGS", "CHECKING");
        dialog.setTitle("New Account");
        dialog.setHeaderText("Create New Account");
        dialog.setContentText("Select account type:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(accountType -> {
            Account newAccount = bankService.createAccount(currentUser.getUsername(), accountType);
            showAlert("Account Created",
                    "New " + accountType + " account created successfully.\n" +
                            "Account Number: " + newAccount.getAccountNumber());

            // Refresh the accounts view
            bankService.getUser(currentUser.getUsername()).ifPresent(user -> {
                currentUser = user;
                loadUserAccounts();
            });
        });
    }

    @FXML
    private void handleDeposit(ActionEvent event) {
        Account selectedAccount = accountsTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            showAlert("Selection Required", "Please select an account first.");
            return;
        }

        // Show dialog for amount
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Deposit");
        dialog.setHeaderText("Deposit to Account " + selectedAccount.getAccountNumber());
        dialog.setContentText("Enter deposit amount:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                boolean success = bankService.deposit(
                        currentUser.getUsername(), selectedAccount.getAccountNumber(), amount);

                if (success) {
                    showAlert("Success", "Successfully deposited $" + amount);

                    // Refresh UI
                    bankService.getUser(currentUser.getUsername()).ifPresent(user -> {
                        currentUser = user;
                        loadUserAccounts();

                        // Reload the transactions of the selected account
                        bankService.findAccount(user, selectedAccount.getAccountNumber())
                                .ifPresent(this::loadTransactions);
                    });
                } else {
                    showAlert("Error", "Deposit failed. Please enter a positive amount.");
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number.");
            }
        });
    }

    @FXML
    private void handleWithdraw(ActionEvent event) {
        Account selectedAccount = accountsTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            showAlert("Selection Required", "Please select an account first.");
            return;
        }

        // Show dialog for amount
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Withdraw");
        dialog.setHeaderText("Withdraw from Account " + selectedAccount.getAccountNumber());
        dialog.setContentText("Enter withdrawal amount:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                boolean success = bankService.withdraw(
                        currentUser.getUsername(), selectedAccount.getAccountNumber(), amount);

                if (success) {
                    showAlert("Success", "Successfully withdrew $" + amount);

                    // Refresh UI
                    bankService.getUser(currentUser.getUsername()).ifPresent(user -> {
                        currentUser = user;
                        loadUserAccounts();

                        // Reload the transactions of the selected account
                        bankService.findAccount(user, selectedAccount.getAccountNumber())
                                .ifPresent(this::loadTransactions);
                    });
                } else {
                    showAlert("Error", "Withdrawal failed. Insufficient funds or invalid amount.");
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number.");
            }
        });
    }

    // Clean up resources
    public void shutdown() {
        if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
