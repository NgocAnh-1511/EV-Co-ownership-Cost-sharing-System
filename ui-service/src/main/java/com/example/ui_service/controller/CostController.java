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
    public String createCost(@ModelAttribute CostDto costDto) {
        costPaymentClient.createCost(costDto);
        return "redirect:/costs";
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
}