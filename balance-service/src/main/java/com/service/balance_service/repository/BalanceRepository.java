package com.service.balance_service.repository;

import com.service.balance_service.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, String> {

    @Query("SELECT b FROM Balance b WHERE " +
            "(b.user1 = :userId1 AND b.user2 = :userId2) OR " +
            "(b.user1 = :userId2 AND b.user2 = :userId1)")
    Optional<Balance> findBalanceBetweenUsers(@Param("userId1") Long userId1,
                                              @Param("userId2") Long userId2);

    @Query("SELECT b FROM Balance b WHERE b.user1 = :userId OR b.user2 = :userId")
    List<Balance> findBalancesByUser(@Param("userId") Long userId);

    @Query("SELECT b FROM Balance b WHERE (b.user1 = :userId OR b.user2 = :userId) " +
            "AND ABS(b.amount) > 0.01")
    List<Balance> findActiveBalancesByUser(@Param("userId") Long userId);

    @Query("SELECT b FROM Balance b WHERE ABS(b.amount) <= 0.01")
    List<Balance> findSettledBalances();

    @Query("SELECT b FROM Balance b WHERE " +
            "(b.user1 = :userId AND b.amount > 0) OR " +
            "(b.user2 = :userId AND b.amount < 0)")
    List<Balance> findBalancesWhereUserOwes(@Param("userId") Long userId);

    @Query("SELECT b FROM Balance b WHERE " +
            "(b.user1 = :userId AND b.amount < 0) OR " +
            "(b.user2 = :userId AND b.amount > 0)")
    List<Balance> findBalancesWhereUserIsOwed(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(CASE " +
            "WHEN b.user1 = :userId AND b.amount > 0 THEN b.amount " +
            "WHEN b.user2 = :userId AND b.amount < 0 THEN ABS(b.amount) " +
            "ELSE 0 END), 0) " +
            "FROM Balance b WHERE b.user1 = :userId OR b.user2 = :userId")
    BigDecimal calculateTotalOwedByUser(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(CASE " +
            "WHEN b.user1 = :userId AND b.amount < 0 THEN ABS(b.amount) " +
            "WHEN b.user2 = :userId AND b.amount > 0 THEN b.amount " +
            "ELSE 0 END), 0) " +
            "FROM Balance b WHERE b.user1 = :userId OR b.user2 = :userId")
    BigDecimal calculateTotalOwedToUser(@Param("userId") Long userId);

    @Query("SELECT b FROM Balance b WHERE ABS(b.amount) > 0.01 ORDER BY ABS(b.amount) DESC")
    List<Balance> findLargestOutstandingBalances();

    @Query("SELECT COUNT(b) FROM Balance b WHERE ABS(b.amount) > 0.01")
    Long countActiveBalances();

    @Query("SELECT COALESCE(SUM(ABS(b.amount)), 0) FROM Balance b WHERE ABS(b.amount) > 0.01")
    BigDecimal getTotalOutstandingAmount();

    @Query("SELECT b FROM Balance b WHERE b.user1 IN :userIds AND b.user2 IN :userIds")
    List<Balance> findBalancesByUserList(@Param("userIds") List<Long> userIds);
}