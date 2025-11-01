package com.example.user_account_service.controller;

import com.example.user_account_service.dto.LoginRequest;
import com.example.user_account_service.dto.LoginResponse;
import com.example.user_account_service.dto.RegisterRequest;
import com.example.user_account_service.dto.UserProfileUpdateRequest;
import com.example.user_account_service.entity.User;
import com.example.user_account_service.service.UserService;
import com.example.user_account_service.service.StorageService; // <-- THÊM IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // <-- THÊM IMPORT

import java.util.Map; // <-- THÊM IMPORT

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:8080") // Cho phép UI gọi
public class UserController {

    @Autowired
    private UserService userService;

    // NÂNG CẤP: Tiêm (Inject) StorageService
    @Autowired
    private StorageService storageService;

    /**
     * API Đăng ký (PUBLIC)
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
     * API Đăng nhập (PUBLIC)
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = userService.loginUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Sai email hoặc mật khẩu");
        }
    }

    /**
     * API TẢI DỮ LIỆU hồ sơ cá nhân (BẢO VỆ)
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Người dùng không xác định"));
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Không tìm thấy hồ sơ người dùng.");
        }
    }

    /**
     * API CẬP NHẬT hồ sơ cá nhân (BẢO VỆ)
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserProfileUpdateRequest request, Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Người dùng không xác định"));
            User updatedUser = userService.updateProfile(user.getUserId(), request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * NÂNG CẤP: API UPLOAD FILE (BẢO VỆ)
     * URL: POST http://localhost:8081/api/users/profile/upload
     */
    @PostMapping("/profile/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Người dùng không xác định"));

            // 1. Lưu file và nhận lại tên file duy nhất
            String savedFileName = storageService.storeFile(file, user.getUserId());

            // 2. Tạo URL (Giả định: file sẽ được phục vụ tĩnh từ /uploads/)
            String fileUrl = "/uploads/" + savedFileName;

            // 3. Trả về URL cho Frontend (đúng như JS mong đợi)
            // Trả về một đối tượng JSON: { "fileUrl": "/uploads/..." }
            return ResponseEntity.ok(Map.of("fileUrl", fileUrl));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body("Lỗi tải file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi máy chủ khi tải file.");
        }
    }
}