package com.example.costpayment.controller;

import com.example.costpayment.entity.Payment;
import com.example.costpayment.entity.PaymentStatus;
import com.example.costpayment.service.impl.PaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Payment Management
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentServiceImpl paymentService;

    /**
     * Get all payments
     * GET /api/payments
     */
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payment by ID
     * GET /api/payments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Integer id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        return payment.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get payments by user ID
     * GET /api/payments/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getPaymentsByUserId(@PathVariable Integer userId) {
        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get pending payments by user ID
     * GET /api/payments/user/{userId}/pending
     */
    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<List<Payment>> getPendingPaymentsByUserId(@PathVariable Integer userId) {
        List<Payment> payments = paymentService.getPendingPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payment history by user ID (completed payments)
     * GET /api/payments/user/{userId}/history
     */
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<Payment>> getPaymentHistoryByUserId(@PathVariable Integer userId) {
        List<Payment> payments = paymentService.getPaymentHistoryByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payments by cost ID
     * GET /api/payments/cost/{costId}
     */
    @GetMapping("/cost/{costId}")
    public ResponseEntity<List<Payment>> getPaymentsByCostId(@PathVariable Integer costId) {
        List<Payment> payments = paymentService.getPaymentsByCostId(costId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Create new payment
     * POST /api/payments
     */
    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        try {
            Payment createdPayment = paymentService.createPayment(payment);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
        } catch (Exception e) {
            System.err.println("Error creating payment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update payment status
     * PUT /api/payments/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Payment> updatePaymentStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        
        String status = request.get("status");
        if (status == null || status.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Optional<Payment> updatedPayment = paymentService.updatePaymentStatus(id, status);
        return updatedPayment.map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Process payment (mark as PAID)
     * POST /api/payments/{id}/process
     */
    @PostMapping("/{id}/process")
    public ResponseEntity<Map<String, Object>> processPayment(@PathVariable Integer id) {
        try {
            Optional<Payment> paymentOpt = paymentService.updatePaymentStatus(id, "PAID");
            
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Thanh toán thành công",
                    "payment", payment,
                    "transactionCode", payment.getTransactionCode()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy thanh toán"
                ));
            }
        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Lỗi khi xử lý thanh toán: " + e.getMessage()
            ));
        }
    }

    /**
     * Confirm payment with QR code
     * POST /api/payments/{id}/confirm
     * Body: { userId, method, transactionCode }
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmPayment(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        try {
            // Get payment
            Optional<Payment> paymentOpt = paymentService.getPaymentById(id);
            
            if (!paymentOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy thanh toán"
                ));
            }
            
            Payment payment = paymentOpt.get();
            
            // Update payment information
            String method = (String) request.get("method");
            String transactionCode = (String) request.get("transactionCode");
            
            if (method != null) {
                try {
                    payment.setMethod(Payment.Method.valueOf(method));
                } catch (IllegalArgumentException e) {
                    // If invalid method, ignore or use default
                    System.out.println("Invalid payment method: " + method);
                }
            }
            
            if (transactionCode != null) {
                payment.setTransactionCode(transactionCode);
            }
            
            // Mark as PAID
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaymentDate(java.time.LocalDateTime.now());
            
            // Save payment
            Payment updatedPayment = paymentService.createPayment(payment);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Xác nhận thanh toán thành công",
                "payment", updatedPayment,
                "transactionCode", updatedPayment.getTransactionCode()
            ));
            
        } catch (Exception e) {
            System.err.println("Error confirming payment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Lỗi khi xác nhận thanh toán: " + e.getMessage()
            ));
        }
    }
}

