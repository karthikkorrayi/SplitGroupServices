package com.service.transaction_service.dto;

import com.service.transaction_service.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private Long paidBy;
    private String paidByName;  // User name who paid
    private Long owedBy;
    private String owedByName;  // User name who owes
    private BigDecimal amount;
    private String description;
    private String category;
    private BigDecimal totalAmount;
    private LocalDateTime transactionDate;
    private Long createdBy;
    private String createdByName;
    private String groupId;
    private Transaction.TransactionStatus status;
    private Transaction.SplitType splitType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;

    // Default constructor
    public TransactionResponse() {
    }

    // Constructor from Transaction entity
    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.paidBy = transaction.getPaidBy();
        this.owedBy = transaction.getOwedBy();
        this.amount = transaction.getAmount();
        this.description = transaction.getDescription();
        this.category = transaction.getCategory();
        this.totalAmount = transaction.getTotalAmount();
        this.transactionDate = transaction.getTransactionDate();
        this.createdBy = transaction.getCreatedBy();
        this.groupId = transaction.getGroupId();
        this.status = transaction.getStatus();
        this.splitType = transaction.getSplitType();
        this.createdAt = transaction.getCreatedAt();
        this.updatedAt = transaction.getUpdatedAt();
        this.notes = transaction.getNotes();
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

    public String getPaidByName() {
        return paidByName;
    }

    public void setPaidByName(String paidByName) {
        this.paidByName = paidByName;
    }

    public Long getOwedBy() {
        return owedBy;
    }

    public void setOwedBy(Long owedBy) {
        this.owedBy = owedBy;
    }

    public String getOwedByName() {
        return owedByName;
    }

    public void setOwedByName(String owedByName) {
        this.owedByName = owedByName;
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

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Transaction.TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(Transaction.TransactionStatus status) {
        this.status = status;
    }

    public Transaction.SplitType getSplitType() {
        return splitType;
    }

    public void setSplitType(Transaction.SplitType splitType) {
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
}
