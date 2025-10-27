package com.example.ui_service.controller;

import com.example.ui_service.client.GroupManagementClient;
import com.example.ui_service.client.CostPaymentClient;
import com.example.ui_service.dto.GroupDto;
import com.example.ui_service.dto.CostDto;
import com.example.ui_service.dto.PaymentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private GroupManagementClient groupManagementClient;

    @Autowired
    private CostPaymentClient costPaymentClient;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        try {
            List<GroupDto> groups = groupManagementClient.getAllGroups();
            List<CostDto> costs = costPaymentClient.getAllCosts();
            List<PaymentDto> payments = costPaymentClient.getAllPayments();

            model.addAttribute("groups", groups);
            model.addAttribute("costs", costs);
            model.addAttribute("payments", payments);
            model.addAttribute("totalGroups", groups.size());
            model.addAttribute("totalCosts", costs.size());
            model.addAttribute("totalPayments", payments.size());

            // Calculate total amount
            double totalAmount = costs.stream()
                    .mapToDouble(cost -> cost.getAmount() != null ? cost.getAmount() : 0.0)
                    .sum();
            model.addAttribute("totalAmount", totalAmount);

        } catch (Exception e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            model.addAttribute("groups", List.of());
            model.addAttribute("costs", List.of());
            model.addAttribute("payments", List.of());
            model.addAttribute("totalGroups", 0);
            model.addAttribute("totalCosts", 0);
            model.addAttribute("totalPayments", 0);
            model.addAttribute("totalAmount", 0.0);
        }

        return "dashboard";
    }
    
    @GetMapping("/reports")
    public String reports(Model model) {
        return "reports/index";
    }
}