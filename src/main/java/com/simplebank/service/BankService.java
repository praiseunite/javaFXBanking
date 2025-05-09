package com.simplebank.service;

import com.simplebank.model.Account;
import com.simplebank.model.User;
import com.simplebank.util.FileUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// Core banking service
public class BankService {
    // Session 1 - Map interface, Session 5 - Thread-safe collection
    private Map<String, User> users;
    private static final String USERS_FILENAME = "users.dat";

    // Session 4 - Thread management using ExecutorService
    private ExecutorService executorService;

    public BankService() {
        // Session 5 - ConcurrentHashMap for thread safety
        users = new ConcurrentHashMap<>();

        // Session 4 - Fixed thread pool
        executorService = Executors.newFixedThreadPool(2);

        // Load users on startup
        loadUsers();
    }

    // Session 3 - File I/O operations
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        try {
            List<User> userList = FileUtil.loadFromFile(USERS_FILENAME);
            if (userList != null) {
                for (User user : userList) {
                    // Ensure accounts is initialized if it's null
                    if (user.getAccounts() == null) {
                        // Use reflection or setter to initialize the accounts field
                        // If you have a setter: user.setAccounts(new ArrayList<>());
                    }
                    users.put(user.getUsername(), user);
                }
                System.out.println("Loaded " + users.size() + " users");
            }
        } catch (Exception e) {
            System.err.println("Failed to load users: " + e.getMessage());
        }
    }

    // Session 4 - Asynchronous execution with Future
    public Future<?> saveUsersAsync() {
        return executorService.submit(() -> {
            try {
                saveUsers();
            } catch (Exception e) {
                System.err.println("Error saving users asynchronously: " + e.getMessage());
            }
        });
    }

    // Session 3 - File I/O operations
    private void saveUsers() {
        try {
            List<User> userList = new ArrayList<>(users.values());
            FileUtil.saveToFile(userList, USERS_FILENAME);
            System.out.println("Saved " + userList.size() + " users");
        } catch (IOException e) {
            System.err.println("Failed to save users: " + e.getMessage());
        }
    }

    // Create a new user
    public boolean registerUser(String username, String password, String fullName, String email) {
        if (username == null || username.trim().isEmpty() || users.containsKey(username)) {
            return false;
        }

        User newUser = new User(username, password, fullName, email);
        users.put(username, newUser);

        // Save changes
        saveUsersAsync();
        return true;
    }

    // Login with username and password
    // Session 2 - Return type using Java 8 Optional
    public Optional<User> loginUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.validatePassword(password)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    // Create a new account for a user
    public Account createAccount(String username, String accountType) {
        User user = users.get(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Generate unique account number
        String accountNumber = generateAccountNumber();
        Account account = new Account(accountNumber, accountType);

        user.addAccount(account);
        saveUsersAsync();

        return account;
    }

    // Generate a random account number
    private String generateAccountNumber() {
        Random random = new Random();
        return String.format("%010d", random.nextInt(1000000000));
    }

    // Deposit money into an account
    // Session 5 - Synchronized method for thread safety
    public synchronized boolean deposit(String username, String accountNumber, BigDecimal amount) {
        User user = users.get(username);
        if (user == null) {
            return false;
        }

        Optional<Account> accountOpt = findAccount(user, accountNumber);
        if (!accountOpt.isPresent()) {
            return false;
        }

        Account account = accountOpt.get();
        boolean success = account.deposit(amount);

        if (success) {
            saveUsersAsync();
        }

        return success;
    }

    // Withdraw money from an account
    // Session 5 - Synchronized method for thread safety
    public synchronized boolean withdraw(String username, String accountNumber, BigDecimal amount) {
        User user = users.get(username);
        if (user == null) {
            return false;
        }

        Optional<Account> accountOpt = findAccount(user, accountNumber);
        if (!accountOpt.isPresent()) {
            return false;
        }

        Account account = accountOpt.get();
        boolean success = account.withdraw(amount);

        if (success) {
            saveUsersAsync();
        }

        return success;
    }

    // Find an account by number
    // Session 2 - Generic method and using Optional
    public Optional<Account> findAccount(User user, String accountNumber) {
        return user.getAccounts().stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber))
                .findFirst();
    }

    // Get all users in the system
    // Session 3 - Stream API, Session 1 - List interface
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    // Session 3 - Stream API to filter and transform collections
    public List<Account> getAccountsByType(User user, String accountType) {
        return user.getAccounts().stream()
                .filter(account -> account.getAccountType().equals(accountType))
                .collect(Collectors.toList());
    }

    // Session 2 - Generic method with bounded type parameter and predicate
    public <T extends Account> List<T> filterAccounts(List<T> accounts, Predicate<T> condition) {
        return accounts.stream()
                .filter(condition)
                .collect(Collectors.toList());
    }

    // Close the service and clean up resources
    public void shutdown() {
        try {
            // Save all data before shutting down
            saveUsers();

            // Session 4 - Thread management, shutting down thread pool
            executorService.shutdown();
            System.out.println("Bank service shutdown complete");
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }

    // Get a user by username
    public Optional<User> getUser(String username) {
        return Optional.ofNullable(users.get(username));
    }
}