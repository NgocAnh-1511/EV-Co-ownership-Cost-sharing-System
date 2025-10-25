package com.example.costpayment.service;

import com.example.costpayment.entity.CostItem;
import com.example.costpayment.entity.CostSplit;
import com.example.costpayment.entity.Payment;
import com.example.costpayment.repository.CostItemRepository;
import com.example.costpayment.repository.CostSplitRepository;
import com.example.costpayment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CostPaymentService {

    private final CostItemRepository costItemRepository;
    private final CostSplitRepository costSplitRepository;
    private final PaymentRepository paymentRepository;

    public CostItem createCostItem(CostItem costItem) {
        return costItemRepository.save(costItem);
    }

    public List<CostItem> getAllCostItems() {
        return costItemRepository.findAll();
    }

    public Optional<CostItem> getCostItemById(Long id) {
        return costItemRepository.findById(id);
    }

    public List<CostItem> getCostItemsByGroup(String groupId) {
        return costItemRepository.findByGroupId(groupId);
    }

    public Optional<CostItem> updateCostItem(Long id, CostItem costItem) {
        return costItemRepository.findById(id)
                .map(existingCost -> {
                    existingCost.setTitle(costItem.getTitle());
                    existingCost.setDescription(costItem.getDescription());
                    existingCost.setTotalAmount(costItem.getTotalAmount());
                    existingCost.setStatus(costItem.getStatus());
                    return costItemRepository.save(existingCost);
                });
    }

    public void deleteCostItem(Long id) {
        costItemRepository.deleteById(id);
    }

    public List<CostSplit> createCostSplits(Long costItemId) {
        CostItem costItem = costItemRepository.findById(costItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cost item not found"));

        // This would typically fetch group members from Group Management Service
        // For now, we'll create a simple split based on ownership percentage
        List<CostSplit> splits = costSplitRepository.findByCostItemId(costItemId);
        
        if (splits.isEmpty()) {
            // Create splits based on group members (this would be fetched from Group Management Service)
            // For demo purposes, we'll create a single split
            CostSplit split = new CostSplit();
            split.setCostItem(costItem);
            split.setUserId("demo-user"); // This would come from Group Management Service
            split.setOwnershipPercentage(100.0);
            split.setSplitAmount(costItem.getTotalAmount());
            splits.add(costSplitRepository.save(split));
        }
        
        return splits;
    }

    public List<CostSplit> getCostSplits(Long costItemId) {
        return costSplitRepository.findByCostItemId(costItemId);
    }

    public Optional<CostSplit> updateCostSplit(Long splitId, CostSplit split) {
        return costSplitRepository.findById(splitId)
                .map(existingSplit -> {
                    existingSplit.setPaidAmount(split.getPaidAmount());
                    existingSplit.setStatus(split.getStatus());
                    return costSplitRepository.save(existingSplit);
                });
    }

    public Payment createPayment(Long splitId, Payment payment) {
        CostSplit split = costSplitRepository.findById(splitId)
                .orElseThrow(() -> new IllegalArgumentException("Cost split not found"));

        payment.setCostItem(split.getCostItem());
        Payment savedPayment = paymentRepository.save(payment);

        // Update split status and paid amount
        BigDecimal newPaidAmount = split.getPaidAmount().add(payment.getAmount());
        split.setPaidAmount(newPaidAmount);
        
        if (newPaidAmount.compareTo(split.getSplitAmount()) >= 0) {
            split.setStatus(CostSplit.SplitStatus.PAID);
        }
        
        costSplitRepository.save(split);
        
        return savedPayment;
    }

    public List<Payment> getPaymentsBySplit(Long splitId) {
        return paymentRepository.findByCostItemId(splitId);
    }

    public List<Payment> getPaymentsByUser(String userId) {
        return paymentRepository.findByUserId(userId);
    }

    public Optional<Payment> updatePayment(Long paymentId, Payment payment) {
        return paymentRepository.findById(paymentId)
                .map(existingPayment -> {
                    existingPayment.setStatus(payment.getStatus());
                    existingPayment.setTransactionId(payment.getTransactionId());
                    return paymentRepository.save(existingPayment);
                });
    }

    public Map<String, Object> getGroupFinancialSummary(String groupId) {
        Map<String, Object> summary = new HashMap<>();
        
        Double totalPaid = costItemRepository.getTotalPaidAmount(groupId);
        Double totalOutstanding = costItemRepository.getTotalOutstandingAmount(groupId);
        
        summary.put("groupId", groupId);
        summary.put("totalPaid", totalPaid != null ? totalPaid : 0.0);
        summary.put("totalOutstanding", totalOutstanding != null ? totalOutstanding : 0.0);
        summary.put("totalCosts", (totalPaid != null ? totalPaid : 0.0) + (totalOutstanding != null ? totalOutstanding : 0.0));
        
        return summary;
    }

    public Map<String, Object> getUserFinancialSummary(String userId) {
        Map<String, Object> summary = new HashMap<>();
        
        Double totalPaid = costSplitRepository.getTotalPaidByUser(userId);
        Double totalOutstanding = costSplitRepository.getTotalOutstandingByUser(userId);
        
        summary.put("userId", userId);
        summary.put("totalPaid", totalPaid != null ? totalPaid : 0.0);
        summary.put("totalOutstanding", totalOutstanding != null ? totalOutstanding : 0.0);
        summary.put("totalCosts", (totalPaid != null ? totalPaid : 0.0) + (totalOutstanding != null ? totalOutstanding : 0.0));
        
        return summary;
    }
}
