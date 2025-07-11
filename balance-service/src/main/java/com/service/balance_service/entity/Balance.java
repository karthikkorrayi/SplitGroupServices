package com.service.balance_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "balances")
public class Balance {

    @Id
    @Column(name = "balance_id")
    private String balanceId;  // Format: "userId1_userId2" (always ordered: user1 < user2)

    @NotNull
    @Column(name = "user1", nullable = false)
    private Long user1;  // Lower user ID

    @NotNull
    @Column(name = "user2", nullable = false)
    private Long user2;  // Higher user ID

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;  // Net balance amount

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "transaction_count")
    private Long transactionCount = 0L;  // Number of transactions contributing to this balance

    @Column(name = "last_transaction_id")
    private Long lastTransactionId;  // ID of the last transaction that updated this balance

    // Default constructor (required by JPA)
    public Balance() {
    }

    // Constructor with user IDs (automatically orders them)
    public Balance(Long userId1, Long userId2) {
        if (userId1.equals(userId2)) {
            throw new IllegalArgumentException("Cannot create balance between same user");
        }

        // Always order user IDs to ensure consistent balance ID
        if (userId1 < userId2) {
            this.user1 = userId1;
            this.user2 = userId2;
        } else {
            this.user1 = userId2;
            this.user2 = userId1;
        }

        this.balanceId = this.user1 + "_" + this.user2;
        this.amount = BigDecimal.ZERO;
        this.transactionCount = 0L;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    // Constructor with initial amount
    public Balance(Long userId1, Long userId2, BigDecimal initialAmount) {
        this(userId1, userId2);
        this.amount = initialAmount;
    }

    // JPA lifecycle methods
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    // Helper methods
    public void addAmount(BigDecimal amountToAdd) {
        this.amount = this.amount.add(amountToAdd);
        this.transactionCount++;
        this.lastUpdated = LocalDateTime.now();
    }

    public void subtractAmount(BigDecimal amountToSubtract) {
        this.amount = this.amount.subtract(amountToSubtract);
        this.transactionCount++;
        this.lastUpdated = LocalDateTime.now();
    }

    public BigDecimal getAmountForUser(Long userId) {
        if (userId.equals(user1)) {
            return amount; // Positive = user1 owes user2
        } else if (userId.equals(user2)) {
            return amount.negate(); // Negative = user2 owes user1
        } else {
            throw new IllegalArgumentException("User " + userId + " is not part of this balance");
        }
    }

    public Long getOtherUser(Long userId) {
        if (userId.equals(user1)) {
            return user2;
        } else if (userId.equals(user2)) {
            return user1;
        } else {
            throw new IllegalArgumentException("User " + userId + " is not part of this balance");
        }
    }

    public boolean isSettled() {
        return amount.abs().compareTo(new BigDecimal("0.01")) < 0;
    }

    public boolean userOwes(Long userId) {
        BigDecimal userAmount = getAmountForUser(userId);
        return userAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean userIsOwed(Long userId) {
        BigDecimal userAmount = getAmountForUser(userId);
        return userAmount.compareTo(BigDecimal.ZERO) < 0;
    }

    public String getBalanceDescription() {
        if (isSettled()) {
            return "Users " + user1 + " and " + user2 + " are settled";
        } else if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return "User " + user1 + " owes User " + user2 + " $" + amount;
        } else {
            return "User " + user2 + " owes User " + user1 + " $" + amount.abs();
        }
    }

    // Static utility method to create balance ID
    public static String createBalanceId(Long userId1, Long userId2) {
        if (userId1.equals(userId2)) {
            throw new IllegalArgumentException("Cannot create balance ID for same user");
        }

        Long lower = Math.min(userId1, userId2);
        Long higher = Math.max(userId1, userId2);
        return lower + "_" + higher;
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

    public Long getUser2() {
        return user2;
    }

    public void setUser2(Long user2) {
        this.user2 = user2;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public Long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public Long getLastTransactionId() {
        return lastTransactionId;
    }

    public void setLastTransactionId(Long lastTransactionId) {
        this.lastTransactionId = lastTransactionId;
    }

    @Override
    public String toString() {
        return "Balance{" +
                "balanceId='" + balanceId + '\'' +
                ", user1=" + user1 +
                ", user2=" + user2 +
                ", amount=" + amount +
                ", transactionCount=" + transactionCount +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}