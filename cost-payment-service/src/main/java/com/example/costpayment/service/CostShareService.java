package com.example.costpayment.service;

import com.example.costpayment.entity.CostShare;
import java.util.List;

public interface CostShareService {
    CostShare createCostShare(Integer costId, CostShare costShare);
    List<CostShare> getCostSharesByCostId(Integer costId);
    List<CostShare> calculateCostShares(Integer costId, List<Integer> userIds, List<Double> percentages);
    
    // Additional methods for UI Service compatibility
    List<CostShare> getAllCostShares();
    CostShare getCostShareById(Integer id);
    CostShare updateCostShare(Integer id, CostShare costShare);
    void deleteCostShare(Integer id);
}
