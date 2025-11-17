package com.example.ui_service.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller cho trang Home của User (Nhóm của tôi)
 */
@Controller
@RequestMapping("/user")
public class UserHomeController {

    @GetMapping({"", "/", "/home"})
    public String home(Model model) {
        model.addAttribute("pageTitle", "Hoàn tất đăng ký");
        model.addAttribute("currentPage", "auth-onboarding");
        return "auth/user-onboarding";
    }
}

