package com.service.balance_service.service;

import com.service.balance_service.dto.BalanceOptimization;
import com.service.balance_service.dto.BalanceResponse;
import com.service.balance_service.dto.SettlementRequest;
import com.service.balance_service.entity.Balance;
import com.service.balance_service.entity.Settlement;
import com.service.balance_service.repository.BalanceRepository;
import com.service.balance_service.repository.SettlementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BalanceService {

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private AuthClientService authClientService;

    @Value("${balance.rules.min-settlement-amount:0.01}")
    private BigDecimal minSettlementAmount;

    @Value("${balance.rules.auto-settle-threshold:0.01}")
    private BigDecimal autoSettleThreshold;

    public void updateBalance(Long paidBy, Long owedBy, BigDecimal amount, Long transactionId) {
        if (paidBy.equals(owedBy)) {
            // Self-transaction, no balance update needed
            return;
        }

        String balanceId = Balance.createBalanceId(paidBy, owedBy);
        Balance balance = balanceRepository.findById(balanceId)
                .orElse(new Balance(paidBy, owedBy));

        // Calculate the balance change
        // If user1 paid for user2, then user2 owes user1
        if (paidBy.equals(balance.getUser1())) {
            // User1 paid, so user2 owes more (positive balance means user1 is owed money)
            balance.addAmount(amount.negate());
        } else {
            // User2 paid, so user1 owes more (positive balance means user1 owes money)
            balance.addAmount(amount);
        }

        balance.setLastTransactionId(transactionId);
        balanceRepository.save(balance);
    }

    public BalanceResponse getBalanceBetweenUsers(Long userId1, Long userId2) {
        Optional<Balance> balanceOpt = balanceRepository.findBalanceBetweenUsers(userId1, userId2);

        if (balanceOpt.isPresent()) {
            BalanceResponse response = new BalanceResponse(balanceOpt.get(), userId1);
            addUserNamesToBalance(response);
            return response;
        } else {
            // No balance exists, return zero balance
            Balance zeroBalance = new Balance(userId1, userId2);
            BalanceResponse response = new BalanceResponse(zeroBalance, userId1);
            addUserNamesToBalance(response);
            return response;
        }
    }

    public List<BalanceResponse> getUserBalances(Long userId) {
        List<Balance> balances = balanceRepository.findActiveBalancesByUser(userId);
        return balances.stream()
                .map(balance -> {
                    BalanceResponse response = new BalanceResponse(balance, userId);
                    addUserNamesToBalance(response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    public Settlement createSettlement(SettlementRequest request, Long createdByUserId) {
        // Validate settlement request
        validateSettlementRequest(request);

        // Create settlement record
        Settlement settlement = new Settlement(
                request.getPayerId(),
                request.getPayeeId(),
                request.getAmount(),
                request.getDescription()
        );

        settlement.setMethod(request.getMethod());
        settlement.setCreatedBy(createdByUserId);
        settlement.setNotes(request.getNotes());
        settlement.setReferenceId(request.getReferenceId());

        if (request.getSettlementDate() != null) {
            settlement.setSettlementDate(request.getSettlementDate());
        }

        // Save settlement
        Settlement savedSettlement = settlementRepository.save(settlement);

        // Update balance to reflect the settlement
        updateBalanceFromSettlement(savedSettlement);

        return savedSettlement;
    }

    public List<Settlement> getUserSettlements(Long userId) {
        return settlementRepository.findSettlementsByUser(userId);
    }

    public List<Settlement> getSettlementsBetweenUsers(Long userId1, Long userId2) {
        return settlementRepository.findSettlementsBetweenUsers(userId1, userId2);
    }

    public UserBalanceSummary getUserBalanceSummary(Long userId) {
        BigDecimal totalOwed = balanceRepository.calculateTotalOwedByUser(userId);
        BigDecimal totalOwedTo = balanceRepository.calculateTotalOwedToUser(userId);
        BigDecimal netBalance = totalOwedTo.subtract(totalOwed);

        List<Balance> activeBalances = balanceRepository.findActiveBalancesByUser(userId);
        long activeBalanceCount = activeBalances.size();

        BigDecimal totalPaid = settlementRepository.calculateTotalPaidByUser(userId);
        BigDecimal totalReceived = settlementRepository.calculateTotalReceivedByUser(userId);

        String userName = getUserName(userId);

        return new UserBalanceSummary(
                userId, userName, totalOwed, totalOwedTo, netBalance,
                activeBalanceCount, totalPaid, totalReceived
        );
    }

    public BalanceOptimization optimizeBalances(List<Long> userIds) {
        if (userIds.size() < 3) {
            return new BalanceOptimization(Collections.emptyList());
        }

        // Get all balances between the users
        List<Balance> balances = balanceRepository.findBalancesByUserList(userIds);

        // Calculate net position for each user
        Map<Long, BigDecimal> netPositions = calculateNetPositions(balances, userIds);

        // Generate optimized payments
        List<BalanceOptimization.OptimizedPayment> optimizedPayments =
                generateOptimizedPayments(netPositions);

        BalanceOptimization optimization = new BalanceOptimization(optimizedPayments);
        optimization.setOriginalTransactionCount(balances.size());

        String summary = String.format(
                "Reduced from %d potential transactions to %d optimized payments",
                balances.size(), optimizedPayments.size()
        );
        optimization.setOptimizationSummary(summary);

        return optimization;
    }

    public BalanceStats getBalanceStats() {
        Long activeBalances = balanceRepository.countActiveBalances();
        BigDecimal totalOutstanding = balanceRepository.getTotalOutstandingAmount();
        Long totalSettlements = settlementRepository.countCompletedSettlements();
        BigDecimal totalSettled = settlementRepository.getTotalSettlementVolume();

        return new BalanceStats(activeBalances, totalOutstanding, totalSettlements, totalSettled);
    }

    // Private helper methods
    private void validateSettlementRequest(SettlementRequest request) {
        if (request.getPayerId().equals(request.getPayeeId())) {
            throw new RuntimeException("Payer and payee cannot be the same user");
        }

        if (request.getAmount().compareTo(minSettlementAmount) < 0) {
            throw new RuntimeException("Settlement amount must be at least " + minSettlementAmount);
        }

        // Check if there's an outstanding balance to settle
        Optional<Balance> balance = balanceRepository.findBalanceBetweenUsers(
                request.getPayerId(), request.getPayeeId());

        if (balance.isPresent()) {
            BigDecimal payerOwes = balance.get().getAmountForUser(request.getPayerId());
            if (payerOwes.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("User " + request.getPayerId() +
                        " does not owe money to User " + request.getPayeeId());
            }

            if (request.getAmount().compareTo(payerOwes) > 0) {
                throw new RuntimeException("Settlement amount ($" + request.getAmount() +
                        ") cannot exceed outstanding balance ($" + payerOwes + ")");
            }
        } else {
            throw new RuntimeException("No outstanding balance found between these users");
        }
    }

    private void updateBalanceFromSettlement(Settlement settlement) {
        String balanceId = settlement.getBalanceId();
        Balance balance = balanceRepository.findById(balanceId).orElse(null);

        if (balance != null) {
            // Settlement reduces the amount owed
            if (settlement.getPayerId().equals(balance.getUser1())) {
                // User1 paid user2, so reduce user1's debt (subtract from positive balance)
                balance.subtractAmount(settlement.getAmount());
            } else {
                // User2 paid user1, so reduce user2's debt (add to positive balance)
                balance.addAmount(settlement.getAmount());
            }

            // Auto-settle if balance is very small
            if (balance.getAmount().abs().compareTo(autoSettleThreshold) <= 0) {
                balance.setAmount(BigDecimal.ZERO);
            }

            balanceRepository.save(balance);
        }
    }

    private Map<Long, BigDecimal> calculateNetPositions(List<Balance> balances, List<Long> userIds) {
        Map<Long, BigDecimal> netPositions = new HashMap<>();

        // Initialize all users with zero position
        for (Long userId : userIds) {
            netPositions.put(userId, BigDecimal.ZERO);
        }

        // Calculate net position for each user
        for (Balance balance : balances) {
            BigDecimal amount = balance.getAmount();

            if (amount.compareTo(BigDecimal.ZERO) != 0) {
                // User1 owes user2 if amount is positive
                BigDecimal user1Position = netPositions.get(balance.getUser1());
                BigDecimal user2Position = netPositions.get(balance.getUser2());

                netPositions.put(balance.getUser1(), user1Position.subtract(amount));
                netPositions.put(balance.getUser2(), user2Position.add(amount));
            }
        }

        return netPositions;
    }

    private List<BalanceOptimization.OptimizedPayment> generateOptimizedPayments(
            Map<Long, BigDecimal> netPositions) {

        List<BalanceOptimization.OptimizedPayment> payments = new ArrayList<>();

        // Separate debtors (negative balance) and creditors (positive balance)
        List<Map.Entry<Long, BigDecimal>> debtors = netPositions.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());

        List<Map.Entry<Long, BigDecimal>> creditors = netPositions.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toList());

        // Match debtors with creditors
        int debtorIndex = 0;
        int creditorIndex = 0;

        while (debtorIndex < debtors.size() && creditorIndex < creditors.size()) {
            Map.Entry<Long, BigDecimal> debtor = debtors.get(debtorIndex);
            Map.Entry<Long, BigDecimal> creditor = creditors.get(creditorIndex);

            BigDecimal debtAmount = debtor.getValue().abs();
            BigDecimal creditAmount = creditor.getValue();

            BigDecimal paymentAmount = debtAmount.min(creditAmount);

            if (paymentAmount.compareTo(autoSettleThreshold) > 0) {
                String debtorName = getUserName(debtor.getKey());
                String creditorName = getUserName(creditor.getKey());

                payments.add(new BalanceOptimization.OptimizedPayment(
                        debtor.getKey(), debtorName,
                        creditor.getKey(), creditorName,
                        paymentAmount.setScale(2, RoundingMode.HALF_UP)
                ));
            }

            // Update remaining amounts
            debtor.setValue(debtor.getValue().add(paymentAmount));
            creditor.setValue(creditor.getValue().subtract(paymentAmount));

            // Move to next debtor or creditor if settled
            if (debtor.getValue().abs().compareTo(autoSettleThreshold) <= 0) {
                debtorIndex++;
            }
            if (creditor.getValue().compareTo(autoSettleThreshold) <= 0) {
                creditorIndex++;
            }
        }

        return payments;
    }

    private void addUserNamesToBalance(BalanceResponse response) {
        try {
            response.setUser1Name(getUserName(response.getUser1()));
            response.setUser2Name(getUserName(response.getUser2()));
        } catch (Exception e) {
            // If we can't get user names, that's okay - we'll just show IDs
            System.err.println("Warning: Could not fetch user names: " + e.getMessage());
        }
    }

    private String getUserName(Long userId) {
        try {
            AuthClientService.UserInfo userInfo = authClientService.getUserInfo(userId);
            return userInfo.getName();
        } catch (Exception e) {
            return "User " + userId; // Fallback if we can't get the name
        }
    }

    public static class UserBalanceSummary {
        private Long userId;
        private String userName;
        private BigDecimal totalOwed;
        private BigDecimal totalOwedTo;
        private BigDecimal netBalance;
        private Long activeBalanceCount;
        private BigDecimal totalPaid;
        private BigDecimal totalReceived;

        public UserBalanceSummary(Long userId, String userName, BigDecimal totalOwed,
                                  BigDecimal totalOwedTo, BigDecimal netBalance,
                                  Long activeBalanceCount, BigDecimal totalPaid,
                                  BigDecimal totalReceived) {
            this.userId = userId;
            this.userName = userName;
            this.totalOwed = totalOwed;
            this.totalOwedTo = totalOwedTo;
            this.netBalance = netBalance;
            this.activeBalanceCount = activeBalanceCount;
            this.totalPaid = totalPaid;
            this.totalReceived = totalReceived;
        }

        // Getters
        public Long getUserId() { return userId; }
        public String getUserName() { return userName; }
        public BigDecimal getTotalOwed() { return totalOwed; }
        public BigDecimal getTotalOwedTo() { return totalOwedTo; }
        public BigDecimal getNetBalance() { return netBalance; }
        public Long getActiveBalanceCount() { return activeBalanceCount; }
        public BigDecimal getTotalPaid() { return totalPaid; }
        public BigDecimal getTotalReceived() { return totalReceived; }
    }

    public static class BalanceStats {
        private Long activeBalances;
        private BigDecimal totalOutstandingAmount;
        private Long totalSettlements;
        private BigDecimal totalSettledAmount;

        public BalanceStats(Long activeBalances, BigDecimal totalOutstandingAmount,
                            Long totalSettlements, BigDecimal totalSettledAmount) {
            this.activeBalances = activeBalances;
            this.totalOutstandingAmount = totalOutstandingAmount;
            this.totalSettlements = totalSettlements;
            this.totalSettledAmount = totalSettledAmount;
        }

        // Getters
        public Long getActiveBalances() { return activeBalances; }
        public BigDecimal getTotalOutstandingAmount() { return totalOutstandingAmount; }
        public Long getTotalSettlements() { return totalSettlements; }
        public BigDecimal getTotalSettledAmount() { return totalSettledAmount; }
    }
}