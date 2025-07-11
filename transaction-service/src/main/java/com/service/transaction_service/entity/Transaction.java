package com.service.transaction_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Paid by user ID is required")
    @Column(name = "paid_by", nullable = false)
    private Long paidBy;  // User ID who actually paid the money

    @NotNull(message = "Owed by user ID is required")
    @Column(name = "owed_by", nullable = false)
    private Long owedBy;  // User ID who owes money for this transaction

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;  // Amount owed by this person

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(nullable = false)
    private String description;  // What was the expense for

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;  // Food, Transport, Entertainment, etc.

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;  // Total amount of the original expense

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;  // When the expense occurred

    @Column(name = "created_by")
    private Long createdBy;  // User who created this transaction record

    @Column(name = "group_id")
    private String groupId;  // Optional: for grouping related transactions

    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    private SplitType splitType = SplitType.EQUAL;

    // Metadata
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "notes")
    private String notes;  // Additional notes about the transaction

    // Enums
    public enum TransactionStatus {
        ACTIVE,     // Transaction is valid and counts toward balances
        CANCELLED,  // Transaction was cancelled
        SETTLED     // Transaction has been settled/paid back
    }

    public enum SplitType {
        EQUAL,      // Split equally among all participants
        EXACT,      // Exact amounts specified for each person
        PERCENTAGE  // Percentage-based split
    }

    // Default constructor (required by JPA)
    public Transaction() {
    }

    // Constructor for creating new transaction
    public Transaction(Long paidBy, Long owedBy, BigDecimal amount, String description) {
        this.paidBy = paidBy;
        this.owedBy = owedBy;
        this.amount = amount;
        this.description = description;
        this.status = TransactionStatus.ACTIVE;
        this.splitType = SplitType.EQUAL;
        this.transactionDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // JPA lifecycle methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods

    public boolean involvesUser(Long userId) {
        return userId.equals(paidBy) || userId.equals(owedBy);
    }

    public boolean isSelfTransaction() {
        return paidBy.equals(owedBy);
    }

    public BigDecimal getAmountForUser(Long userId) {
        if (userId.equals(owedBy)) {
            return amount; // They owe this amount
        } else if (userId.equals(paidBy)) {
            return amount.negate(); // They are owed this amount
        } else {
            return BigDecimal.ZERO; // Not involved in this transaction
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(Long paidBy) {
        this.paidBy = paidBy;
    }

    public Long getOwedBy() {
        return owedBy;
    }

    public void setOwedBy(Long owedBy) {
        this.owedBy = owedBy;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public SplitType getSplitType() {
        return splitType;
    }

    public void setSplitType(SplitType splitType) {
        this.splitType = splitType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", paidBy=" + paidBy +
                ", owedBy=" + owedBy +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", status=" + status +
                ", transactionDate=" + transactionDate +
                '}';
    }
}