package com.example.costpayment.controller;

import com.example.costpayment.entity.Cost;
import com.example.costpayment.entity.CostShare;
import com.example.costpayment.entity.Payment;
import com.example.costpayment.repository.CostRepository;
import com.example.costpayment.repository.CostShareRepository;
import com.example.costpayment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/costs")
@CrossOrigin(origins = "*")
public class CostPaymentController {

    @Autowired
    private CostRepository costRepository;

    @Autowired
    private CostShareRepository costShareRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // Cost endpoints
    @GetMapping
    public List<Cost> getAllCosts() {
        return costRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cost> getCostById(@PathVariable Integer id) {
        Optional<Cost> cost = costRepository.findById(id);
        return cost.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Cost createCost(@RequestBody Cost cost) {
        return costRepository.save(cost);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cost> updateCost(@PathVariable Integer id, @RequestBody Cost costDetails) {
        Optional<Cost> cost = costRepository.findById(id);
        if (cost.isPresent()) {
            Cost existingCost = cost.get();
            existingCost.setVehicleId(costDetails.getVehicleId());
            existingCost.setCostType(costDetails.getCostType());
            existingCost.setAmount(costDetails.getAmount());
            existingCost.setDescription(costDetails.getDescription());
            return ResponseEntity.ok(costRepository.save(existingCost));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCost(@PathVariable Integer id) {
        if (costRepository.existsById(id)) {
            costRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // CostShare endpoints
    @GetMapping("/{costId}/shares")
    public List<CostShare> getCostShares(@PathVariable Integer costId) {
        return costShareRepository.findByCost_CostId(costId);
    }

    @PostMapping("/{costId}/shares")
    public CostShare createCostShare(@PathVariable Integer costId, @RequestBody CostShare costShare) {
        Optional<Cost> cost = costRepository.findById(costId);
        if (cost.isPresent()) {
            costShare.setCost(cost.get());
            return costShareRepository.save(costShare);
        }
        return null;
    }

    // Payment endpoints
    @GetMapping("/payments")
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @PostMapping("/payments")
    public Payment createPayment(@RequestBody Payment payment) {
        return paymentRepository.save(payment);
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Integer id) {
        Optional<Payment> payment = paymentRepository.findById(id);
        return payment.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}