package com.example.ui_service.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller cho trang Chia tự động Admin
 */
@Controller
@RequestMapping("/admin/auto-split")
public class AdminAutoSplitController {

    @GetMapping
    public String autoSplit(Model model) {
        model.addAttribute("pageTitle", "Chia tự động");
        model.addAttribute("activePage", "auto-split");
        model.addAttribute("contentFragment", "admin/auto-split :: content");
        model.addAttribute("pageCss", new String[]{"/css/admin-auto-split.css"});
        model.addAttribute("pageJs", new String[]{"/js/admin-auto-split.js"});
        return "fragments/admin-layout";
    }
}

