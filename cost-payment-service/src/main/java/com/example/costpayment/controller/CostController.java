package com.example.costpayment.controller;

import com.example.costpayment.entity.Cost;
import com.example.costpayment.entity.CostShare;
import com.example.costpayment.entity.Payment;
import com.example.costpayment.service.CostPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/costs")
@CrossOrigin(origins = "*")
public class CostController {

    @Autowired
    private CostPaymentService costPaymentService;

    // ================================
    // COST ENDPOINTS
    // ================================

    /**
     * Get all costs
     */
    @GetMapping
    public ResponseEntity<List<Cost>> getAllCosts() {
        List<Cost> costs = costPaymentService.getAllCosts();
        return ResponseEntity.ok(costs);
    }

    /**
     * Get cost by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Cost> getCostById(@PathVariable Integer id) {
        Optional<Cost> cost = costPaymentService.getCostById(id);
        return cost.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new cost
     */
    @PostMapping
    public ResponseEntity<Cost> createCost(@RequestBody Cost cost) {
        Cost createdCost = costPaymentService.createCost(cost);
        return ResponseEntity.ok(createdCost);
    }

    /**
     * Update cost
     */
    @PutMapping("/{id}")
    public ResponseEntity<Cost> updateCost(@PathVariable Integer id, @RequestBody Cost cost) {
        Cost updatedCost = costPaymentService.updateCost(id, cost);
        if (updatedCost != null) {
            return ResponseEntity.ok(updatedCost);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Delete cost
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCost(@PathVariable Integer id) {
        boolean deleted = costPaymentService.deleteCost(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get costs by vehicle ID
     */
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<Cost>> getCostsByVehicleId(@PathVariable Integer vehicleId) {
        List<Cost> costs = costPaymentService.getCostsByVehicleId(vehicleId);
        return ResponseEntity.ok(costs);
    }

    /**
     * Get costs by cost type
     */
    @GetMapping("/type/{costType}")
    public ResponseEntity<List<Cost>> getCostsByCostType(@PathVariable String costType) {
        try {
            Cost.CostType type = Cost.CostType.valueOf(costType);
            List<Cost> costs = costPaymentService.getCostsByCostType(type);
            return ResponseEntity.ok(costs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Search costs with filters
     */
    @GetMapping("/search")
    public ResponseEntity<List<Cost>> searchCosts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String costType,
            @RequestParam(required = false) Integer vehicleId) {
        
        Cost.CostType type = null;
        if (costType != null && !costType.isEmpty()) {
            try {
                type = Cost.CostType.valueOf(costType);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        List<Cost> costs = costPaymentService.searchCosts(query, type, vehicleId);
        return ResponseEntity.ok(costs);
    }

    /**
     * Get cost statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<CostPaymentService.CostStatistics> getCostStatistics() {
        CostPaymentService.CostStatistics stats = costPaymentService.getCostStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get cost statistics by vehicle
     */
    @GetMapping("/statistics/vehicle/{vehicleId}")
    public ResponseEntity<CostPaymentService.CostStatistics> getCostStatisticsByVehicle(@PathVariable Integer vehicleId) {
        CostPaymentService.CostStatistics stats = costPaymentService.getCostStatisticsByVehicle(vehicleId);
        return ResponseEntity.ok(stats);
    }

    // ================================
    // COST SHARE ENDPOINTS
    // ================================

    /**
     * Create cost share
     */
    @PostMapping("/{costId}/shares")
    public ResponseEntity<CostShare> createCostShare(@PathVariable Integer costId, @RequestBody CostShare costShare) {
        costShare.setCostId(costId);
        CostShare createdShare = costPaymentService.createCostShare(costShare);
        return ResponseEntity.ok(createdShare);
    }

    /**
     * Get cost shares by cost ID
     */
    @GetMapping("/{costId}/shares")
    public ResponseEntity<List<CostShare>> getCostSharesByCostId(@PathVariable Integer costId) {
        List<CostShare> shares = costPaymentService.getCostSharesByCostId(costId);
        return ResponseEntity.ok(shares);
    }

    /**
     * Calculate cost shares for a cost
     */
    @PostMapping("/{costId}/calculate-shares")
    public ResponseEntity<List<CostShare>> calculateCostShares(
            @PathVariable Integer costId,
            @RequestBody CostShareRequest request) {
        
        List<CostShare> shares = costPaymentService.calculateCostShares(
            costId, request.getUserIds(), request.getPercentages());
        
        if (shares != null) {
            return ResponseEntity.ok(shares);
        }
        return ResponseEntity.notFound().build();
    }

    // ================================
    // PAYMENT ENDPOINTS
    // ================================

    /**
     * Get all payments
     */
    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = costPaymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Integer paymentId) {
        Optional<Payment> payment = costPaymentService.getPaymentById(paymentId);
        return payment.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new payment
     */
    @PostMapping("/payments")
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        Payment createdPayment = costPaymentService.createPayment(payment);
        return ResponseEntity.ok(createdPayment);
    }

    /**
     * Update payment status
     */
    @PutMapping("/payments/{paymentId}/status")
    public ResponseEntity<Payment> updatePaymentStatus(
            @PathVariable Integer paymentId,
            @RequestParam String status) {
        
        try {
            Payment.PaymentStatus paymentStatus = Payment.PaymentStatus.valueOf(status);
            Payment updatedPayment = costPaymentService.updatePaymentStatus(paymentId, paymentStatus);
            if (updatedPayment != null) {
                return ResponseEntity.ok(updatedPayment);
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get payments by user ID
     */
    @GetMapping("/payments/user/{userId}")
    public ResponseEntity<List<Payment>> getPaymentsByUserId(@PathVariable Integer userId) {
        List<Payment> payments = costPaymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payments by cost ID
     */
    @GetMapping("/{costId}/payments")
    public ResponseEntity<List<Payment>> getPaymentsByCostId(@PathVariable Integer costId) {
        List<Payment> payments = costPaymentService.getPaymentsByCostId(costId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payment statistics
     */
    @GetMapping("/payments/statistics")
    public ResponseEntity<CostPaymentService.PaymentStatistics> getPaymentStatistics() {
        CostPaymentService.PaymentStatistics stats = costPaymentService.getPaymentStatistics();
        return ResponseEntity.ok(stats);
    }

    // ================================
    // REQUEST DTOs
    // ================================

    public static class CostShareRequest {
        private List<Integer> userIds;
        private List<Double> percentages;

        // Getters and Setters
        public List<Integer> getUserIds() { return userIds; }
        public void setUserIds(List<Integer> userIds) { this.userIds = userIds; }

        public List<Double> getPercentages() { return percentages; }
        public void setPercentages(List<Double> percentages) { this.percentages = percentages; }
    }
}
