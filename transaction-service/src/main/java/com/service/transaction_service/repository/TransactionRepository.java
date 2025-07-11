package com.service.transaction_service.repository;

import com.service.transaction_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.paidBy = :userId OR t.owedBy = :userId ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByUser(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.paidBy = :userId1 AND t.owedBy = :userId2) OR " +
            "(t.paidBy = :userId2 AND t.owedBy = :userId1) " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsBetweenUsers(@Param("userId1") Long userId1,
                                                   @Param("userId2") Long userId2);

    List<Transaction> findByPaidByOrderByTransactionDateDesc(Long userId);

    List<Transaction> findByOwedByOrderByTransactionDateDesc(Long userId);

    List<Transaction> findByCategoryOrderByTransactionDateDesc(String category);

    List<Transaction> findByGroupIdOrderByTransactionDateDesc(String groupId);

    List<Transaction> findByStatusOrderByTransactionDateDesc(Transaction.TransactionStatus status);

    @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.paidBy = :userId AND t.status = 'ACTIVE'")
    BigDecimal calculateTotalPaidByUser(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.owedBy = :userId AND t.status = 'ACTIVE'")
    BigDecimal calculateTotalOwedByUser(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(CASE WHEN t.paidBy = :userId THEN t.amount ELSE -t.amount END), 0) " +
            "FROM Transaction t WHERE (t.paidBy = :userId OR t.owedBy = :userId) AND t.status = 'ACTIVE'")
    BigDecimal calculateNetBalanceForUser(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(CASE " +
            "WHEN t.paidBy = :userId1 AND t.owedBy = :userId2 THEN -t.amount " +
            "WHEN t.paidBy = :userId2 AND t.owedBy = :userId1 THEN t.amount " +
            "ELSE 0 END), 0) " +
            "FROM Transaction t WHERE " +
            "((t.paidBy = :userId1 AND t.owedBy = :userId2) OR " +
            "(t.paidBy = :userId2 AND t.owedBy = :userId1)) AND t.status = 'ACTIVE'")
    BigDecimal calculateBalanceBetweenUsers(@Param("userId1") Long userId1,
                                            @Param("userId2") Long userId2);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.paidBy = :userId OR t.owedBy = :userId")
    Long countTransactionsByUser(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.paidBy = :userId OR t.owedBy = :userId " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactionsByUser(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')) " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByDescriptionContainingIgnoreCase(@Param("description") String description);

    @Query("SELECT t FROM Transaction t ORDER BY t.amount DESC")
    List<Transaction> findLargestTransactions();

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = 'ACTIVE'")
    Long countActiveTransactions();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.status = 'ACTIVE'")
    BigDecimal getTotalTransactionVolume();

    List<Transaction> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
}