package com.service.transaction_service.controller;

import com.service.transaction_service.dto.TransactionRequest;
import com.service.transaction_service.dto.TransactionResponse;
import com.service.transaction_service.dto.TransactionSummary;
import com.service.transaction_service.entity.Transaction;
import com.service.transaction_service.service.AuthClientService;
import com.service.transaction_service.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/")  // Base path (API Gateway strips /api/transactions)
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AuthClientService authClientService;

    @PostMapping("/")
    public ResponseEntity<?> createTransaction(@Valid @RequestBody TransactionRequest request,
                                               @RequestHeader("Authorization") String authHeader) {
        try {
            // Validate token and get user ID
            Long createdByUserId = authClientService.extractUserIdFromToken(authHeader);

            // Verify that the user creating the transaction is authorized
            // For now, anyone can create transactions for anyone, but we could add checks here

            List<TransactionResponse> responses = transactionService.createTransaction(request, createdByUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransaction(@PathVariable Long transactionId) {
        try {
            TransactionResponse response = transactionService.getTransaction(transactionId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserTransactions(@PathVariable Long userId,
                                                 @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // For now, allow anyone to view any user's transactions
            // In production, you might want to restrict this

            List<TransactionResponse> responses = transactionService.getUserTransactions(userId);
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/between/{userId1}/{userId2}")
    public ResponseEntity<?> getTransactionsBetweenUsers(@PathVariable Long userId1,
                                                         @PathVariable Long userId2) {
        try {
            List<TransactionResponse> responses = transactionService.getTransactionsBetweenUsers(userId1, userId2);
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/summary/{userId}")
    public ResponseEntity<?> getUserTransactionSummary(@PathVariable Long userId) {
        try {
            TransactionSummary summary = transactionService.getUserTransactionSummary(userId);
            return ResponseEntity.ok(summary);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/balance/{userId1}/{userId2}")
    public ResponseEntity<?> getBalanceBetweenUsers(@PathVariable Long userId1,
                                                    @PathVariable Long userId2) {
        try {
            BigDecimal balance = transactionService.calculateBalanceBetweenUsers(userId1, userId2);
            BalanceResponse response = new BalanceResponse(userId1, userId2, balance);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getTransactionsByCategory(@PathVariable String category) {
        try {
            List<TransactionResponse> responses = transactionService.getTransactionsByCategory(category);
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchTransactions(@RequestParam("q") String description) {
        try {
            List<TransactionResponse> responses = transactionService.searchTransactions(description);
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{transactionId}/status")
    public ResponseEntity<?> updateTransactionStatus(@PathVariable Long transactionId,
                                                     @RequestBody StatusUpdateRequest statusRequest,
                                                     @RequestHeader("Authorization") String authHeader) {
        try {
            // Validate token and get user ID
            Long userId = authClientService.extractUserIdFromToken(authHeader);

            TransactionResponse response = transactionService.updateTransactionStatus(
                    transactionId, statusRequest.getStatus(), userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long transactionId,
                                               @RequestHeader("Authorization") String authHeader) {
        try {
            // Validate token and get user ID
            Long userId = authClientService.extractUserIdFromToken(authHeader);

            transactionService.deleteTransaction(transactionId, userId);
            return ResponseEntity.ok(new SuccessResponse("Transaction deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/recent/{userId}")
    public ResponseEntity<?> getRecentTransactions(@PathVariable Long userId,
                                                   @RequestParam(defaultValue = "10") int limit) {
        try {
            List<TransactionResponse> responses = transactionService.getRecentTransactions(userId, limit);
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<TransactionService.TransactionStats> getTransactionStats() {
        TransactionService.TransactionStats stats = transactionService.getTransactionStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        TransactionService.TransactionStats stats = transactionService.getTransactionStats();
        return ResponseEntity.ok("Transaction Service is healthy! Total transactions: " + stats.getTotalTransactions());
    }

    @GetMapping("/info")
    public ResponseEntity<SuccessResponse> info() {
        return ResponseEntity.ok(new SuccessResponse("Transaction Service v1.0 - Expense tracking and bill splitting"));
    }

    // Helper DTOs
    public static class StatusUpdateRequest {
        private Transaction.TransactionStatus status;

        public StatusUpdateRequest() {
        }

        public Transaction.TransactionStatus getStatus() {
            return status;
        }

        public void setStatus(Transaction.TransactionStatus status) {
            this.status = status;
        }
    }

    public static class BalanceResponse {
        private Long userId1;
        private Long userId2;
        private BigDecimal balance;
        private String interpretation;

        public BalanceResponse(Long userId1, Long userId2, BigDecimal balance) {
            this.userId1 = userId1;
            this.userId2 = userId2;
            this.balance = balance;

            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                this.interpretation = "User " + userId1 + " owes User " + userId2 + " $" + balance;
            } else if (balance.compareTo(BigDecimal.ZERO) < 0) {
                this.interpretation = "User " + userId2 + " owes User " + userId1 + " $" + balance.abs();
            } else {
                this.interpretation = "Users are settled (no debt)";
            }
        }

        // Getters
        public Long getUserId1() {
            return userId1;
        }

        public Long getUserId2() {
            return userId2;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public String getInterpretation() {
            return interpretation;
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