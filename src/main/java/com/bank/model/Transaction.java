package com.bank.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "sender_account_number")
    private String senderAccountNumber;

    @Column(name = "receiver_account_number")
    private String receiverAccountNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    public Transaction() {}

    public Transaction(String senderAccountNumber, String receiverAccountNumber, BigDecimal amount, TransactionType type) {
        this.senderAccountNumber = senderAccountNumber;
        this.receiverAccountNumber = receiverAccountNumber;
        this.amount = amount;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public String getSenderAccountNumber() { return senderAccountNumber; }
    public void setSenderAccountNumber(String senderAccountNumber) { this.senderAccountNumber = senderAccountNumber; }

    public String getReceiverAccountNumber() { return receiverAccountNumber; }
    public void setReceiverAccountNumber(String receiverAccountNumber) { this.receiverAccountNumber = receiverAccountNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", senderAccountNumber='" + senderAccountNumber + '\'' +
                ", receiverAccountNumber='" + receiverAccountNumber + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", type=" + type +
                '}';
    }
}