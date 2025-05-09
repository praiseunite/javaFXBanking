package com.simplebank.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String type;  // "DEPOSIT", "WITHDRAWAL", "TRANSFER"
    private BigDecimal amount;
    private String description;
    private LocalDateTime timestamp;

    public Transaction(String type, BigDecimal amount, String description) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return timestamp + " | " + type + " | $" + amount.abs() + " | " + description;
    }


//    @Override
//    public String toString() {
//        return "Transaction{" +
//                "type='" + type + '\'' +
//                ", amount=" + amount +
//                ", description='" + description + '\'' +
//                ", timestamp=" + timestamp +
//                '}';
//    }
}
