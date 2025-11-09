package com.example.ui_service.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller cho trang Quản lý chi phí Admin
 */
@Controller
@RequestMapping("/admin/costs")
public class AdminCostsController {

    @GetMapping
    public String costs(Model model) {
        model.addAttribute("pageTitle", "Quản lý chi phí");
        model.addAttribute("activePage", "costs");
        model.addAttribute("contentFragment", "admin/costs :: content");
        model.addAttribute("pageCss", new String[]{"/css/admin-costs.css"});
        model.addAttribute("pageJs", new String[]{"/js/admin-costs.js"});
        return "fragments/admin-layout";
    }
}

