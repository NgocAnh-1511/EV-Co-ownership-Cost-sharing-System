package com.example.user_account_service.controller;

import com.example.user_account_service.dto.LoginRequest;
import com.example.user_account_service.dto.LoginResponse;
import com.example.user_account_service.dto.RefreshTokenRequest;
import com.example.user_account_service.dto.RegisterRequest;
import com.example.user_account_service.dto.UserProfileUpdateRequest;
import com.example.user_account_service.entity.User;
import com.example.user_account_service.service.UserService;
import com.example.user_account_service.service.StorageService; // <-- THÊM IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // <-- THÊM IMPORT

import java.util.HashMap;
import java.util.Map; // <-- THÊM IMPORT

@RestController
@RequestMapping("/api/auth/users")
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
            LoginResponse response = userService.registerUser(registerRequest);
            return ResponseEntity.ok(response);
        } catch (DataIntegrityViolationException e) {
            // Xử lý lỗi duplicate entry từ database (race condition hoặc check bị bypass)
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Duplicate entry")) {
                if (errorMessage.contains("email") || errorMessage.contains("UK6dotkott2kjsp8vw4d0m25fb7")) {
                    return ResponseEntity.badRequest().body("Email này đã được đăng ký. Vui lòng sử dụng email khác hoặc đăng nhập.");
                } else if (errorMessage.contains("phone_number")) {
                    return ResponseEntity.badRequest().body("Số điện thoại này đã được đăng ký. Vui lòng sử dụng số điện thoại khác.");
                } else if (errorMessage.contains("id_card_number")) {
                    return ResponseEntity.badRequest().body("Số CMND/CCCD này đã được đăng ký.");
                } else if (errorMessage.contains("license_number")) {
                    return ResponseEntity.badRequest().body("Số giấy phép lái xe này đã được đăng ký.");
                }
            }
            return ResponseEntity.badRequest().body("Lỗi: Dữ liệu không hợp lệ hoặc đã tồn tại trong hệ thống.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi máy chủ: " + e.getMessage());
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
     * API Refresh Token (PUBLIC - dựa vào refresh token hợp lệ)
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            LoginResponse response = userService.refreshSession(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        }
    }

    /**
     * Thu hồi refresh token (Logout)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request) {
        userService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    /**
     * API TẢI DỮ LIỆU hồ sơ cá nhân (BẢO VỆ)
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            User user = getAuthenticatedUser(authentication);
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
            User currentUser = getAuthenticatedUser(authentication);
            User updatedUser = userService.updateProfile(currentUser.getUserId(), request);
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
            User user = getAuthenticatedUser(authentication);

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

    /**
     * API gửi hồ sơ KYC (upload nhiều file)
     */
    @PostMapping("/profile/kyc")
    public ResponseEntity<?> submitKycDocuments(
            Authentication authentication,
            @RequestParam(value = "idCardFront", required = false) MultipartFile idCardFront,
            @RequestParam(value = "idCardBack", required = false) MultipartFile idCardBack,
            @RequestParam(value = "driverLicense", required = false) MultipartFile driverLicense,
            @RequestParam(value = "portrait", required = false) MultipartFile portrait
    ) {
        try {
            User user = getAuthenticatedUser(authentication);
            Map<String, String> uploadedUrls = new HashMap<>();

            if (idCardFront != null && !idCardFront.isEmpty()) {
                uploadedUrls.put("idCardFrontUrl", saveKycFile(idCardFront, user.getUserId()));
            }
            if (idCardBack != null && !idCardBack.isEmpty()) {
                uploadedUrls.put("idCardBackUrl", saveKycFile(idCardBack, user.getUserId()));
            }
            if (driverLicense != null && !driverLicense.isEmpty()) {
                uploadedUrls.put("licenseImageUrl", saveKycFile(driverLicense, user.getUserId()));
            }
            if (portrait != null && !portrait.isEmpty()) {
                uploadedUrls.put("portraitImageUrl", saveKycFile(portrait, user.getUserId()));
            }

            if (uploadedUrls.isEmpty()) {
                return ResponseEntity.badRequest().body("Vui lòng tải lên ít nhất một tệp.");
            }

            User updatedUser = userService.updateKycDocuments(user.getUserId(), uploadedUrls);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Không thể xử lý hồ sơ KYC.");
        }
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Người dùng không xác định");
        }
        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Người dùng không xác định"));
    }

    private String saveKycFile(MultipartFile file, Long userId) {
        String savedFileName = storageService.storeFile(file, userId);
        return "/uploads/" + savedFileName;
    }
}
