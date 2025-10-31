package com.example.user_account_service.controller;

import com.example.user_account_service.dto.LoginRequest; // <-- THÊM IMPORT
import com.example.user_account_service.dto.LoginResponse; // <-- THÊM IMPORT
import com.example.user_account_service.dto.RegisterRequest;
import com.example.user_account_service.entity.User;
import com.example.user_account_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
// @CrossOrigin(origins = "http://localhost:8080") // Không cần nữa vì đã cấu hình trong SecurityConfig
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * API để đăng ký tài khoản mới.
     * URL: POST http://localhost:8081/api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            User newUser = userService.registerUser(registerRequest);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * THÊM ENDPOINT NÀY VÀO
     * API để đăng nhập.
     * URL: POST http://localhost:8081/api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = userService.loginUser(loginRequest);
            // Trả về 200 OK cùng với JWT token
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Trả về 401 Unauthorized nếu sai email hoặc mật khẩu
            return ResponseEntity.status(401).body("Sai email hoặc mật khẩu");
        }
    }
}