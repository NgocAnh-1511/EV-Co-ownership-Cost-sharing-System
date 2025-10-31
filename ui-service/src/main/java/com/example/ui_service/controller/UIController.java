package com.example.ui_service.controller; // Đảm bảo package này là đúng

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // <-- Phải có @Controller để Spring tìm thấy
public class UIController {

    /**
     * Hiển thị trang đăng nhập
     * URL: http://localhost:8080/login
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Trả về file login.html trong thư mục templates
    }

    /**
     * Hiển thị trang đăng ký
     * URL: http://localhost:8080/register
     */
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // Trả về file register.html
    }
}