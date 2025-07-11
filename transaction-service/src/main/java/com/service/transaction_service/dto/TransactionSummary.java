package com.service.transaction_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionSummary {

    private Long userId;
    private String userName;
    private BigDecimal totalPaid;      // Total amount this user has paid
    private BigDecimal totalOwed;      // Total amount this user owes
    private BigDecimal netBalance;     // Net balance (positive = owed money, negative = owes money)
    private Long transactionCount;
    private LocalDateTime lastTransactionDate;

    // Default constructor
    public TransactionSummary() {
    }

    public TransactionSummary(Long userId, String userName, BigDecimal totalPaid,
                              BigDecimal totalOwed, Long transactionCount) {
        this.userId = userId;
        this.userName = userName;
        this.totalPaid = totalPaid != null ? totalPaid : BigDecimal.ZERO;
        this.totalOwed = totalOwed != null ? totalOwed : BigDecimal.ZERO;
        this.transactionCount = transactionCount != null ? transactionCount : 0L;
        this.netBalance = this.totalPaid.subtract(this.totalOwed);
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
        updateNetBalance();
    }

    public BigDecimal getTotalOwed() {
        return totalOwed;
    }

    public void setTotalOwed(BigDecimal totalOwed) {
        this.totalOwed = totalOwed;
        updateNetBalance();
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }

    public Long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public LocalDateTime getLastTransactionDate() {
        return lastTransactionDate;
    }

    public void setLastTransactionDate(LocalDateTime lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }

    // Helper methods
    private void updateNetBalance() {
        if (totalPaid != null && totalOwed != null) {
            this.netBalance = totalPaid.subtract(totalOwed);
        }
    }

    public boolean isInDebt() {
        return netBalance != null && netBalance.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isOwedMoney() {
        return netBalance != null && netBalance.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isSettled() {
        return netBalance != null && netBalance.compareTo(BigDecimal.ZERO) == 0;
    }
}