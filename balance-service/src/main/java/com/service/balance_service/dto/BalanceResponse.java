package com.service.balance_service.dto;

import com.service.balance_service.entity.Balance;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BalanceResponse {

    private String balanceId;
    private Long user1;
    private String user1Name;
    private Long user2;
    private String user2Name;
    private BigDecimal amount;
    private String description;  // Human-readable description
    private boolean isSettled;
    private Long transactionCount;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;

    // Default constructor
    public BalanceResponse() {
    }

    // Constructor from Balance entity
    public BalanceResponse(Balance balance) {
        this.balanceId = balance.getBalanceId();
        this.user1 = balance.getUser1();
        this.user2 = balance.getUser2();
        this.amount = balance.getAmount();
        this.description = balance.getBalanceDescription();
        this.isSettled = balance.isSettled();
        this.transactionCount = balance.getTransactionCount();
        this.lastUpdated = balance.getLastUpdated();
        this.createdAt = balance.getCreatedAt();
    }

    // Constructor for user-specific view
    public BalanceResponse(Balance balance, Long fromUserPerspective) {
        this(balance);

        // Adjust amount from specific user's perspective
        this.amount = balance.getAmountForUser(fromUserPerspective);

        // Adjust description from user's perspective
        if (balance.isSettled()) {
            this.description = "Settled with " + getOtherUserName(fromUserPerspective);
        } else if (this.amount.compareTo(BigDecimal.ZERO) > 0) {
            this.description = "You owe " + getOtherUserName(fromUserPerspective) + " $" + this.amount;
        } else if (this.amount.compareTo(BigDecimal.ZERO) < 0) {
            this.description = getOtherUserName(fromUserPerspective) + " owes you $" + this.amount.abs();
        }
    }

    // Helper method to get other user name
    private String getOtherUserName(Long userId) {
        if (userId.equals(user1)) {
            return user2Name != null ? user2Name : "User " + user2;
        } else {
            return user1Name != null ? user1Name : "User " + user1;
        }
    }

    // Getters and Setters
    public String getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(String balanceId) {
        this.balanceId = balanceId;
    }

    public Long getUser1() {
        return user1;
    }

    public void setUser1(Long user1) {
        this.user1 = user1;
    }

    public String getUser1Name() {
        return user1Name;
    }

    public void setUser1Name(String user1Name) {
        this.user1Name = user1Name;
    }

    public Long getUser2() {
        return user2;
    }

    public void setUser2(Long user2) {
        this.user2 = user2;
    }

    public String getUser2Name() {
        return user2Name;
    }

    public void setUser2Name(String user2Name) {
        this.user2Name = user2Name;
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

    public boolean isSettled() {
        return isSettled;
    }

    public void setSettled(boolean settled) {
        isSettled = settled;
    }

    public Long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}