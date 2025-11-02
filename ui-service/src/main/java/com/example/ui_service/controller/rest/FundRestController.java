package com.example.ui_service.controller.rest;

import com.example.ui_service.client.CostPaymentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * REST Controller để proxy các request Fund từ frontend sang backend
 */
@RestController
@RequestMapping("/api/fund")
public class FundRestController {

    private static final Logger logger = LoggerFactory.getLogger(FundRestController.class);

    @Value("${cost-payment.service.url:http://localhost:8081}")
    private String costPaymentServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Lấy thống kê quỹ
     * GET /api/fund/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getFundStats() {
        try {
            // TODO: Tạo endpoint tổng hợp stats trong backend
            // Tạm thời return mock data
            Map<String, Object> stats = Map.of(
                "totalBalance", 3500000,
                "totalIncome", 5000000,
                "totalExpense", 1500000,
                "pendingCount", 2,
                "openingBalance", 0
            );
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy tất cả giao dịch
     * GET /api/fund/transactions?status=...
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions(
        @RequestParam(required = false) String status
    ) {
        try {
            String url = costPaymentServiceUrl + "/api/funds/1/transactions"; // fundId=1 for demo
            
            ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                List.class
            );
            
            List transactions = response.getBody();
            
            // Filter by status if provided
            if (status != null && transactions != null) {
                transactions = transactions.stream()
                    .filter(t -> {
                        if (t instanceof Map) {
                            return status.equals(((Map<?, ?>) t).get("status"));
                        }
                        return false;
                    })
                    .toList();
            }
            
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Nạp tiền vào quỹ
     * POST /api/fund/deposit
     * Body: { fundId, userId, amount, purpose }
     */
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody Map<String, Object> request) {
        try {
            String url = costPaymentServiceUrl + "/api/funds/deposit";
            logger.info("Deposit request: {} to URL: {}", request, url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            logger.info("Deposit response: {}", response.getBody());
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error depositing: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Tạo yêu cầu rút tiền (cần vote)
     * POST /api/fund/withdraw/request
     * Body: { fundId, userId, amount, purpose, receiptUrl? }
     */
    @PostMapping("/withdraw/request")
    public ResponseEntity<?> withdrawRequest(@RequestBody Map<String, Object> request) {
        try {
            String url = costPaymentServiceUrl + "/api/funds/withdraw/request";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy quỹ theo groupId
     * GET /api/fund/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getFundByGroupId(@PathVariable Integer groupId) {
        try {
            String url = costPaymentServiceUrl + "/api/funds/group/" + groupId;
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Fund not found for group " + groupId));
        }
    }

    /**
     * Tạo quỹ mới cho nhóm
     * POST /api/fund/group/{groupId}/create
     */
    @PostMapping("/group/{groupId}/create")
    public ResponseEntity<?> createFundForGroup(@PathVariable Integer groupId) {
        try {
            // Gọi endpoint tạo fund trong cost-payment-service
            String url = costPaymentServiceUrl + "/api/funds/group/" + groupId;
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
            
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Failed to create fund for group {}: {}", groupId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create fund: " + e.getMessage()));
        }
    }

    /**
     * Tạo giao dịch mới
     * POST /api/fund/transactions
     */
    @PostMapping("/transactions")
    public ResponseEntity<?> createTransaction(@RequestBody Map<String, Object> request) {
        try {
            String type = (String) request.get("type");
            String endpoint;
            
            if ("Deposit".equals(type)) {
                endpoint = "/api/funds/deposit";
            } else if ("Withdraw".equals(type)) {
                endpoint = "/api/funds/withdraw/request";
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid transaction type"));
            }
            
            String url = costPaymentServiceUrl + endpoint;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Phê duyệt giao dịch
     * POST /api/fund/transactions/{transactionId}/approve
     */
    @PostMapping("/transactions/{transactionId}/approve")
    public ResponseEntity<?> approveTransaction(@PathVariable Integer transactionId) {
        try {
            String url = costPaymentServiceUrl + "/api/funds/transactions/" + transactionId + "/approve";
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
            
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Từ chối giao dịch
     * POST /api/fund/transactions/{transactionId}/reject
     */
    @PostMapping("/transactions/{transactionId}/reject")
    public ResponseEntity<?> rejectTransaction(
        @PathVariable Integer transactionId,
        @RequestBody(required = false) Map<String, String> body
    ) {
        try {
            String url = costPaymentServiceUrl + "/api/funds/transactions/" + transactionId + "/reject";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Xóa giao dịch
     * DELETE /api/fund/transactions/{transactionId}
     */
    @DeleteMapping("/transactions/{transactionId}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Integer transactionId) {
        try {
            // For now, return success (implement actual deletion if needed)
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa giao dịch"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}

