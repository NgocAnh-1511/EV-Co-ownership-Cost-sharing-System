package com.example.ui_service.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller cho trang Tổng quan Admin
 */
@Controller
@RequestMapping("/admin")
public class AdminOverviewController {

    @GetMapping({"", "/overview"})
    public String overview(Model model) {
        model.addAttribute("pageTitle", "Tổng quan");
        model.addAttribute("activePage", "overview");
        model.addAttribute("contentFragment", "admin/overview :: content");
        model.addAttribute("pageCss", new String[]{"/css/admin-overview.css"});
        model.addAttribute("pageJs", new String[]{"/js/admin-overview.js"});
        return "fragments/admin-layout";
    }
}

