package com.example.user_account_service.controller;

import com.example.user_account_service.model.User;
import com.example.user_account_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional; // <-- Import thêm

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * API Endpoint để UserDetailsService (của ui-service)
     * có thể lấy thông tin user (bao gồm cả hash mật khẩu) bằng email.
     */
    @GetMapping("/by-email")
    // SỬA Ở ĐÂY: Đổi từ ResponseEntity<?> thành ResponseEntity<Object>
    public ResponseEntity<Object> getUserByEmail(@RequestParam String email) {

        Optional<User> userOptional = userRepository.findByEmail(email);

        // SỬA Ở ĐÂY: Dùng if/else để rõ ràng
        if (userOptional.isPresent()) {
            // Trả về 200 OK với đối tượng User
            return ResponseEntity.ok(userOptional.get());
        } else {
            // Trả về 404 Not Found với một chuỗi String
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy người dùng với email: " + email);
        }
    }
}