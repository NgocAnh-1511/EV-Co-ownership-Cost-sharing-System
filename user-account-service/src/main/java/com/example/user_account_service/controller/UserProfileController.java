package com.example.user_account_service.controller;

import com.example.user_account_service.dto.CoOwnerRegistrationDto;
import com.example.user_account_service.model.User;
import com.example.user_account_service.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// 1. Nâng cấp lên @RestController
@RestController
// 2. Thêm tiền tố API chung
@RequestMapping("/api/profile")
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    private UserService userService;

    // 3. XÓA BỎ các hàm GET trả về HTML
    // - public String showCoOwnerRegistrationForm(...) -> ĐÃ XÓA
    // - public String showProfileView(...) -> ĐÃ NÂNG CẤP (xem dưới)
    // - public String showProfileStatus(...) -> ĐÃ XÓA (trạng thái sẽ nằm trong /me)

    /**
     * API Lấy thông tin hồ sơ của người dùng đang đăng nhập.
     * ui-service sẽ gọi API này để hiển thị trang "profile-view.html"
     */
    @GetMapping("/me") // "me" là một REST convention chuẩn cho "hồ sơ của tôi"
    public ResponseEntity<?> getMyProfile() { // <-- Bỏ Model
        try {
            User currentUser = userService.getCurrentAuthenticatedUser();
            // 4. Trả về 200 OK với đối tượng User (sẽ tự động thành JSON)
            return ResponseEntity.ok(currentUser);
        } catch (IllegalStateException | UsernameNotFoundException e) {
            logger.warn("Attempt to view profile without authentication or user not found", e);
            // 5. Trả về 401 UNAUTHORIZED (Chưa đăng nhập)
            return new ResponseEntity<>("Vui lòng đăng nhập để xem hồ sơ.", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * API Cập nhật thông tin hồ sơ (bao gồm cả file upload).
     * ui-service sẽ gọi API này khi submit form "co-owner-registration.html"
     */
    @PostMapping("/update") // <-- Đổi tên đường dẫn
    public ResponseEntity<?> processCoOwnerRegistration(
            // 6. LƯU Ý QUAN TRỌNG:
            // Vì form này có cả Dữ liệu (DTO) và Files (MultipartFile),
            // chúng ta VẪN DÙNG @ModelAttribute.
            // ui-service sẽ phải gửi request dạng "multipart/form-data".
            @Valid @ModelAttribute("coOwnerDto") CoOwnerRegistrationDto coOwnerDto,
            BindingResult bindingResult,
            @RequestParam("idCardFront") MultipartFile idCardFrontFile,
            @RequestParam("idCardBack") MultipartFile idCardBackFile,
            @RequestParam("licenseImage") MultipartFile licenseImageFile,
            @RequestParam("portraitImage") MultipartFile portraitImageFile) { // <-- Bỏ RedirectAttributes, Model

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors found: {}", bindingResult.getAllErrors());
            // 7. Trả về 400 BAD REQUEST nếu dữ liệu không hợp lệ
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }

        try {
            User updatedUser = userService.updateCoOwnerInfo(coOwnerDto, idCardFrontFile, idCardBackFile, licenseImageFile, portraitImageFile);
            // 8. Trả về 200 OK với thông tin User đã cập nhật
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalStateException | UsernameNotFoundException e) {
            logger.error("User not found or not authenticated during profile update", e);
            // 9. Trả về 401 UNAUTHORIZED
            return new ResponseEntity<>("Lỗi: Người dùng không hợp lệ hoặc chưa đăng nhập.", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.error("Error processing co-owner registration/update", e);
            // 10. Trả về 500 Lỗi máy chủ
            return new ResponseEntity<>("Đã xảy ra lỗi khi xử lý: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}