package com.service.balance_service.controller;

import com.service.balance_service.dto.BalanceOptimization;
import com.service.balance_service.dto.BalanceResponse;
import com.service.balance_service.dto.SettlementRequest;
import com.service.balance_service.entity.Settlement;
import com.service.balance_service.service.AuthClientService;
import com.service.balance_service.service.BalanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/")  // Base path (API Gateway strips /api/balances)
public class BalanceController {

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private AuthClientService authClientService;

    @PostMapping("/update")
    public ResponseEntity<?> updateBalance(@RequestBody BalanceUpdateRequest request) {
        try {
            balanceService.updateBalance(
                    request.getPaidBy(),
                    request.getOwedBy(),
                    request.getAmount(),
                    request.getTransactionId()
            );
            return ResponseEntity.ok(new SuccessResponse("Balance updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{userId1}/{userId2}")
    public ResponseEntity<?> getBalanceBetweenUsers(@PathVariable Long userId1,
                                                    @PathVariable Long userId2) {
        try {
            BalanceResponse response = balanceService.getBalanceBetweenUsers(userId1, userId2);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBalances(@PathVariable Long userId,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            List<BalanceResponse> responses = balanceService.getUserBalances(userId);
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/settle")
    public ResponseEntity<?> createSettlement(@Valid @RequestBody SettlementRequest request,
                                              @RequestHeader("Authorization") String authHeader) {
        try {
            // Validate token and get user ID
            Long createdByUserId = authClientService.extractUserIdFromToken(authHeader);

            Settlement settlement = balanceService.createSettlement(request, createdByUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(settlement);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/settlements/user/{userId}")
    public ResponseEntity<?> getUserSettlements(@PathVariable Long userId) {
        try {
            List<Settlement> settlements = balanceService.getUserSettlements(userId);
            return ResponseEntity.ok(settlements);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/settlements/{userId1}/{userId2}")
    public ResponseEntity<?> getSettlementsBetweenUsers(@PathVariable Long userId1,
                                                        @PathVariable Long userId2) {
        try {
            List<Settlement> settlements = balanceService.getSettlementsBetweenUsers(userId1, userId2);
            return ResponseEntity.ok(settlements);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/summary/{userId}")
    public ResponseEntity<?> getUserBalanceSummary(@PathVariable Long userId) {
        try {
            BalanceService.UserBalanceSummary summary = balanceService.getUserBalanceSummary(userId);
            return ResponseEntity.ok(summary);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/optimize")
    public ResponseEntity<?> optimizeBalances(@RequestBody GroupOptimizationRequest request) {
        try {
            BalanceOptimization optimization = balanceService.optimizeBalances(request.getUserIds());
            return ResponseEntity.ok(optimization);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<BalanceService.BalanceStats> getBalanceStats() {
        BalanceService.BalanceStats stats = balanceService.getBalanceStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        BalanceService.BalanceStats stats = balanceService.getBalanceStats();
        return ResponseEntity.ok("Balance Service is healthy! Active balances: " + stats.getActiveBalances());
    }

    @GetMapping("/info")
    public ResponseEntity<SuccessResponse> info() {
        return ResponseEntity.ok(new SuccessResponse("Balance Service v1.0 - Balance management and settlements"));
    }

    // Helper DTOs
    public static class BalanceUpdateRequest {
        private Long paidBy;
        private Long owedBy;
        private BigDecimal amount;
        private Long transactionId;

        public BalanceUpdateRequest() {
        }

        // Getters and Setters
        public Long getPaidBy() {
            return paidBy;
        }

        public void setPaidBy(Long paidBy) {
            this.paidBy = paidBy;
        }

        public Long getOwedBy() {
            return owedBy;
        }

        public void setOwedBy(Long owedBy) {
            this.owedBy = owedBy;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public Long getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(Long transactionId) {
            this.transactionId = transactionId;
        }
    }

    public static class GroupOptimizationRequest {
        private List<Long> userIds;

        public GroupOptimizationRequest() {
        }

        public List<Long> getUserIds() {
            return userIds;
        }

        public void setUserIds(List<Long> userIds) {
            this.userIds = userIds;
        }
    }

    public static class ErrorResponse {
        private String message;
        private String timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = java.time.LocalDateTime.now().toString();
        }

        public String getMessage() {
            return message;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }

    public static class SuccessResponse {
        private String message;
        private String timestamp;

        public SuccessResponse(String message) {
            this.message = message;
            this.timestamp = java.time.LocalDateTime.now().toString();
        }

        public String getMessage() {
            return message;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }
}