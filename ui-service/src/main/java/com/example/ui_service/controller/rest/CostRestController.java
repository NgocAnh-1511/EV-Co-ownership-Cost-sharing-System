package com.example.ui_service.controller.rest;

import com.example.ui_service.client.CostPaymentClient;
import com.example.ui_service.dto.CostDto;
import com.example.ui_service.dto.CostSplitDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller để proxy các request Costs từ frontend sang backend
 */
@RestController
@RequestMapping("/api/costs")
public class CostRestController {

    @Autowired
    private CostPaymentClient costPaymentClient;

    /**
     * Lấy tất cả costs
     * GET /api/costs
     */
    @GetMapping
    public ResponseEntity<List<CostDto>> getAllCosts() {
        List<CostDto> costs = costPaymentClient.getAllCosts();
        return ResponseEntity.ok(costs);
    }

    /**
     * Lấy cost theo ID
     * GET /api/costs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CostDto> getCostById(@PathVariable Integer id) {
        CostDto cost = costPaymentClient.getCostById(id);
        if (cost != null) {
            return ResponseEntity.ok(cost);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lấy cost shares của một cost
     * GET /api/costs/{costId}/shares
     */
    @GetMapping("/{costId}/shares")
    public ResponseEntity<List<CostSplitDto>> getCostShares(@PathVariable Integer costId) {
        List<CostSplitDto> shares = costPaymentClient.getCostSharesByCostId(costId);
        return ResponseEntity.ok(shares);
    }

    /**
     * Tạo cost mới
     * POST /api/costs
     */
    @PostMapping
    public ResponseEntity<CostDto> createCost(@RequestBody CostDto costDto) {
        CostDto created = costPaymentClient.createCost(costDto);
        if (created != null) {
            return ResponseEntity.ok(created);
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cập nhật cost
     * PUT /api/costs/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CostDto> updateCost(@PathVariable Integer id, @RequestBody CostDto costDto) {
        CostDto updated = costPaymentClient.updateCost(id, costDto);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Xóa cost
     * DELETE /api/costs/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCost(@PathVariable Integer id) {
        boolean deleted = costPaymentClient.deleteCost(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

