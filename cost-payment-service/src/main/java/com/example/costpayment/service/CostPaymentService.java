package com.example.costpayment.service;

import com.example.costpayment.entity.Cost;
import com.example.costpayment.entity.CostShare;
import com.example.costpayment.entity.Payment;
import com.example.costpayment.repository.CostRepository;
import com.example.costpayment.repository.CostShareRepository;
import com.example.costpayment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CostPaymentService {

    @Autowired
    private CostRepository costRepository;

    @Autowired
    private CostShareRepository costShareRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // ================================
    // COST MANAGEMENT
    // ================================

    /**
     * Get all costs
     */
    public List<Cost> getAllCosts() {
        return costRepository.findAll();
    }

    /**
     * Get cost by ID
     */
    public Optional<Cost> getCostById(Integer costId) {
        return costRepository.findById(costId);
    }

    /**
     * Create new cost
     */
    public Cost createCost(Cost cost) {
        return costRepository.save(cost);
    }

    /**
     * Update existing cost
     */
    public Cost updateCost(Integer costId, Cost costDetails) {
        Optional<Cost> optionalCost = costRepository.findById(costId);
        if (optionalCost.isPresent()) {
            Cost cost = optionalCost.get();
            cost.setVehicleId(costDetails.getVehicleId());
            cost.setCostType(costDetails.getCostType());
            cost.setAmount(costDetails.getAmount());
            cost.setDescription(costDetails.getDescription());
            return costRepository.save(cost);
        }
        return null;
    }

    /**
     * Delete cost by ID
     */
    public boolean deleteCost(Integer costId) {
        if (costRepository.existsById(costId)) {
            // Delete related cost shares first
            costShareRepository.deleteByCostId(costId);
            costRepository.deleteById(costId);
            return true;
        }
        return false;
    }

    /**
     * Get costs by vehicle ID
     */
    public List<Cost> getCostsByVehicleId(Integer vehicleId) {
        return costRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId);
    }

    /**
     * Get costs by cost type
     */
    public List<Cost> getCostsByCostType(Cost.CostType costType) {
        return costRepository.findByCostTypeOrderByCreatedAtDesc(costType);
    }

    /**
     * Search costs with filters
     */
    public List<Cost> searchCosts(String keyword, Cost.CostType costType, Integer vehicleId) {
        return costRepository.findCostsWithFilters(keyword, costType, vehicleId);
    }

    /**
     * Get cost statistics
     */
    public CostStatistics getCostStatistics() {
        List<Object[]> costTypeSummary = costRepository.getCostTypeSummary();
        
        CostStatistics stats = new CostStatistics();
        
        for (Object[] row : costTypeSummary) {
            Cost.CostType costType = (Cost.CostType) row[0];
            Double totalAmount = (Double) row[1];
            
            switch (costType) {
                case ElectricCharge:
                    stats.setElectricCosts(totalAmount);
                    break;
                case Maintenance:
                    stats.setMaintenanceCosts(totalAmount);
                    break;
                case Insurance:
                    stats.setInsuranceCosts(totalAmount);
                    break;
            }
        }
        
        // Calculate total
        stats.setTotalCosts(stats.getElectricCosts() + stats.getMaintenanceCosts() + stats.getInsuranceCosts());
        
        return stats;
    }

    /**
     * Get cost statistics by vehicle
     */
    public CostStatistics getCostStatisticsByVehicle(Integer vehicleId) {
        Double electricCosts = costRepository.getTotalAmountByVehicleIdAndCostType(vehicleId, Cost.CostType.ElectricCharge);
        Double maintenanceCosts = costRepository.getTotalAmountByVehicleIdAndCostType(vehicleId, Cost.CostType.Maintenance);
        Double insuranceCosts = costRepository.getTotalAmountByVehicleIdAndCostType(vehicleId, Cost.CostType.Insurance);
        
        CostStatistics stats = new CostStatistics();
        stats.setElectricCosts(electricCosts != null ? electricCosts : 0.0);
        stats.setMaintenanceCosts(maintenanceCosts != null ? maintenanceCosts : 0.0);
        stats.setInsuranceCosts(insuranceCosts != null ? insuranceCosts : 0.0);
        stats.setTotalCosts(stats.getElectricCosts() + stats.getMaintenanceCosts() + stats.getInsuranceCosts());
        
        return stats;
    }

    // ================================
    // COST SHARE MANAGEMENT
    // ================================

    /**
     * Create cost share
     */
    public CostShare createCostShare(CostShare costShare) {
        return costShareRepository.save(costShare);
    }

    /**
     * Get cost shares by cost ID
     */
    public List<CostShare> getCostSharesByCostId(Integer costId) {
        return costShareRepository.findByCostIdOrderByCalculatedAtDesc(costId);
    }

    /**
     * Get cost shares by user ID
     */
    public List<CostShare> getCostSharesByUserId(Integer userId) {
        return costShareRepository.findByUserIdOrderByCalculatedAtDesc(userId);
    }

    /**
     * Calculate and create cost shares for a cost
     */
    public List<CostShare> calculateCostShares(Integer costId, List<Integer> userIds, List<Double> percentages) {
        Optional<Cost> optionalCost = costRepository.findById(costId);
        if (!optionalCost.isPresent()) {
            return null;
        }

        Cost cost = optionalCost.get();
        List<CostShare> costShares = new java.util.ArrayList<>();

        for (int i = 0; i < userIds.size() && i < percentages.size(); i++) {
            Double percentage = percentages.get(i);
            Double shareAmount = cost.getAmount() * (percentage / 100.0);

            CostShare costShare = new CostShare();
            costShare.setCostId(costId);
            costShare.setUserId(userIds.get(i));
            costShare.setPercent(percentage);
            costShare.setAmountShare(shareAmount);

            costShares.add(costShareRepository.save(costShare));
        }

        return costShares;
    }

    /**
     * Delete cost shares by cost ID
     */
    public void deleteCostSharesByCostId(Integer costId) {
        costShareRepository.deleteByCostId(costId);
    }

    // ================================
    // PAYMENT MANAGEMENT
    // ================================

    /**
     * Get all payments
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * Get payment by ID
     */
    public Optional<Payment> getPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId);
    }

    /**
     * Create new payment
     */
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    /**
     * Update payment status
     */
    public Payment updatePaymentStatus(Integer paymentId, Payment.PaymentStatus status) {
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            payment.setStatus(status);
            return paymentRepository.save(payment);
        }
        return null;
    }

    /**
     * Get payments by user ID
     */
    public List<Payment> getPaymentsByUserId(Integer userId) {
        return paymentRepository.findByUserIdOrderByPaymentDateDesc(userId);
    }

    /**
     * Get payments by cost ID
     */
    public List<Payment> getPaymentsByCostId(Integer costId) {
        return paymentRepository.findByCostIdOrderByPaymentDateDesc(costId);
    }

    /**
     * Get payments by status
     */
    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByStatusOrderByPaymentDateDesc(status);
    }

    /**
     * Get payment statistics
     */
    public PaymentStatistics getPaymentStatistics() {
        List<Object[]> statusSummary = paymentRepository.getPaymentSummaryByStatus();
        List<Object[]> methodSummary = paymentRepository.getPaymentSummaryByMethod();

        PaymentStatistics stats = new PaymentStatistics();

        for (Object[] row : statusSummary) {
            Payment.PaymentStatus status = (Payment.PaymentStatus) row[0];
            Long count = (Long) row[1];
            Double totalAmount = (Double) row[2];

            switch (status) {
                case Pending:
                    stats.setPendingCount(count);
                    stats.setPendingAmount(totalAmount);
                    break;
                case Completed:
                    stats.setCompletedCount(count);
                    stats.setCompletedAmount(totalAmount);
                    break;
                case Failed:
                    stats.setFailedCount(count);
                    stats.setFailedAmount(totalAmount);
                    break;
            }
        }

        stats.setTotalAmount(stats.getPendingAmount() + stats.getCompletedAmount() + stats.getFailedAmount());

        return stats;
    }

    // ================================
    // STATISTICS CLASSES
    // ================================

    public static class CostStatistics {
        private Double electricCosts = 0.0;
        private Double maintenanceCosts = 0.0;
        private Double insuranceCosts = 0.0;
        private Double totalCosts = 0.0;

        // Getters and Setters
        public Double getElectricCosts() { return electricCosts; }
        public void setElectricCosts(Double electricCosts) { this.electricCosts = electricCosts; }

        public Double getMaintenanceCosts() { return maintenanceCosts; }
        public void setMaintenanceCosts(Double maintenanceCosts) { this.maintenanceCosts = maintenanceCosts; }

        public Double getInsuranceCosts() { return insuranceCosts; }
        public void setInsuranceCosts(Double insuranceCosts) { this.insuranceCosts = insuranceCosts; }

        public Double getTotalCosts() { return totalCosts; }
        public void setTotalCosts(Double totalCosts) { this.totalCosts = totalCosts; }
    }

    public static class PaymentStatistics {
        private Long pendingCount = 0L;
        private Long completedCount = 0L;
        private Long failedCount = 0L;
        private Double pendingAmount = 0.0;
        private Double completedAmount = 0.0;
        private Double failedAmount = 0.0;
        private Double totalAmount = 0.0;

        // Getters and Setters
        public Long getPendingCount() { return pendingCount; }
        public void setPendingCount(Long pendingCount) { this.pendingCount = pendingCount; }

        public Long getCompletedCount() { return completedCount; }
        public void setCompletedCount(Long completedCount) { this.completedCount = completedCount; }

        public Long getFailedCount() { return failedCount; }
        public void setFailedCount(Long failedCount) { this.failedCount = failedCount; }

        public Double getPendingAmount() { return pendingAmount; }
        public void setPendingAmount(Double pendingAmount) { this.pendingAmount = pendingAmount; }

        public Double getCompletedAmount() { return completedAmount; }
        public void setCompletedAmount(Double completedAmount) { this.completedAmount = completedAmount; }

        public Double getFailedAmount() { return failedAmount; }
        public void setFailedAmount(Double failedAmount) { this.failedAmount = failedAmount; }

        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    }
}
