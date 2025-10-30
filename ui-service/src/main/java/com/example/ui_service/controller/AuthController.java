package com.example.ui_service.controller;

import com.example.ui_service.dto.RegistrationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${backend.api.base-url}")
    private String apiBaseUrl;

    /**
     * PHƯƠNG THỨC NÀY SẼ SỬA LỖI VÒNG LẶP.
     * Nó xử lý GET /login và trả về trang login.html.
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Trả về file templates/login.html
    }

    /**
     * Hiển thị form đăng ký.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // (Chúng ta đã copy DTO về ui-service ở Bước 1)
        model.addAttribute("userDto", new RegistrationDto());
        return "register"; // Trả về file templates/register.html
    }

    /**
     * Xử lý việc submit form đăng ký.
     * Phương thức này sẽ GỌI API (backend)
     */
    @PostMapping("/register")
    public String processRegistration(
            @Validated @ModelAttribute("userDto") RegistrationDto userDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "register"; // Quay lại form nếu lỗi
        }

        // (Kiểm tra mật khẩu xác nhận, v.v.)
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "Match", "Mật khẩu xác nhận không khớp");
            return "register";
        }

        try {
            // Gọi API đăng ký của user-account-service
            String registerUrl = apiBaseUrl + "/auth/register";

            // Gửi DTO (userDto) dưới dạng JSON
            ResponseEntity<String> response = restTemplate.postForEntity(
                    registerUrl,
                    userDto, // (RestTemplate tự chuyển thành JSON)
                    String.class
            );

            // Nếu API trả về thành công (201 Created)
            if (response.getStatusCode().is2xxSuccessful()) {
                redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
                return "redirect:/login";
            } else {
                model.addAttribute("errorMessage", "Lỗi từ API: " + response.getBody());
                return "register";
            }

        } catch (Exception e) {
            // Bắt lỗi nếu API (backend) trả về 400 (ví dụ: Email đã tồn tại)
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi đăng ký: " + e.getMessage());
            return "register";
        }
    }
}