package com.example.costpayment.controller;

import com.example.costpayment.dto.*;
import com.example.costpayment.entity.FundTransaction;
import com.example.costpayment.entity.GroupFund;
import com.example.costpayment.service.FundService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller: Quản lý Quỹ chung
 * Phương án C: Yêu cầu rút tiền + Voting + Phê duyệt
 */
@RestController
@RequestMapping("/api/funds")
@CrossOrigin(origins = "*")
public class FundController {

    private static final Logger logger = LoggerFactory.getLogger(FundController.class);

    @Autowired
    private FundService fundService;

    // ========================================
    // QUẢN LÝ QUỸ
    // ========================================

    /**
     * Lấy thông tin quỹ theo groupId
     * GET /api/funds/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getFundByGroupId(@PathVariable Integer groupId) {
        try {
            return fundService.getFundByGroupId(groupId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error getting fund for groupId={}: {}", groupId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Tạo quỹ mới cho nhóm
     * POST /api/funds/group/{groupId}
     */
    @PostMapping("/group/{groupId}")
    public ResponseEntity<?> createFundForGroup(@PathVariable Integer groupId) {
        try {
            GroupFund fund = fundService.createFundForGroup(groupId);
            return ResponseEntity.status(HttpStatus.CREATED).body(fund);
        } catch (Exception e) {
            logger.error("Error creating fund for groupId={}: {}", groupId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy tổng quan quỹ
     * GET /api/funds/{fundId}/summary
     */
    @GetMapping("/{fundId}/summary")
    public ResponseEntity<?> getFundSummary(@PathVariable Integer fundId) {
        try {
            FundSummaryDto summary = fundService.getFundSummary(fundId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error getting fund summary for fundId={}: {}", fundId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // NẠP TIỀN (USER/ADMIN)
    // ========================================

    /**
     * Nạp tiền vào quỹ
     * POST /api/funds/deposit
     * Body: { fundId, userId, amount, purpose }
     */
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@Valid @RequestBody DepositRequestDto request) {
        try {
            logger.info("Received deposit request: fundId={}, userId={}, amount={}, purpose={}", 
                request.getFundId(), request.getUserId(), request.getAmount(), request.getPurpose());
            
            FundTransaction transaction = fundService.deposit(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Nạp tiền thành công");
            response.put("transaction", transaction);
            
            logger.info("Deposit successful: transactionId={}", transaction.getTransactionId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid deposit request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing deposit: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // RÚT TIỀN - USER (CẦN VOTE)
    // ========================================

    /**
     * Tạo yêu cầu rút tiền (USER)
     * POST /api/funds/withdraw/request
     * Body: { fundId, userId, amount, purpose, receiptUrl? }
     */
    @PostMapping("/withdraw/request")
    public ResponseEntity<?> createWithdrawRequest(@Valid @RequestBody WithdrawRequestDto request) {
        try {
            FundTransaction transaction = fundService.createWithdrawRequest(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Yêu cầu rút tiền đã được tạo. Chờ bỏ phiếu và phê duyệt.");
            response.put("transaction", transaction);
            response.put("status", "Pending");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Invalid withdraw request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating withdraw request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy danh sách yêu cầu đang chờ duyệt
     * GET /api/funds/{fundId}/pending-requests
     */
    @GetMapping("/{fundId}/pending-requests")
    public ResponseEntity<?> getPendingRequests(@PathVariable Integer fundId) {
        try {
            List<FundTransaction> requests = fundService.getPendingRequests(fundId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            logger.error("Error getting pending requests: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // RÚT TIỀN - ADMIN (TRỰC TIẾP)
    // ========================================

    /**
     * Admin rút tiền trực tiếp (không cần vote)
     * POST /api/funds/withdraw/admin
     * Body: { fundId, userId (adminId), amount, purpose, receiptUrl? }
     */
    @PostMapping("/withdraw/admin")
    public ResponseEntity<?> adminDirectWithdraw(@Valid @RequestBody WithdrawRequestDto request) {
        try {
            FundTransaction transaction = fundService.adminDirectWithdraw(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Admin đã rút tiền thành công");
            response.put("transaction", transaction);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Invalid admin withdraw: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing admin withdraw: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // PHÊ DUYỆT YÊU CẦU (ADMIN)
    // ========================================

    /**
     * Admin phê duyệt yêu cầu rút tiền
     * POST /api/funds/withdraw/approve
     * Body: { transactionId, adminId, approved: true, note? }
     */
    @PostMapping("/withdraw/approve")
    public ResponseEntity<?> approveWithdrawRequest(@Valid @RequestBody ApproveRequestDto request) {
        try {
            FundTransaction transaction;
            if (request.getApproved()) {
                transaction = fundService.approveWithdrawRequest(request);
            } else {
                transaction = fundService.rejectWithdrawRequest(request);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", request.getApproved() 
                ? "✅ Yêu cầu đã được phê duyệt" 
                : "❌ Yêu cầu đã bị từ chối");
            response.put("transaction", transaction);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            logger.warn("Cannot approve/reject: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error approving/rejecting request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Admin phê duyệt giao dịch (riêng lẻ)
     * POST /api/funds/transactions/{transactionId}/approve
     */
    @PostMapping("/transactions/{transactionId}/approve")
    public ResponseEntity<?> approveTransaction(@PathVariable Integer transactionId) {
        try {
            ApproveRequestDto request = new ApproveRequestDto();
            request.setTransactionId(transactionId);
            request.setApproved(true);
            request.setAdminId(1); // TODO: Get from session
            
            FundTransaction transaction = fundService.approveWithdrawRequest(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Yêu cầu đã được phê duyệt");
            response.put("transaction", transaction);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            logger.warn("Cannot approve: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error approving transaction: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Admin từ chối giao dịch (riêng lẻ)
     * POST /api/funds/transactions/{transactionId}/reject
     * Body: { reason? }
     */
    @PostMapping("/transactions/{transactionId}/reject")
    public ResponseEntity<?> rejectTransaction(
        @PathVariable Integer transactionId,
        @RequestBody(required = false) Map<String, String> body
    ) {
        try {
            ApproveRequestDto request = new ApproveRequestDto();
            request.setTransactionId(transactionId);
            request.setApproved(false);
            request.setAdminId(1); // TODO: Get from session
            if (body != null && body.containsKey("reason")) {
                request.setNote(body.get("reason"));
            }
            
            FundTransaction transaction = fundService.rejectWithdrawRequest(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "❌ Yêu cầu đã bị từ chối");
            response.put("transaction", transaction);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            logger.warn("Cannot reject: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error rejecting transaction: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // LỊCH SỬ GIAO DỊCH
    // ========================================

    /**
     * Lấy tất cả giao dịch của quỹ
     * GET /api/funds/{fundId}/transactions
     */
    @GetMapping("/{fundId}/transactions")
    public ResponseEntity<?> getAllTransactions(@PathVariable Integer fundId) {
        try {
            List<FundTransaction> transactions = fundService.getAllTransactions(fundId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error getting transactions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy giao dịch của user
     * GET /api/funds/transactions/user/{userId}
     */
    @GetMapping("/transactions/user/{userId}")
    public ResponseEntity<?> getTransactionsByUser(@PathVariable Integer userId) {
        try {
            List<FundTransaction> transactions = fundService.getTransactionsByUser(userId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error getting user transactions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy giao dịch theo loại
     * GET /api/funds/{fundId}/transactions/type/{type}
     * type = Deposit | Withdraw
     */
    @GetMapping("/{fundId}/transactions/type/{type}")
    public ResponseEntity<?> getTransactionsByType(
        @PathVariable Integer fundId,
        @PathVariable String type
    ) {
        try {
            List<FundTransaction> transactions = fundService.getTransactionsByType(fundId, type);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error getting transactions by type: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy giao dịch theo khoảng thời gian
     * GET /api/funds/{fundId}/transactions/range?start=...&end=...
     */
    @GetMapping("/{fundId}/transactions/range")
    public ResponseEntity<?> getTransactionsByDateRange(
        @PathVariable Integer fundId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        try {
            List<FundTransaction> transactions = fundService.getTransactionsByDateRange(fundId, start, end);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error getting transactions by date range: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy chi tiết giao dịch
     * GET /api/funds/transactions/{transactionId}
     */
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<?> getTransactionById(@PathVariable Integer transactionId) {
        try {
            return fundService.getTransactionById(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error getting transaction: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // THỐNG KÊ
    // ========================================

    /**
     * Thống kê quỹ
     * GET /api/funds/{fundId}/statistics
     */
    @GetMapping("/{fundId}/statistics")
    public ResponseEntity<?> getStatistics(@PathVariable Integer fundId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalDeposit", fundService.getTotalDeposit(fundId));
            stats.put("totalWithdraw", fundService.getTotalWithdraw(fundId));
            stats.put("currentBalance", fundService.getCurrentBalance(fundId));
            stats.put("pendingRequests", fundService.countPendingRequests(fundId));
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}

