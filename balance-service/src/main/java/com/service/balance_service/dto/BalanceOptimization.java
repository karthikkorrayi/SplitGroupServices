package com.service.balance_service.dto;

import java.math.BigDecimal;
import java.util.List;

public class BalanceOptimization {

    private List<OptimizedPayment> suggestedPayments;
    private BigDecimal totalOptimizedAmount;
    private int originalTransactionCount;
    private int optimizedTransactionCount;
    private String optimizationSummary;

    // Default constructor
    public BalanceOptimization() {
    }

    public BalanceOptimization(List<OptimizedPayment> suggestedPayments) {
        this.suggestedPayments = suggestedPayments;
        this.optimizedTransactionCount = suggestedPayments.size();
        this.totalOptimizedAmount = suggestedPayments.stream()
                .map(OptimizedPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Getters and Setters
    public List<OptimizedPayment> getSuggestedPayments() {
        return suggestedPayments;
    }

    public void setSuggestedPayments(List<OptimizedPayment> suggestedPayments) {
        this.suggestedPayments = suggestedPayments;
    }

    public BigDecimal getTotalOptimizedAmount() {
        return totalOptimizedAmount;
    }

    public void setTotalOptimizedAmount(BigDecimal totalOptimizedAmount) {
        this.totalOptimizedAmount = totalOptimizedAmount;
    }

    public int getOriginalTransactionCount() {
        return originalTransactionCount;
    }

    public void setOriginalTransactionCount(int originalTransactionCount) {
        this.originalTransactionCount = originalTransactionCount;
    }

    public int getOptimizedTransactionCount() {
        return optimizedTransactionCount;
    }

    public void setOptimizedTransactionCount(int optimizedTransactionCount) {
        this.optimizedTransactionCount = optimizedTransactionCount;
    }

    public String getOptimizationSummary() {
        return optimizationSummary;
    }

    public void setOptimizationSummary(String optimizationSummary) {
        this.optimizationSummary = optimizationSummary;
    }

    public static class OptimizedPayment {
        private Long fromUserId;
        private String fromUserName;
        private Long toUserId;
        private String toUserName;
        private BigDecimal amount;
        private String description;

        public OptimizedPayment() {
        }

        public OptimizedPayment(Long fromUserId, Long toUserId, BigDecimal amount) {
            this.fromUserId = fromUserId;
            this.toUserId = toUserId;
            this.amount = amount;
            this.description = "User " + fromUserId + " should pay User " + toUserId + " $" + amount;
        }

        public OptimizedPayment(Long fromUserId, String fromUserName, Long toUserId, String toUserName, BigDecimal amount) {
            this.fromUserId = fromUserId;
            this.fromUserName = fromUserName;
            this.toUserId = toUserId;
            this.toUserName = toUserName;
            this.amount = amount;
            this.description = fromUserName + " should pay " + toUserName + " $" + amount;
        }

        // Getters and Setters
        public Long getFromUserId() {
            return fromUserId;
        }

        public void setFromUserId(Long fromUserId) {
            this.fromUserId = fromUserId;
        }

        public String getFromUserName() {
            return fromUserName;
        }

        public void setFromUserName(String fromUserName) {
            this.fromUserName = fromUserName;
        }

        public Long getToUserId() {
            return toUserId;
        }

        public void setToUserId(Long toUserId) {
            this.toUserId = toUserId;
        }

        public String getToUserName() {
            return toUserName;
        }

        public void setToUserName(String toUserName) {
            this.toUserName = toUserName;
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
    }
}