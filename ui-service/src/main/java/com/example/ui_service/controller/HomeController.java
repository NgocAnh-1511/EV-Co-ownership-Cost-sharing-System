package com.example.ui_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * Trang chủ hoặc dashboard
     * Có thể thêm các endpoint khác cho trang chủ ở đây
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Trang Chủ");
        model.addAttribute("pageDescription", "Hệ thống quản lý xe đồng sở hữu");
        return "index"; // Hoặc redirect đến trang dashboard
    }

    /**
     * Dashboard page
     */
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("pageDescription", "Trang tổng quan hệ thống");
        return "admin/dashboard"; // Tạo template này nếu cần
    }
}
