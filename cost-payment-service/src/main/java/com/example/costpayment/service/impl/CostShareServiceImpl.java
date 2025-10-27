package com.example.costpayment.service.impl;

import com.example.costpayment.entity.Cost;
import com.example.costpayment.entity.CostShare;
import com.example.costpayment.repository.CostRepository;
import com.example.costpayment.repository.CostShareRepository;
import com.example.costpayment.service.CostShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CostShareServiceImpl implements CostShareService {

    @Autowired
    private CostRepository costRepository;

    @Autowired
    private CostShareRepository costShareRepository;

    public CostShare createCostShare(Integer costId, CostShare costShare) {
        costShare.setCostId(costId);
        return costShareRepository.save(costShare);
    }

    public List<CostShare> getCostSharesByCostId(Integer costId) {
        return costShareRepository.findByCostId(costId);
    }

    public List<CostShare> calculateCostShares(Integer costId, List<Integer> userIds, List<Double> percentages) {
        Cost cost = costRepository.findById(costId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi phí ID: " + costId));

        List<CostShare> shares = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            CostShare share = new CostShare(costId, userIds.get(i), percentages.get(i),
                    cost.getAmount() * (percentages.get(i) / 100));
            shares.add(costShareRepository.save(share));
        }
        return shares;
    }

    // Additional methods for UI Service compatibility
    public List<CostShare> getAllCostShares() {
        return costShareRepository.findAll();
    }

    public CostShare getCostShareById(Integer id) {
        Optional<CostShare> costShare = costShareRepository.findById(id);
        return costShare.orElse(null);
    }

    public CostShare updateCostShare(Integer id, CostShare costShare) {
        Optional<CostShare> existingCostShare = costShareRepository.findById(id);
        if (existingCostShare.isPresent()) {
            costShare.setShareId(id);
            return costShareRepository.save(costShare);
        }
        return null;
    }

    public void deleteCostShare(Integer id) {
        costShareRepository.deleteById(id);
    }
}
