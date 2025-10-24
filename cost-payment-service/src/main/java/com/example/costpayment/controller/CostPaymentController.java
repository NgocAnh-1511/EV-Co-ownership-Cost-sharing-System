package com.example.costpayment.controller;

import com.example.costpayment.entity.CostItem;
import com.example.costpayment.entity.CostSplit;
import com.example.costpayment.entity.Payment;
import com.example.costpayment.service.CostPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/costs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CostPaymentController {

    private final CostPaymentService costPaymentService;

    // Cost Item Management APIs
    @PostMapping
    public ResponseEntity<CostItem> createCostItem(@RequestBody CostItem costItem) {
        CostItem createdCost = costPaymentService.createCostItem(costItem);
        return ResponseEntity.ok(createdCost);
    }

    @GetMapping
    public ResponseEntity<List<CostItem>> getAllCostItems() {
        List<CostItem> costItems = costPaymentService.getAllCostItems();
        return ResponseEntity.ok(costItems);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CostItem> getCostItemById(@PathVariable Long id) {
        return costPaymentService.getCostItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<CostItem>> getCostItemsByGroup(@PathVariable String groupId) {
        List<CostItem> costItems = costPaymentService.getCostItemsByGroup(groupId);
        return ResponseEntity.ok(costItems);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CostItem> updateCostItem(@PathVariable Long id, @RequestBody CostItem costItem) {
        return costPaymentService.updateCostItem(id, costItem)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCostItem(@PathVariable Long id) {
        costPaymentService.deleteCostItem(id);
        return ResponseEntity.ok().build();
    }

    // Cost Split Management APIs
    @PostMapping("/{costItemId}/splits")
    public ResponseEntity<List<CostSplit>> createCostSplits(@PathVariable Long costItemId) {
        List<CostSplit> splits = costPaymentService.createCostSplits(costItemId);
        return ResponseEntity.ok(splits);
    }

    @GetMapping("/{costItemId}/splits")
    public ResponseEntity<List<CostSplit>> getCostSplits(@PathVariable Long costItemId) {
        List<CostSplit> splits = costPaymentService.getCostSplits(costItemId);
        return ResponseEntity.ok(splits);
    }

    @PutMapping("/splits/{splitId}")
    public ResponseEntity<CostSplit> updateCostSplit(@PathVariable Long splitId, @RequestBody CostSplit split) {
        return costPaymentService.updateCostSplit(splitId, split)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Payment Management APIs
    @PostMapping("/splits/{splitId}/payments")
    public ResponseEntity<Payment> createPayment(@PathVariable Long splitId, @RequestBody Payment payment) {
        Payment createdPayment = costPaymentService.createPayment(splitId, payment);
        return ResponseEntity.ok(createdPayment);
    }

    @GetMapping("/splits/{splitId}/payments")
    public ResponseEntity<List<Payment>> getPaymentsBySplit(@PathVariable Long splitId) {
        List<Payment> payments = costPaymentService.getPaymentsBySplit(splitId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/user/{userId}/payments")
    public ResponseEntity<List<Payment>> getPaymentsByUser(@PathVariable String userId) {
        List<Payment> payments = costPaymentService.getPaymentsByUser(userId);
        return ResponseEntity.ok(payments);
    }

    @PutMapping("/payments/{paymentId}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long paymentId, @RequestBody Payment payment) {
        return costPaymentService.updatePayment(paymentId, payment)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Financial Summary APIs
    @GetMapping("/group/{groupId}/summary")
    public ResponseEntity<?> getGroupFinancialSummary(@PathVariable String groupId) {
        return ResponseEntity.ok(costPaymentService.getGroupFinancialSummary(groupId));
    }

    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<?> getUserFinancialSummary(@PathVariable String userId) {
        return ResponseEntity.ok(costPaymentService.getUserFinancialSummary(userId));
    }
}
