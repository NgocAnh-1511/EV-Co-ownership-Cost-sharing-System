package com.example.costpayment.controller;

import com.example.costpayment.dto.CostDto;
import com.example.costpayment.dto.CostShareDto;
import com.example.costpayment.dto.CostSplitRequestDto;
import com.example.costpayment.entity.CostShare;
import com.example.costpayment.service.CostShareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/costs")
public class CostController {

    private static final Logger logger = LoggerFactory.getLogger(CostController.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CostShareService costShareService;

    @GetMapping
    public List<CostDto> getAllCosts() {
        logger.info("=== getAllCosts() method called ===");
        List<CostDto> costs = new ArrayList<>();
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Cost ORDER BY createdAt DESC");
            
            int count = 0;
            while (resultSet.next()) {
                count++;
                try {
                    CostDto cost = new CostDto();
                    cost.setCostId(resultSet.getInt("costId"));
                    cost.setVehicleId(resultSet.getInt("vehicleId"));
                    cost.setCostType(resultSet.getString("costType"));
                    cost.setAmount(resultSet.getDouble("amount"));
                    cost.setDescription(resultSet.getString("description"));
                    
                    // Convert Timestamp to LocalDateTime
                    java.sql.Timestamp timestamp = resultSet.getTimestamp("createdAt");
                    if (timestamp != null) {
                        cost.setCreatedAt(timestamp.toLocalDateTime());
                    }
                    
                    costs.add(cost);
                    logger.info("Processed cost {}: ID={}, Amount={}", count, cost.getCostId(), cost.getAmount());
                } catch (Exception rowException) {
                    logger.error("Error processing row {}: {}", count, rowException.getMessage(), rowException);
                }
            }
            
            logger.info("Total rows processed: {}, costs added: {}", count, costs.size());
            
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            logger.error("Database error: {}", e.getMessage(), e);
        }
        return costs;
    }

    @GetMapping("/simple")
    public String getSimpleCosts() {
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) as count FROM Cost");
            
            int count = 0;
            if (resultSet.next()) {
                count = resultSet.getInt("count");
            }
            
            resultSet.close();
            statement.close();
            connection.close();
            
            return "Database connected! Found " + count + " costs.";
        } catch (Exception e) {
            return "Database error: " + e.getMessage();
        }
    }

    // ========== COST SHARING ENDPOINTS ==========

    /**
     * Lấy thông tin chia chi phí cho một cost cụ thể
     */
    @GetMapping("/{costId}/splits")
    public ResponseEntity<List<CostShareDto>> getCostSplits(@PathVariable Integer costId) {
        logger.info("=== getCostSplits() method called for costId: {} ===", costId);
        try {
            List<CostShare> costShares = costShareService.getCostSharesByCostId(costId);
            List<CostShareDto> costShareDtos = costShares.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            logger.info("Found {} cost shares for costId: {}", costShareDtos.size(), costId);
            return ResponseEntity.ok(costShareDtos);
        } catch (Exception e) {
            logger.error("Error getting cost splits for costId {}: {}", costId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Tạo chia chi phí mới cho một cost
     */
    @PostMapping("/{costId}/splits")
    public ResponseEntity<List<CostShareDto>> createCostSplits(
            @PathVariable Integer costId,
            @RequestBody CostSplitRequestDto request) {
        logger.info("=== createCostSplits() method called for costId: {} ===", costId);
        logger.info("Request: {}", request);
        
        try {
            // Validate request
            if (request.getUserIds() == null || request.getPercentages() == null ||
                request.getUserIds().size() != request.getPercentages().size()) {
                logger.error("Invalid request: userIds and percentages must have same size");
                return ResponseEntity.badRequest().build();
            }

            // Validate percentages sum to 100
            double totalPercent = request.getPercentages().stream().mapToDouble(Double::doubleValue).sum();
            if (Math.abs(totalPercent - 100.0) > 0.01) {
                logger.error("Invalid percentages: total must be 100%, got: {}%", totalPercent);
                return ResponseEntity.badRequest().build();
            }

            List<CostShare> costShares = costShareService.calculateCostShares(
                    costId, request.getUserIds(), request.getPercentages());
            
            List<CostShareDto> costShareDtos = costShares.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            logger.info("Created {} cost shares for costId: {}", costShareDtos.size(), costId);
            return ResponseEntity.ok(costShareDtos);
        } catch (Exception e) {
            logger.error("Error creating cost splits for costId {}: {}", costId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy tất cả cost shares
     */
    @GetMapping("/splits")
    public ResponseEntity<List<CostShareDto>> getAllCostSplits() {
        logger.info("=== getAllCostSplits() method called ===");
        try {
            List<CostShare> costShares = costShareService.getAllCostShares();
            List<CostShareDto> costShareDtos = costShares.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            logger.info("Found {} total cost shares", costShareDtos.size());
            return ResponseEntity.ok(costShareDtos);
        } catch (Exception e) {
            logger.error("Error getting all cost splits: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Convert CostShare entity to CostShareDto
     */
    private CostShareDto convertToDto(CostShare costShare) {
        return new CostShareDto(
                costShare.getShareId(),
                costShare.getCostId(),
                costShare.getUserId(),
                costShare.getPercent(),
                costShare.getAmountShare(),
                costShare.getCalculatedAt()
        );
    }
}