package com.example.ui_service.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller cho trang Quản lý quỹ chung Admin
 */
@Controller
@RequestMapping("/admin/funds")
public class AdminFundsController {

    @GetMapping
    public String funds(Model model) {
        model.addAttribute("pageTitle", "Quản lý quỹ chung");
        model.addAttribute("pageSubtitle", "Quản lý và theo dõi quỹ chung của các nhóm");
        return "admin-funds";
    }
}

