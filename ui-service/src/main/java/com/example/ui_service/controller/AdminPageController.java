package com.example.ui_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @GetMapping("/login")
    public String adminLogin() {
        return "admin-login"; // Trả về template admin-login.html
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Logic để lấy dữ liệu từ API (ví dụ: vehicles và reservations)
        // Đây là placeholder, bạn có thể tích hợp fetch API từ JavaScript
        model.addAttribute("message", "Chào mừng đến bảng điều khiển admin!");
        return "admin-dashboard"; // Trả về template admin-dashboard.html
    }
}