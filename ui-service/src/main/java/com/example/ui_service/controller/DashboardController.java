package com.example.ui_service.controller;

import com.example.ui_service.client.GroupManagementClient;
import com.example.ui_service.client.CostPaymentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final GroupManagementClient groupManagementClient;
    private final CostPaymentClient costPaymentClient;

    @GetMapping("/")
    public String dashboard(Model model) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Load groups
            var groups = groupManagementClient.getAllGroups();
            stats.put("totalGroups", groups.size());
            stats.put("activeGroups", groups.stream().filter(g -> "ACTIVE".equals(g.getStatus())).count());
            
            // Load costs
            var costs = costPaymentClient.getAllCostItems();
            stats.put("totalCosts", costs.size());
            stats.put("pendingCosts", costs.stream().filter(c -> "PENDING".equals(c.getStatus())).count());
            stats.put("paidCosts", costs.stream().filter(c -> "PAID".equals(c.getStatus())).count());
            
            // Calculate total amounts
            double totalAmount = costs.stream()
                .mapToDouble(c -> c.getTotalAmount().doubleValue())
                .sum();
            double paidAmount = costs.stream()
                .mapToDouble(c -> c.getPaidAmount().doubleValue())
                .sum();
            
            stats.put("totalAmount", totalAmount);
            stats.put("paidAmount", paidAmount);
            stats.put("outstandingAmount", totalAmount - paidAmount);
            
            model.addAttribute("stats", stats);
            model.addAttribute("groups", groups);
            model.addAttribute("costs", costs);
            model.addAttribute("success", "Dashboard loaded successfully");
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load dashboard: " + e.getMessage());
            model.addAttribute("stats", stats);
            model.addAttribute("groups", java.util.List.of());
            model.addAttribute("costs", java.util.List.of());
        }
        
        return "dashboard";
    }

    @GetMapping("/health")
    public String healthCheck(Model model) {
        Map<String, String> services = new HashMap<>();
        
        try {
            groupManagementClient.getAllGroups();
            services.put("Group Management Service", "UP");
        } catch (Exception e) {
            services.put("Group Management Service", "DOWN - " + e.getMessage());
        }
        
        try {
            costPaymentClient.getAllCostItems();
            services.put("Cost Payment Service", "UP");
        } catch (Exception e) {
            services.put("Cost Payment Service", "DOWN - " + e.getMessage());
        }
        
        model.addAttribute("services", services);
        return "health";
    }
}
