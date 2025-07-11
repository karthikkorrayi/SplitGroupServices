package com.service.transaction_service.service;

import com.service.transaction_service.dto.TransactionRequest;
import com.service.transaction_service.dto.TransactionResponse;
import com.service.transaction_service.dto.TransactionSummary;
import com.service.transaction_service.entity.Transaction;
import com.service.transaction_service.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AuthClientService authClientService;

    @Value("${transaction.rules.max-amount:100000.00}")
    private BigDecimal maxTransactionAmount;

    @Value("${transaction.rules.min-amount:0.01}")
    private BigDecimal minTransactionAmount;

    @Value("${transaction.rules.max-participants:20}")
    private int maxParticipants;

    public List<TransactionResponse> createTransaction(TransactionRequest request, Long createdByUserId) {
        // Validate request
        validateTransactionRequest(request);

        // Calculate individual amounts based on split type
        List<TransactionRequest.ParticipantShare> calculatedShares = calculateShares(request);

        // Create individual transaction records
        List<Transaction> transactions = new ArrayList<>();
        String groupId = generateGroupId();

        for (TransactionRequest.ParticipantShare participant : calculatedShares) {
            Transaction transaction = new Transaction();
            transaction.setPaidBy(request.getPaidBy());
            transaction.setOwedBy(participant.getUserId());
            transaction.setAmount(participant.getAmount());
            transaction.setDescription(request.getDescription());
            transaction.setCategory(request.getCategory());
            transaction.setTotalAmount(request.getTotalAmount());
            transaction.setTransactionDate(request.getTransactionDate() != null ?
                    request.getTransactionDate() : LocalDateTime.now());
            transaction.setCreatedBy(createdByUserId);
            transaction.setGroupId(groupId);
            transaction.setSplitType(request.getSplitType());
            transaction.setNotes(request.getNotes());

            transactions.add(transaction);
        }

        // Save all transactions
        List<Transaction> savedTransactions = transactionRepository.saveAll(transactions);

        // Convert to responses with user names
        return savedTransactions.stream()
                .map(this::convertToResponseWithUserNames)
                .collect(Collectors.toList());
    }

    public TransactionResponse getTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));

        return convertToResponseWithUserNames(transaction);
    }

    /**
     * Get all transactions for a user
     */
    public List<TransactionResponse> getUserTransactions(Long userId) {
        List<Transaction> transactions = transactionRepository.findTransactionsByUser(userId);
        return transactions.stream()
                .map(this::convertToResponseWithUserNames)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getTransactionsBetweenUsers(Long userId1, Long userId2) {
        List<Transaction> transactions = transactionRepository.findTransactionsBetweenUsers(userId1, userId2);
        return transactions.stream()
                .map(this::convertToResponseWithUserNames)
                .collect(Collectors.toList());
    }

    public TransactionSummary getUserTransactionSummary(Long userId) {
        BigDecimal totalPaid = transactionRepository.calculateTotalPaidByUser(userId);
        BigDecimal totalOwed = transactionRepository.calculateTotalOwedByUser(userId);
        Long transactionCount = transactionRepository.countTransactionsByUser(userId);

        // Get user name
        String userName = getUserName(userId);

        TransactionSummary summary = new TransactionSummary(userId, userName, totalPaid, totalOwed, transactionCount);

        // Set last transaction date
        List<Transaction> recentTransactions = transactionRepository.findRecentTransactionsByUser(userId);
        if (!recentTransactions.isEmpty()) {
            summary.setLastTransactionDate(recentTransactions.get(0).getTransactionDate());
        }

        return summary;
    }

    public BigDecimal calculateBalanceBetweenUsers(Long userId1, Long userId2) {
        return transactionRepository.calculateBalanceBetweenUsers(userId1, userId2);
    }

    public List<TransactionResponse> getTransactionsByCategory(String category) {
        List<Transaction> transactions = transactionRepository.findByCategoryOrderByTransactionDateDesc(category);
        return transactions.stream()
                .map(this::convertToResponseWithUserNames)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> searchTransactions(String description) {
        List<Transaction> transactions = transactionRepository.findByDescriptionContainingIgnoreCase(description);
        return transactions.stream()
                .map(this::convertToResponseWithUserNames)
                .collect(Collectors.toList());
    }

    public TransactionResponse updateTransactionStatus(Long transactionId, Transaction.TransactionStatus status, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));

        // Check if user has permission to modify this transaction
        if (!canUserModifyTransaction(transaction, userId)) {
            throw new RuntimeException("You don't have permission to modify this transaction");
        }

        transaction.setStatus(status);
        Transaction updatedTransaction = transactionRepository.save(transaction);

        return convertToResponseWithUserNames(updatedTransaction);
    }

    public void deleteTransaction(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));

        // Check if user has permission to delete this transaction
        if (!canUserModifyTransaction(transaction, userId)) {
            throw new RuntimeException("You don't have permission to delete this transaction");
        }

        transaction.setStatus(Transaction.TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);
    }

    public TransactionStats getTransactionStats() {
        Long totalTransactions = transactionRepository.countActiveTransactions();
        BigDecimal totalVolume = transactionRepository.getTotalTransactionVolume();

        return new TransactionStats(totalTransactions, totalVolume);
    }

    public List<TransactionResponse> getRecentTransactions(Long userId, int limit) {
        List<Transaction> transactions = transactionRepository.findRecentTransactionsByUser(userId);
        return transactions.stream()
                .limit(limit)
                .map(this::convertToResponseWithUserNames)
                .collect(Collectors.toList());
    }

    // Private helper methods
    private void validateTransactionRequest(TransactionRequest request) {
        // Validate amount range
        if (request.getTotalAmount().compareTo(minTransactionAmount) < 0) {
            throw new RuntimeException("Transaction amount must be at least " + minTransactionAmount);
        }

        if (request.getTotalAmount().compareTo(maxTransactionAmount) > 0) {
            throw new RuntimeException("Transaction amount cannot exceed " + maxTransactionAmount);
        }

        // Validate participants
        if (request.getParticipants() == null || request.getParticipants().isEmpty()) {
            throw new RuntimeException("At least one participant is required");
        }

        if (request.getParticipants().size() > maxParticipants) {
            throw new RuntimeException("Maximum " + maxParticipants + " participants allowed");
        }

        // Validate that payer is included in participants
        boolean payerIncluded = request.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(request.getPaidBy()));

        if (!payerIncluded) {
            throw new RuntimeException("The person who paid must be included in the participants");
        }

        // Validate exact amounts sum up to total (for EXACT split type)
        if (request.getSplitType() == Transaction.SplitType.EXACT) {
            BigDecimal totalSpecified = request.getParticipants().stream()
                    .map(TransactionRequest.ParticipantShare::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalSpecified.compareTo(request.getTotalAmount()) != 0) {
                throw new RuntimeException("Sum of individual amounts must equal total amount for exact splits");
            }
        }

        // Validate percentages sum up to 100 (for PERCENTAGE split type)
        if (request.getSplitType() == Transaction.SplitType.PERCENTAGE) {
            BigDecimal totalPercentage = request.getParticipants().stream()
                    .map(TransactionRequest.ParticipantShare::getPercentage)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalPercentage.compareTo(new BigDecimal("100")) != 0) {
                throw new RuntimeException("Percentages must sum up to 100% for percentage splits");
            }
        }
    }

    private List<TransactionRequest.ParticipantShare> calculateShares(TransactionRequest request) {
        List<TransactionRequest.ParticipantShare> calculatedShares = new ArrayList<>();

        switch (request.getSplitType()) {
            case EQUAL:
                BigDecimal equalShare = request.getTotalAmount()
                        .divide(new BigDecimal(request.getParticipants().size()), 2, RoundingMode.HALF_UP);

                for (TransactionRequest.ParticipantShare participant : request.getParticipants()) {
                    TransactionRequest.ParticipantShare share = new TransactionRequest.ParticipantShare();
                    share.setUserId(participant.getUserId());
                    share.setAmount(equalShare);
                    calculatedShares.add(share);
                }
                break;

            case EXACT:
                // Use the exact amounts provided
                calculatedShares.addAll(request.getParticipants());
                break;

            case PERCENTAGE:
                for (TransactionRequest.ParticipantShare participant : request.getParticipants()) {
                    TransactionRequest.ParticipantShare share = new TransactionRequest.ParticipantShare();
                    share.setUserId(participant.getUserId());

                    BigDecimal amount = request.getTotalAmount()
                            .multiply(participant.getPercentage())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                    share.setAmount(amount);
                    calculatedShares.add(share);
                }
                break;
        }

        return calculatedShares;
    }

    private String generateGroupId() {
        return "TXN_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    private TransactionResponse convertToResponseWithUserNames(Transaction transaction) {
        TransactionResponse response = new TransactionResponse(transaction);

        // Get user names
        try {
            response.setPaidByName(getUserName(transaction.getPaidBy()));
            response.setOwedByName(getUserName(transaction.getOwedBy()));
            if (transaction.getCreatedBy() != null) {
                response.setCreatedByName(getUserName(transaction.getCreatedBy()));
            }
        } catch (Exception e) {
            // If we can't get user names, that's okay - we'll just show IDs
            System.err.println("Warning: Could not fetch user names: " + e.getMessage());
        }

        return response;
    }

    private String getUserName(Long userId) {
        try {
            AuthClientService.UserInfo userInfo = authClientService.getUserInfo(userId);
            return userInfo.getName();
        } catch (Exception e) {
            return "User " + userId; // Fallback if we can't get the name
        }
    }

    private boolean canUserModifyTransaction(Transaction transaction, Long userId) {
        // User can modify if they are the creator, payer, or ower
        return userId.equals(transaction.getCreatedBy()) ||
                userId.equals(transaction.getPaidBy()) ||
                userId.equals(transaction.getOwedBy());
    }

    public static class TransactionStats {
        private Long totalTransactions;
        private BigDecimal totalVolume;
        private BigDecimal averageTransactionAmount;

        public TransactionStats(Long totalTransactions, BigDecimal totalVolume) {
            this.totalTransactions = totalTransactions;
            this.totalVolume = totalVolume;

            if (totalTransactions > 0) {
                this.averageTransactionAmount = totalVolume.divide(
                        new BigDecimal(totalTransactions), 2, RoundingMode.HALF_UP);
            } else {
                this.averageTransactionAmount = BigDecimal.ZERO;
            }
        }

        // Getters
        public Long getTotalTransactions() {
            return totalTransactions;
        }

        public BigDecimal getTotalVolume() {
            return totalVolume;
        }

        public BigDecimal getAverageTransactionAmount() {
            return averageTransactionAmount;
        }
    }
}