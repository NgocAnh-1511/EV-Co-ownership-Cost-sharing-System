package com.example.ui_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Collections;

@Controller
public class UIController {

    // 1. Trang Đăng nhập
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // 2. Trang Đăng ký
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    // 3. Trang Admin Dashboard
    @GetMapping("/admin/groups")
    public String showGroupManagement(Model model) {
        model.addAttribute("pageTitle", "Quản Lí Nhóm Đồng Sở Hữu");
        model.addAttribute("adminName", "Admin");
        // ... (dữ liệu mẫu khác cho admin) ...
        model.addAttribute("groups", Collections.emptyList());
        return "group-management";
    }

    // 4. Trang Admin Duyệt Hồ Sơ
    @GetMapping("/admin/profile-approval")
    public String showProfileApprovalPage(Model model) {
        model.addAttribute("pageTitle", "Duyệt Hồ Sơ Người Dùng");
        return "admin/profile-approval";
    }

    // 5. Trang User Onboarding (Đăng ký hồ sơ)
    @GetMapping("/user/onboarding")
    public String showUserOnboardingPage(Model model) {
        model.addAttribute("pageTitle", "Hoàn tất đăng ký");
        model.addAttribute("currentPage", "onboarding"); // <-- NÂNG CẤP: Báo cho Sidebar
        return "user-onboarding";
    }

    // 6. Trang xem Tình trạng Hồ sơ (User)
    @GetMapping("/user/profile-status")
    public String showProfileStatusPage(Model model) {
        model.addAttribute("pageTitle", "Tình trạng Hồ sơ");
        model.addAttribute("currentPage", "status"); // <-- NÂNG CẤP: Báo cho Sidebar
        return "profile-status";
    }

    // 7. Trang chủ (Redirect)
    @GetMapping("/")
    public String index() {
        // Chuyển hướng mặc định (JS ở /login sẽ xử lý phân quyền)
        return "redirect:/login";
    }
}