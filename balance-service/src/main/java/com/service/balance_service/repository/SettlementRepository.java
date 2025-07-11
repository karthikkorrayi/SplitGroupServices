package com.service.balance_service.repository;

import com.service.balance_service.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @Query("SELECT s FROM Settlement s WHERE " +
            "(s.payerId = :userId1 AND s.payeeId = :userId2) OR " +
            "(s.payerId = :userId2 AND s.payeeId = :userId1) " +
            "ORDER BY s.settlementDate DESC")
    List<Settlement> findSettlementsBetweenUsers(@Param("userId1") Long userId1,
                                                 @Param("userId2") Long userId2);

    @Query("SELECT s FROM Settlement s WHERE s.payerId = :userId OR s.payeeId = :userId " +
            "ORDER BY s.settlementDate DESC")
    List<Settlement> findSettlementsByUser(@Param("userId") Long userId);

    List<Settlement> findByPayerIdOrderBySettlementDateDesc(Long payerId);

    List<Settlement> findByPayeeIdOrderBySettlementDateDesc(Long payeeId);

    List<Settlement> findByStatusOrderBySettlementDateDesc(Settlement.SettlementStatus status);

    List<Settlement> findByMethodOrderBySettlementDateDesc(Settlement.SettlementMethod method);

    @Query("SELECT s FROM Settlement s WHERE s.settlementDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.settlementDate DESC")
    List<Settlement> findSettlementsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Settlement s WHERE s.payerId = :userId " +
            "AND s.status = 'COMPLETED'")
    BigDecimal calculateTotalPaidByUser(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Settlement s WHERE s.payeeId = :userId " +
            "AND s.status = 'COMPLETED'")
    BigDecimal calculateTotalReceivedByUser(@Param("userId") Long userId);

    @Query("SELECT s FROM Settlement s WHERE s.payerId = :userId OR s.payeeId = :userId " +
            "ORDER BY s.settlementDate DESC")
    List<Settlement> findRecentSettlementsByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM Settlement s WHERE s.payerId = :userId OR s.payeeId = :userId")
    Long countSettlementsByUser(@Param("userId") Long userId);

    List<Settlement> findByBalanceIdOrderBySettlementDateDesc(String balanceId);

    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Settlement s WHERE s.status = 'COMPLETED'")
    BigDecimal getTotalSettlementVolume();

    @Query("SELECT COUNT(s) FROM Settlement s WHERE s.status = 'COMPLETED'")
    Long countCompletedSettlements();

    List<Settlement> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
}