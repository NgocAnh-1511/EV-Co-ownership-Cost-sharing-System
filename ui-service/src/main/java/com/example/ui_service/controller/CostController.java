package com.example.ui_service.controller;

import com.example.ui_service.client.CostPaymentClient;
import com.example.ui_service.dto.CostItemDto;
import com.example.ui_service.dto.CostSplitDto;
import com.example.ui_service.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/costs")
@RequiredArgsConstructor
public class CostController {

    private final CostPaymentClient costPaymentClient;

    @GetMapping
    public String getAllCosts(Model model) {
        try {
            List<CostItemDto> costs = costPaymentClient.getAllCostItems();
            model.addAttribute("costs", costs);
            model.addAttribute("success", "Costs loaded successfully");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load costs: " + e.getMessage());
            model.addAttribute("costs", List.of());
        }
        return "costs/list";
    }

    @GetMapping("/group/{groupId}")
    public String getCostsByGroup(@PathVariable String groupId, Model model) {
        try {
            List<CostItemDto> costs = costPaymentClient.getCostItemsByGroup(groupId);
            Map<String, Object> summary = costPaymentClient.getGroupFinancialSummary(groupId);
            
            model.addAttribute("costs", costs);
            model.addAttribute("summary", summary);
            model.addAttribute("groupId", groupId);
            model.addAttribute("success", "Group costs loaded successfully");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load group costs: " + e.getMessage());
            model.addAttribute("costs", List.of());
        }
        return "costs/group-list";
    }

    @GetMapping("/{id}")
    public String getCostById(@PathVariable Long id, Model model) {
        try {
            CostItemDto cost = costPaymentClient.getCostItemById(id);
            List<CostSplitDto> splits = costPaymentClient.getCostSplits(id);
            
            model.addAttribute("cost", cost);
            model.addAttribute("splits", splits);
            model.addAttribute("success", "Cost details loaded successfully");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load cost details: " + e.getMessage());
        }
        return "costs/detail";
    }

    @GetMapping("/create")
    public String createCostForm(Model model) {
        model.addAttribute("cost", new CostItemDto());
        return "costs/create";
    }

    @PostMapping("/create")
    public String createCost(@ModelAttribute CostItemDto cost, Model model) {
        try {
            CostItemDto createdCost = costPaymentClient.createCostItem(cost);
            model.addAttribute("success", "Cost created successfully");
            return "redirect:/costs/" + createdCost.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create cost: " + e.getMessage());
            model.addAttribute("cost", cost);
            return "costs/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editCostForm(@PathVariable Long id, Model model) {
        try {
            CostItemDto cost = costPaymentClient.getCostItemById(id);
            model.addAttribute("cost", cost);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load cost: " + e.getMessage());
            return "redirect:/costs";
        }
        return "costs/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateCost(@PathVariable Long id, @ModelAttribute CostItemDto cost, Model model) {
        try {
            costPaymentClient.updateCostItem(id, cost);
            model.addAttribute("success", "Cost updated successfully");
            return "redirect:/costs/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update cost: " + e.getMessage());
            model.addAttribute("cost", cost);
            return "costs/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCost(@PathVariable Long id, Model model) {
        try {
            costPaymentClient.deleteCostItem(id);
            model.addAttribute("success", "Cost deleted successfully");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete cost: " + e.getMessage());
        }
        return "redirect:/costs";
    }

    @PostMapping("/{id}/create-splits")
    public String createCostSplits(@PathVariable Long id, Model model) {
        try {
            costPaymentClient.createCostSplits(id);
            model.addAttribute("success", "Cost splits created successfully");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create cost splits: " + e.getMessage());
        }
        return "redirect:/costs/" + id;
    }

    @GetMapping("/user/{userId}/payments")
    public String getUserPayments(@PathVariable String userId, Model model) {
        try {
            List<PaymentDto> payments = costPaymentClient.getPaymentsByUser(userId);
            Map<String, Object> summary = costPaymentClient.getUserFinancialSummary(userId);
            
            model.addAttribute("payments", payments);
            model.addAttribute("summary", summary);
            model.addAttribute("userId", userId);
            model.addAttribute("success", "User payments loaded successfully");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load user payments: " + e.getMessage());
            model.addAttribute("payments", List.of());
        }
        return "costs/user-payments";
    }

    @GetMapping("/split/{splitId}/payment")
    public String createPaymentForm(@PathVariable Long splitId, Model model) {
        model.addAttribute("payment", new PaymentDto());
        model.addAttribute("splitId", splitId);
        return "costs/create-payment";
    }

    @PostMapping("/split/{splitId}/payment")
    public String createPayment(@PathVariable Long splitId, @ModelAttribute PaymentDto payment, Model model) {
        try {
            PaymentDto createdPayment = costPaymentClient.createPayment(splitId, payment);
            model.addAttribute("success", "Payment created successfully");
            return "redirect:/costs/user/" + payment.getUserId() + "/payments";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create payment: " + e.getMessage());
            model.addAttribute("payment", payment);
            model.addAttribute("splitId", splitId);
            return "costs/create-payment";
        }
    }
}
