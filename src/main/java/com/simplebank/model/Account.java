package com.simplebank.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Account implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String accountNumber;
    private String accountType; // "SAVINGS" or "CHECKING"
    private BigDecimal balance;
    private LocalDateTime createdDate;
    // Session 1 - List interface for storing transactions
    private List<Transaction> transactions;

    public Account(String accountNumber, String accountType) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = BigDecimal.ZERO;
        this.createdDate = LocalDateTime.now();
        this.transactions = new ArrayList<>(); // Session 1 - ArrayList implementation
    }

    // Session 5 - Synchronized for thread safety in multithreading
    public synchronized boolean deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false; // Cannot deposit negative or zero amount
        }

        this.balance = this.balance.add(amount);
        this.transactions.add(new Transaction("DEPOSIT", amount, "Deposit to account"));
        return true;
    }

    // Session 5 - Synchronized method for thread safety
    public synchronized boolean withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(balance) > 0) {
            return false; // Invalid amount or insufficient funds
        }

        this.balance = this.balance.subtract(amount);
        this.transactions.add(new Transaction("WITHDRAWAL", amount.negate(), "Withdrawal from account"));
        return true;
    }

    // Getters
    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    //Task for test.
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }


    // Session 1 - Unmodifiable Collection for safe returns
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return accountNumber.equals(account.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }

    @Override
    public String toString() {
        return accountType + " Account: " + accountNumber + " (Balance: $" + balance + ")";
    }
}
