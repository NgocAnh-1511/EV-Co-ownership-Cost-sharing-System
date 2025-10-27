package com.example.ui_service.controller;

import com.example.ui_service.client.CostPaymentClient;
import com.example.ui_service.dto.CostDto;
import com.example.ui_service.dto.CostSplitDto;
import com.example.ui_service.dto.PaymentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/costs")
public class CostController {

    @Autowired
    private CostPaymentClient costPaymentClient;

    @GetMapping
    public String listCosts(Model model) {
        List<CostDto> costs = costPaymentClient.getAllCosts();
        model.addAttribute("costs", costs);
        return "costs/list";
    }

    @GetMapping("/create")
    public String createCostForm(Model model) {
        model.addAttribute("cost", new CostDto());
        return "costs/create";
    }

    @PostMapping("/create")
    public String createCost(@ModelAttribute CostDto costDto, Model model) {
        try {
            CostDto createdCost = costPaymentClient.createCost(costDto);
            if (createdCost != null) {
                return "redirect:/costs?success=true";
            } else {
                model.addAttribute("error", "Không thể tạo chi phí. Vui lòng thử lại.");
                return "costs/create";
            }
        } catch (Exception e) {
            System.err.println("Error creating cost: " + e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tạo chi phí: " + e.getMessage());
            return "costs/create";
        }
    }

    @GetMapping("/{id}/splits")
    public String listCostSplits(@PathVariable Integer id, Model model) {
        List<CostSplitDto> splits = costPaymentClient.getCostSplits(id);
        model.addAttribute("splits", splits);
        model.addAttribute("costId", id);
        return "costs/splits";
    }

    @PostMapping("/{id}/splits")
    public String createCostSplit(@PathVariable Integer id, @ModelAttribute CostSplitDto splitDto) {
        costPaymentClient.createCostSplit(id, splitDto);
        return "redirect:/costs/" + id + "/splits";
    }

    @GetMapping("/payments")
    public String listPayments(Model model) {
        List<PaymentDto> payments = costPaymentClient.getAllPayments();
        model.addAttribute("payments", payments);
        return "costs/payments";
    }

    @PostMapping("/payments")
    public String createPayment(@ModelAttribute PaymentDto paymentDto) {
        costPaymentClient.createPayment(paymentDto);
        return "redirect:/costs/payments";
    }

    @GetMapping("/{id}/edit")
    public String editCostForm(@PathVariable Integer id, Model model) {
        try {
            CostDto cost = costPaymentClient.getCostById(id);
            if (cost != null) {
                model.addAttribute("cost", cost);
                return "costs/edit";
            } else {
                return "redirect:/costs?error=notfound";
            }
        } catch (Exception e) {
            System.err.println("Error fetching cost for edit: " + e.getMessage());
            return "redirect:/costs?error=fetch";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateCost(@PathVariable Integer id, @ModelAttribute CostDto costDto, Model model) {
        try {
            CostDto updatedCost = costPaymentClient.updateCost(id, costDto);
            if (updatedCost != null) {
                return "redirect:/costs?success=updated";
            } else {
                model.addAttribute("error", "Không thể cập nhật chi phí. Vui lòng thử lại.");
                return "costs/edit";
            }
        } catch (Exception e) {
            System.err.println("Error updating cost: " + e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi cập nhật chi phí: " + e.getMessage());
            return "costs/edit";
        }
    }

    @PostMapping("/{id}/delete")
    @ResponseBody
    public String deleteCost(@PathVariable Integer id) {
        try {
            boolean deleted = costPaymentClient.deleteCost(id);
            if (deleted) {
                return "success";
            } else {
                return "error";
            }
        } catch (Exception e) {
            System.err.println("Error deleting cost: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/sharing")
    public String costSharing(Model model) {
        // Load costs for sharing page
        List<CostDto> costs = costPaymentClient.getAllCosts();
        model.addAttribute("costs", costs);
        return "costs/sharing";
    }

    @PostMapping("/sharing")
    @ResponseBody
    public String createCostForSharing(@RequestBody CostDto costDto) {
        try {
            costPaymentClient.createCost(costDto);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/api/costs")
    @ResponseBody
    public List<CostDto> getCostsApi() {
        return costPaymentClient.getAllCosts();
    }

    @PostMapping("/api/costs")
    @ResponseBody
    public String createCostApi(@RequestBody CostDto costDto) {
        try {
            costPaymentClient.createCost(costDto);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    @GetMapping("/reports")
    public String costReports(Model model) {
        return "costs/reports";
    }

    // Edit Cost functionality
    @GetMapping("/{id}/edit")
    public String editCostForm(@PathVariable Integer id, Model model) {
        try {
            CostDto cost = costPaymentClient.getCostById(id);
            if (cost != null) {
                model.addAttribute("cost", cost);
                return "costs/edit";
            } else {
                return "redirect:/costs?error=notfound";
            }
        } catch (Exception e) {
            return "redirect:/costs?error=load";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateCost(@PathVariable Integer id, @ModelAttribute CostDto costDto) {
        try {
            costPaymentClient.updateCost(id, costDto);
            return "redirect:/costs?success=updated";
        } catch (Exception e) {
            return "redirect:/costs/" + id + "/edit?error=update";
        }
    }

    @PostMapping("/{id}/delete")
    @ResponseBody
    public String deleteCost(@PathVariable Integer id) {
        try {
            costPaymentClient.deleteCost(id);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/{id}")
    public String viewCost(@PathVariable Integer id, Model model) {
        try {
            CostDto cost = costPaymentClient.getCostById(id);
            if (cost != null) {
                model.addAttribute("cost", cost);
                return "costs/view";
            } else {
                return "redirect:/costs?error=notfound";
            }
        } catch (Exception e) {
            return "redirect:/costs?error=load";
        }
    }

    // Search and filter functionality
    @GetMapping("/search")
    public String searchCosts(@RequestParam(required = false) String query,
                             @RequestParam(required = false) String costType,
                             @RequestParam(required = false) Integer vehicleId,
                             Model model) {
        try {
            List<CostDto> costs = costPaymentClient.searchCosts(query, costType, vehicleId);
            model.addAttribute("costs", costs);
            model.addAttribute("query", query);
            model.addAttribute("costType", costType);
            model.addAttribute("vehicleId", vehicleId);
            return "costs/list";
        } catch (Exception e) {
            model.addAttribute("costs", List.of());
            return "costs/list";
        }
    }
}