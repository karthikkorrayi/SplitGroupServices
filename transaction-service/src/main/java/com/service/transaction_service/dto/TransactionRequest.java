package com.service.transaction_service.dto;

import com.service.transaction_service.entity.Transaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionRequest {

    @NotNull(message = "Paid by user ID is required")
    private Long paidBy;

    @NotNull(message = "At least one participant is required")
    @Size(min = 1, message = "At least one participant is required")
    private List<ParticipantShare> participants;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal totalAmount;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    private LocalDateTime transactionDate;

    private Transaction.SplitType splitType = Transaction.SplitType.EQUAL;

    private String groupId;

    private String notes;

    // Default constructor
    public TransactionRequest() {
    }

    // Getters and Setters
    public Long getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(Long paidBy) {
        this.paidBy = paidBy;
    }

    public List<ParticipantShare> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantShare> participants) {
        this.participants = participants;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Transaction.SplitType getSplitType() {
        return splitType;
    }

    public void setSplitType(Transaction.SplitType splitType) {
        this.splitType = splitType;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public static class ParticipantShare {

        @NotNull(message = "User ID is required")
        private Long userId;

        private BigDecimal amount;  // For exact splits

        private BigDecimal percentage;  // For percentage splits

        // Default constructor
        public ParticipantShare() {
        }

        public ParticipantShare(Long userId) {
            this.userId = userId;
        }

        public ParticipantShare(Long userId, BigDecimal amount) {
            this.userId = userId;
            this.amount = amount;
        }

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal getPercentage() {
            return percentage;
        }

        public void setPercentage(BigDecimal percentage) {
            this.percentage = percentage;
        }
    }
}