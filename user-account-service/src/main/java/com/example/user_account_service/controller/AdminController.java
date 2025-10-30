package com.example.user_account_service.controller; // <-- Đổi package

import com.example.user_account_service.model.User;
import com.example.user_account_service.repository.UserRepository;
import com.example.user_account_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // <-- Import mới
import org.springframework.http.ResponseEntity; // <-- Import mới
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

// 1. Nâng cấp lên @RestController
@RestController
// 2. Thêm tiền tố API chung
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    // 3. XÓA BỎ các hàm GET trả về HTML (ví dụ: showAdminDashboard)
    //    Module 'ui-service' sẽ chịu trách nhiệm hiển thị trang HTML đó.

    /**
     * API Lấy tất cả người dùng (cho trang 'user-management.html')
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() { // <-- Bỏ Model
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users); // <-- Trả về danh sách User dạng JSON
    }

    /**
     * API Nâng cấp vai trò user thành Admin
     */
    @PostMapping("/users/promote/{userId}")
    public ResponseEntity<String> promoteUser(@PathVariable Long userId) { // <-- Bỏ RedirectAttributes
        try {
            userService.promoteUserToAdmin(userId);
            // 4. Trả về 200 OK với thông báo
            return ResponseEntity.ok("Đã nâng cấp người dùng thành Admin!");
        } catch (Exception e) {
            // 5. Trả về 400 Bad Request nếu có lỗi
            return ResponseEntity.badRequest().body("Lỗi khi nâng cấp vai trò: " + e.getMessage());
        }
    }

    /**
     * API Lấy danh sách hồ sơ đang chờ duyệt (cho trang 'profile-review.html')
     */
    @GetMapping("/profile-review")
    public ResponseEntity<List<User>> getPendingProfiles() { // <-- Bỏ Model
        List<User> pendingUsers = userService.findUnverifiedUsers();
        return ResponseEntity.ok(pendingUsers); // <-- Trả về danh sách User dạng JSON
    }

    /**
     * API Lấy chi tiết một hồ sơ để xem xét (cho trang 'profile-detail.html')
     */
    @GetMapping("/profile-review/{userId}")
    public ResponseEntity<?> getProfileDetails(@PathVariable Long userId) { // <-- Bỏ Model, RedirectAttributes
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get()); // <-- Trả về JSON của User
        } else {
            // 6. Trả về 404 Not Found nếu không tìm thấy
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy người dùng với ID: " + userId);
        }
    }

    /**
     * API Duyệt (approve) hồ sơ người dùng
     */
    @PostMapping("/profile-review/approve")
    public ResponseEntity<String> approveUserProfile(@RequestParam Long userId) { // <-- Bỏ RedirectAttributes
        try {
            userService.verifyUser(userId);
            return ResponseEntity.ok("Đã duyệt thành công hồ sơ user ID: " + userId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi duyệt hồ sơ: " + e.getMessage());
        }
    }
}