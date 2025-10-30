package com.example.user_account_service.controller;

import com.example.user_account_service.dto.RegistrationDto;
import com.example.user_account_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*; // <-- Quan trọng!

// 1. Nâng cấp lên @RestController
@RestController
// 2. Thêm tiền tố API chung
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // 3. XÓA BỎ các hàm GET (vì API backend không trả về trang HTML)
    // - public String showRegistrationForm(...) -> ĐÃ XÓA
    // - public String showLoginForm(...) -> ĐÃ XÓA

    /**
     * API Endpoint để xử lý đăng ký tài khoản mới.
     * Sẽ được gọi từ ui-service.
     */
    @PostMapping("/register")
    // 4. Đổi kiểu trả về sang ResponseEntity
    public ResponseEntity<?> processRegistration(
            // 5. Đổi từ @ModelAttribute sang @RequestBody
            //    Điều này yêu cầu ui-service gửi dữ liệu dạng JSON
            @Valid @RequestBody RegistrationDto userDto,
            BindingResult bindingResult) { // <-- Xóa Model, RedirectAttributes

        // Xử lý lỗi validation (nếu mật khẩu không khớp, email sai)
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }

        try {
            userService.register(userDto);
            // 6. Trả về mã 201 CREATED (Tạo thành công)
            return new ResponseEntity<>("Đăng ký tài khoản thành công!", HttpStatus.CREATED);

        } catch (Exception e) {
            // 7. Trả về mã 400 BAD REQUEST nếu có lỗi (ví dụ: Email đã tồn tại)
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}