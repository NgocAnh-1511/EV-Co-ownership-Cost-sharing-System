package com.example.costpayment.controller;

import com.example.costpayment.entity.Cost;
import com.example.costpayment.entity.CostShare;
import com.example.costpayment.service.AutoCostSplitService;
import com.example.costpayment.service.CostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API cho Auto Split - Tự động chia chi phí
 */
@RestController
@RequestMapping("/api/auto-split")
@CrossOrigin(origins = "*")
public class AutoSplitController {

    @Autowired
    private AutoCostSplitService autoSplitService;

    @Autowired
    private CostService costService;

    /**
     * Tự động chia chi phí
     * POST /api/auto-split/cost/{costId}
     */
    @PostMapping("/cost/{costId}")
    public ResponseEntity<Map<String, Object>> autoSplitCost(
            @PathVariable Integer costId,
            @RequestParam Integer groupId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        
        // Lấy tháng/năm hiện tại nếu không có
        if (month == null) {
            month = java.time.LocalDate.now().getMonthValue();
        }
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }

        // Thực hiện auto split
        List<CostShare> shares = autoSplitService.autoSplitCost(costId, groupId, month, year);

        // Trả về kết quả
        Map<String, Object> result = new HashMap<>();
        result.put("costId", costId);
        result.put("totalShares", shares.size());
        result.put("shares", shares);
        result.put("message", "Đã tự động chia chi phí thành công!");

        return ResponseEntity.ok(result);
    }

    /**
     * Tạo chi phí MỚI và tự động chia luôn
     * POST /api/auto-split/create-and-split
     */
    @PostMapping("/create-and-split")
    public ResponseEntity<Map<String, Object>> createAndAutoSplit(
            @RequestBody Map<String, Object> request) {
        
        try {
            // Parse request
            Integer vehicleId = (Integer) request.get("vehicleId");
            String costType = (String) request.get("costType");
            Double amount = ((Number) request.get("amount")).doubleValue();
            String description = (String) request.get("description");
            String splitMethod = (String) request.get("splitMethod"); // QUAN TRỌNG: Nhận splitMethod từ form
            Integer groupId = (Integer) request.get("groupId");
            
            // Lấy tháng/năm (mặc định là hiện tại nếu không có)
            Integer month = request.get("month") != null ? 
                (Integer) request.get("month") : 
                java.time.LocalDate.now().getMonthValue();
            Integer year = request.get("year") != null ? 
                (Integer) request.get("year") : 
                java.time.LocalDate.now().getYear();

            System.out.println("=== AUTO SPLIT REQUEST ===");
            System.out.println("Vehicle ID: " + vehicleId);
            System.out.println("Cost Type: " + costType);
            System.out.println("Amount: " + amount);
            System.out.println("Split Method: " + splitMethod);
            System.out.println("Group ID: " + groupId);
            System.out.println("Month/Year: " + month + "/" + year);

            // Tạo cost mới
            Cost cost = new Cost();
            cost.setVehicleId(vehicleId);
            cost.setCostType(Cost.CostType.valueOf(costType));
            cost.setAmount(amount);
            cost.setDescription(description);
            
            Cost savedCost = costService.createCost(cost);
            System.out.println("Created Cost ID: " + savedCost.getCostId());

            // Tự động chia CHI PHÍ theo splitMethod
            List<CostShare> shares = autoSplitService.autoSplitCostWithMethod(
                savedCost.getCostId(), 
                groupId, 
                splitMethod, 
                month, 
                year
            );

            System.out.println("Created " + shares.size() + " cost shares");

            // Trả về kết quả
            Map<String, Object> result = new HashMap<>();
            result.put("cost", savedCost);
            result.put("shares", shares);
            result.put("message", "Đã tạo chi phí và tự động chia thành công!");

            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Lỗi khi tạo và chia chi phí: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Chia theo sở hữu
     * POST /api/auto-split/by-ownership
     */
    @PostMapping("/by-ownership")
    public ResponseEntity<List<CostShare>> splitByOwnership(
            @RequestParam Integer costId,
            @RequestParam Integer groupId) {
        
        Map<Integer, Double> ownershipMap = autoSplitService.getGroupOwnership(groupId);
        List<CostShare> shares = autoSplitService.splitByOwnership(costId, ownershipMap);
        
        return ResponseEntity.ok(shares);
    }

    /**
     * Chia theo km (usage)
     * POST /api/auto-split/by-usage
     */
    @PostMapping("/by-usage")
    public ResponseEntity<List<CostShare>> splitByUsage(
            @RequestParam Integer costId,
            @RequestParam Integer groupId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        
        // Lấy km từ database
        Map<Integer, Double> usageMap = new HashMap<>();
        // TODO: Get from UsageTrackingService
        
        List<CostShare> shares = autoSplitService.splitByUsage(costId, usageMap);
        
        return ResponseEntity.ok(shares);
    }

    /**
     * Chia đều
     * POST /api/auto-split/equal
     */
    @PostMapping("/equal")
    public ResponseEntity<List<CostShare>> splitEqually(
            @RequestParam Integer costId,
            @RequestBody List<Integer> userIds) {
        
        List<CostShare> shares = autoSplitService.splitEqually(costId, userIds);
        
        return ResponseEntity.ok(shares);
    }

    /**
     * Lấy ownership của nhóm
     * GET /api/auto-split/ownership/{groupId}
     */
    @GetMapping("/ownership/{groupId}")
    public ResponseEntity<Map<Integer, Double>> getGroupOwnership(@PathVariable Integer groupId) {
        Map<Integer, Double> ownership = autoSplitService.getGroupOwnership(groupId);
        return ResponseEntity.ok(ownership);
    }

    /**
     * Preview kết quả chia (không lưu)
     * POST /api/auto-split/preview
     */
    @PostMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewSplit(@RequestBody Map<String, Object> request) {
        try {
            Double amount = ((Number) request.get("amount")).doubleValue();
            String splitMethod = (String) request.get("splitMethod");
            Integer groupId = (Integer) request.get("groupId");
            Integer month = request.get("month") != null ? 
                (Integer) request.get("month") : 
                java.time.LocalDate.now().getMonthValue();
            Integer year = request.get("year") != null ? 
                (Integer) request.get("year") : 
                java.time.LocalDate.now().getYear();

            System.out.println("=== PREVIEW REQUEST ===");
            System.out.println("Amount: " + amount);
            System.out.println("Split Method: " + splitMethod);
            System.out.println("Group ID: " + groupId);
            System.out.println("Month/Year: " + month + "/" + year);

            Map<String, Object> preview = new HashMap<>();
            preview.put("amount", amount);
            preview.put("splitMethod", splitMethod);

            // Tạo danh sách shares để preview
            List<Map<String, Object>> shares = new java.util.ArrayList<>();

            // Calculate preview based on method
            if ("BY_OWNERSHIP".equals(splitMethod)) {
                Map<Integer, Double> ownership = autoSplitService.getGroupOwnership(groupId);
                for (Map.Entry<Integer, Double> entry : ownership.entrySet()) {
                    Map<String, Object> share = new HashMap<>();
                    share.put("userId", entry.getKey());
                    share.put("percent", entry.getValue());
                    share.put("amountShare", amount * entry.getValue() / 100);
                    shares.add(share);
                }
            } else if ("BY_USAGE".equals(splitMethod)) {
                // Chia theo km driven
                Map<Integer, Double> usageMap = new HashMap<>();
                // TODO: Get from UsageTrackingService
                // For now, use mock data
                usageMap.put(1, 100.0);
                usageMap.put(2, 150.0);
                usageMap.put(3, 50.0);
                
                double totalKm = usageMap.values().stream().mapToDouble(Double::doubleValue).sum();
                for (Map.Entry<Integer, Double> entry : usageMap.entrySet()) {
                    Map<String, Object> share = new HashMap<>();
                    share.put("userId", entry.getKey());
                    double percent = (entry.getValue() / totalKm) * 100;
                    share.put("percent", Math.round(percent * 100.0) / 100.0);
                    share.put("amountShare", amount * percent / 100);
                    shares.add(share);
                }
            } else if ("EQUAL".equals(splitMethod)) {
                // Chia đều
                Map<Integer, Double> ownership = autoSplitService.getGroupOwnership(groupId);
                int memberCount = ownership.size();
                double equalPercent = 100.0 / memberCount;
                double equalAmount = amount / memberCount;
                
                for (Integer userId : ownership.keySet()) {
                    Map<String, Object> share = new HashMap<>();
                    share.put("userId", userId);
                    share.put("percent", Math.round(equalPercent * 100.0) / 100.0);
                    share.put("amountShare", equalAmount);
                    shares.add(share);
                }
            }

            preview.put("shares", shares);
            System.out.println("Preview shares: " + shares.size());

            return ResponseEntity.ok(preview);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Lỗi khi xem trước: " + e.getMessage());
            error.put("shares", new java.util.ArrayList<>());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

