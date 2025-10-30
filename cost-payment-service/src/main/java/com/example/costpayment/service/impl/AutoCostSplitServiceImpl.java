package com.example.costpayment.service.impl;

import com.example.costpayment.entity.Cost;
import com.example.costpayment.entity.CostShare;
import com.example.costpayment.entity.SplitMethod;
import com.example.costpayment.entity.UsageTracking;
import com.example.costpayment.repository.CostRepository;
import com.example.costpayment.repository.CostShareRepository;
import com.example.costpayment.repository.UsageTrackingRepository;
import com.example.costpayment.service.AutoCostSplitService;
import com.example.costpayment.service.CostShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Service tự động chia chi phí theo các phương thức khác nhau
 */
@Service
public class AutoCostSplitServiceImpl implements AutoCostSplitService {

    @Autowired
    private CostRepository costRepository;

    @Autowired
    private CostShareRepository costShareRepository;

    @Autowired
    private UsageTrackingRepository usageTrackingRepository;

    @Autowired
    private CostShareService costShareService;

    @Autowired(required = false)
    private RestTemplate restTemplate;

    /**
     * Tự động chia chi phí dựa trên split method
     */
    @Override
    @Transactional
    public List<CostShare> autoSplitCost(Integer costId, Integer groupId, Integer month, Integer year) {
        // Lấy thông tin chi phí
        Cost cost = costRepository.findById(costId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi phí ID: " + costId));

        // Xác định split method (nếu có trong entity hoặc dựa vào costType)
        SplitMethod splitMethod = determineSplitMethod(cost);

        // Chia chi phí theo phương thức tương ứng
        switch (splitMethod) {
            case BY_OWNERSHIP:
                Map<Integer, Double> ownershipMap = getGroupOwnership(groupId);
                return splitByOwnership(costId, ownershipMap);

            case BY_USAGE:
                Map<Integer, Double> usageMap = getGroupUsageKm(groupId, month, year);
                return splitByUsage(costId, usageMap);

            case EQUAL:
                List<Integer> userIds = getUserIdsByGroup(groupId);
                return splitEqually(costId, userIds);

            case CUSTOM:
                throw new RuntimeException("CUSTOM split method requires manual input");

            default:
                throw new RuntimeException("Unknown split method: " + splitMethod);
        }
    }

    /**
     * Tự động chia chi phí với splitMethod được chỉ định (từ form)
     */
    @Override
    @Transactional
    public List<CostShare> autoSplitCostWithMethod(Integer costId, Integer groupId, String splitMethodStr, Integer month, Integer year) {
        System.out.println("=== AUTO SPLIT WITH METHOD ===");
        System.out.println("Cost ID: " + costId);
        System.out.println("Group ID: " + groupId);
        System.out.println("Split Method: " + splitMethodStr);
        System.out.println("Month/Year: " + month + "/" + year);

        // Validate cost exists
        Cost cost = costRepository.findById(costId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi phí ID: " + costId));

        // Parse split method từ String
        SplitMethod splitMethod;
        try {
            splitMethod = SplitMethod.valueOf(splitMethodStr);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Split method không hợp lệ: " + splitMethodStr);
        }

        // Chia chi phí theo phương thức tương ứng
        switch (splitMethod) {
            case BY_OWNERSHIP:
                System.out.println("Splitting by OWNERSHIP...");
                Map<Integer, Double> ownershipMap = getGroupOwnership(groupId);
                System.out.println("Ownership map: " + ownershipMap);
                return splitByOwnership(costId, ownershipMap);

            case BY_USAGE:
                System.out.println("Splitting by USAGE...");
                Map<Integer, Double> usageMap = getGroupUsageKm(groupId, month, year);
                System.out.println("Usage map: " + usageMap);
                return splitByUsage(costId, usageMap);

            case EQUAL:
                System.out.println("Splitting EQUALLY...");
                List<Integer> userIds = getUserIdsByGroup(groupId);
                System.out.println("User IDs: " + userIds);
                return splitEqually(costId, userIds);

            case CUSTOM:
                throw new RuntimeException("CUSTOM split method requires manual input");

            default:
                throw new RuntimeException("Unknown split method: " + splitMethod);
        }
    }

    /**
     * Chia chi phí theo tỉ lệ sở hữu
     */
    @Override
    @Transactional
    public List<CostShare> splitByOwnership(Integer costId, Map<Integer, Double> ownershipMap) {
        Cost cost = costRepository.findById(costId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi phí ID: " + costId));

        // Validate tổng ownership = 100%
        double totalOwnership = ownershipMap.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(totalOwnership - 100.0) > 0.01) {
            throw new RuntimeException("Tổng ownership phải bằng 100%, hiện tại: " + totalOwnership + "%");
        }

        // Xóa các chia sẻ cũ
        deletePreviousShares(costId);

        // Tạo danh sách userId và % từ map
        List<Integer> userIds = new ArrayList<>(ownershipMap.keySet());
        List<Double> percentages = new ArrayList<>(ownershipMap.values());

        // Sử dụng service có sẵn
        return costShareService.calculateCostShares(costId, userIds, percentages);
    }

    /**
     * Chia chi phí theo mức độ sử dụng (km)
     */
    @Override
    @Transactional
    public List<CostShare> splitByUsage(Integer costId, Map<Integer, Double> usageMap) {
        Cost cost = costRepository.findById(costId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi phí ID: " + costId));

        // Tính tổng km
        double totalKm = usageMap.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalKm <= 0) {
            throw new RuntimeException("Tổng km phải lớn hơn 0");
        }

        // Tính % cho mỗi user
        Map<Integer, Double> percentageMap = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : usageMap.entrySet()) {
            double percent = (entry.getValue() / totalKm) * 100.0;
            percentageMap.put(entry.getKey(), percent);
        }

        // Xóa các chia sẻ cũ
        deletePreviousShares(costId);

        // Tạo danh sách userId và %
        List<Integer> userIds = new ArrayList<>(percentageMap.keySet());
        List<Double> percentages = new ArrayList<>(percentageMap.values());

        // Sử dụng service có sẵn
        return costShareService.calculateCostShares(costId, userIds, percentages);
    }

    /**
     * Chia đều chi phí
     */
    @Override
    @Transactional
    public List<CostShare> splitEqually(Integer costId, List<Integer> userIds) {
        Cost cost = costRepository.findById(costId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi phí ID: " + costId));

        if (userIds == null || userIds.isEmpty()) {
            throw new RuntimeException("Danh sách user không được rỗng");
        }

        // Xóa các chia sẻ cũ
        deletePreviousShares(costId);

        // Tính % đều cho mỗi người
        double percentPerPerson = 100.0 / userIds.size();
        List<Double> percentages = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            percentages.add(percentPerPerson);
        }

        // Sử dụng service có sẵn
        return costShareService.calculateCostShares(costId, userIds, percentages);
    }

    /**
     * Lấy ownership % của nhóm
     * TODO: Call Group Management Service để lấy ownership thực tế
     */
    @Override
    public Map<Integer, Double> getGroupOwnership(Integer groupId) {
        // Mock data - Trong thực tế sẽ call Group Management Service
        Map<Integer, Double> ownershipMap = new HashMap<>();
        
        if (groupId == 1) {
            ownershipMap.put(1, 50.0);
            ownershipMap.put(2, 30.0);
            ownershipMap.put(3, 20.0);
        } else if (groupId == 2) {
            ownershipMap.put(2, 60.0);
            ownershipMap.put(4, 40.0);
        }

        return ownershipMap;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Xác định split method dựa vào cost hoặc cost type
     */
    private SplitMethod determineSplitMethod(Cost cost) {
        // Nếu cost có splitMethod field (cần thêm vào Cost entity)
        // return cost.getSplitMethod();

        // Tạm thời dựa vào costType
        switch (cost.getCostType()) {
            case ElectricCharge:
                return SplitMethod.BY_USAGE;
            case Maintenance:
            case Insurance:
            case Inspection:
                return SplitMethod.BY_OWNERSHIP;
            case Cleaning:
            case Other:
                return SplitMethod.EQUAL;
            default:
                return SplitMethod.BY_OWNERSHIP;
        }
    }

    /**
     * Lấy km của nhóm trong tháng
     */
    private Map<Integer, Double> getGroupUsageKm(Integer groupId, Integer month, Integer year) {
        List<UsageTracking> usageList = usageTrackingRepository
                .findByGroupIdAndMonthAndYear(groupId, month, year);

        Map<Integer, Double> usageMap = new HashMap<>();
        for (UsageTracking usage : usageList) {
            usageMap.put(usage.getUserId(), usage.getKmDriven());
        }

        if (usageMap.isEmpty()) {
            throw new RuntimeException("Không có dữ liệu km cho nhóm " + groupId + 
                                     " trong tháng " + month + "/" + year);
        }

        return usageMap;
    }

    /**
     * Lấy danh sách userId của nhóm
     */
    private List<Integer> getUserIdsByGroup(Integer groupId) {
        // Mock data - Trong thực tế call Group Management Service
        List<Integer> userIds = new ArrayList<>();
        
        if (groupId == 1) {
            userIds.addAll(Arrays.asList(1, 2, 3));
        } else if (groupId == 2) {
            userIds.addAll(Arrays.asList(2, 4));
        }

        return userIds;
    }

    /**
     * Xóa các chia sẻ cũ của cost
     */
    private void deletePreviousShares(Integer costId) {
        List<CostShare> existingShares = costShareRepository.findByCostId(costId);
        if (!existingShares.isEmpty()) {
            costShareRepository.deleteAll(existingShares);
        }
    }
}

