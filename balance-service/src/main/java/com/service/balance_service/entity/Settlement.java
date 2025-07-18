package com.service.balance_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Payer user ID is required")
    @Column(name = "payer_id", nullable = false)
    private Long payerId;  // User who made the payment

    @NotNull(message = "Payee user ID is required")
    @Column(name = "payee_id", nullable = false)
    private Long payeeId;  // User who received the payment

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Settlement amount must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;  // Amount of the settlement

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;  // Optional description of the settlement

    @Column(name = "balance_id")
    private String balanceId;  // Reference to the balance this settlement affects

    @Column(name = "settlement_date")
    private LocalDateTime settlementDate;  // When the settlement occurred

    @Column(name = "created_by")
    private Long createdBy;  // User who recorded this settlement

    @Enumerated(EnumType.STRING)
    private SettlementMethod method = SettlementMethod.CASH;

    @Enumerated(EnumType.STRING)
    private SettlementStatus status = SettlementStatus.COMPLETED;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "notes")
    private String notes;  // Additional notes about the settlement

    @Column(name = "reference_id")
    private String referenceId;  // External reference

    // Enums
    public enum SettlementMethod {
        CASH,           // Cash payment
//        BANK_TRANSFER,  // Bank transfer
//        Phonepay,          // Phonepay payment
//        PAYPAL,         // PayPal payment
//        UPI,            // UPI payment (India)
//        OTHER           // Other payment method
    }

    public enum SettlementStatus {
        PENDING,    // Settlement recorded but not confirmed
        COMPLETED,  // Settlement completed successfully
        CANCELLED,  // Settlement was cancelled
        FAILED      // Settlement failed (e.g., payment bounced)
    }

    // Default constructor (required by JPA)
    public Settlement() {
    }

    // Constructor for creating new settlement
    public Settlement(Long payerId, Long payeeId, BigDecimal amount, String description) {
        this.payerId = payerId;
        this.payeeId = payeeId;
        this.amount = amount;
        this.description = description;
        this.balanceId = Balance.createBalanceId(payerId, payeeId);
        this.settlementDate = LocalDateTime.now();
        this.method = SettlementMethod.CASH;
        this.status = SettlementStatus.COMPLETED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // JPA lifecycle methods
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (settlementDate == null) {
            settlementDate = LocalDateTime.now();
        }
        if (balanceId == null) {
            balanceId = Balance.createBalanceId(payerId, payeeId);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean involvesUser(Long userId) {
        return userId.equals(payerId) || userId.equals(payeeId);
    }

    public BigDecimal getAmountForUser(Long userId) {
        if (userId.equals(payerId)) {
            return amount; // They paid this amount
        } else if (userId.equals(payeeId)) {
            return amount.negate(); // They received this amount
        } else {
            return BigDecimal.ZERO; // Not involved in this settlement
        }
    }

    public boolean isActive() {
        return status == SettlementStatus.COMPLETED;
    }

    public String getSettlementDescription() {
        return "User " + payerId + " paid User " + payeeId + " $" + amount +
                (description != null ? " (" + description + ")" : "");
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPayerId() {
        return payerId;
    }

    public void setPayerId(Long payerId) {
        this.payerId = payerId;
    }

    public Long getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(Long payeeId) {
        this.payeeId = payeeId;
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

    public String getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(String balanceId) {
        this.balanceId = balanceId;
    }

    public LocalDateTime getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(LocalDateTime settlementDate) {
        this.settlementDate = settlementDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public SettlementMethod getMethod() {
        return method;
    }

    public void setMethod(SettlementMethod method) {
        this.method = method;
    }

    public SettlementStatus getStatus() {
        return status;
    }

    public void setStatus(SettlementStatus status) {
        this.status = status;
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

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    @Override
    public String toString() {
        return "Settlement{" +
                "id=" + id +
                ", payerId=" + payerId +
                ", payeeId=" + payeeId +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", method=" + method +
                ", status=" + status +
                ", settlementDate=" + settlementDate +
                '}';
    }
}