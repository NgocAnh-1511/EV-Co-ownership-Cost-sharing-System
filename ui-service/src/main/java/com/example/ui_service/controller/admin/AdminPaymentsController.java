package com.example.ui_service.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller cho trang Theo dõi thanh toán Admin
 */
@Controller
@RequestMapping("/admin/payments")
public class AdminPaymentsController {

    @GetMapping
    public String payments(Model model) {
        model.addAttribute("pageTitle", "Theo dõi thanh toán");
        model.addAttribute("activePage", "payments");
        model.addAttribute("contentFragment", "admin/payments :: content");
        model.addAttribute("pageCss", new String[]{"/css/admin-payments.css"});
        model.addAttribute("pageJs", new String[]{"/js/admin-payments.js"});
        return "fragments/admin-layout";
    }
}

