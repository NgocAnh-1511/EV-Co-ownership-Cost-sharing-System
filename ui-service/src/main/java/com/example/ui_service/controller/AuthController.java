package com.example.ui_service.controller;

import com.example.ui_service.dto.RegistrationDto;
import com.example.ui_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // 1. Hiển thị form đăng ký
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new RegistrationDto());
        return "register";
    }

    // 2. Xử lý submit form đăng ký
    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("userDto") RegistrationDto userDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.register(userDto);
            // Gửi thông báo thành công sang trang login
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    // 3. Hiển thị trang Login
    // Spring Security sẽ xử lý lỗi, nên hàm này chỉ cần trả về tên view
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // templates/login.html
    }

    // 4. (Tạm thời) Trang chủ sau khi đăng nhập
    // Đây là trang /checkin-checkout mà tôi đã đặt trong SecurityConfig

}