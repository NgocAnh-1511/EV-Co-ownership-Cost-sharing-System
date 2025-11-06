package com.example.reservationservice.controller;

import com.example.reservationservice.model.User;
import com.example.reservationservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller để quản lý thông tin Users
 * Phục vụ cho các services khác (như AIService) lấy thông tin user
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserRepository userRepository;
    
    /**
     * Lấy danh sách tất cả users
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            
            List<Map<String, Object>> userMaps = users.stream()
                .map(this::convertUserToMap)
                .toList();
            
            log.info("Retrieved {} users", userMaps.size());
            return ResponseEntity.ok(userMaps);
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thông tin user theo ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        try {
            log.info("Fetching user with ID: {}", id);
            
            User user = userRepository.findById(id)
                .orElse(null);
            
            if (user == null) {
                log.warn("User not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> userMap = convertUserToMap(user);
            log.info("Found user: {} ({})", user.getUsername(), user.getFullName());
            
            return ResponseEntity.ok(userMap);
        } catch (Exception e) {
            log.error("Error fetching user {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thông tin user theo username
     * GET /api/users/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
        try {
            log.info("Fetching user with username: {}", username);
            
            User user = userRepository.findByUsername(username)
                .orElse(null);
            
            if (user == null) {
                log.warn("User not found with username: {}", username);
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> userMap = convertUserToMap(user);
            log.info("Found user: {} ({})", user.getUsername(), user.getFullName());
            
            return ResponseEntity.ok(userMap);
        } catch (Exception e) {
            log.error("Error fetching user {}: {}", username, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Chuyển User entity sang Map để trả về JSON
     */
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getUserId());
        map.put("username", user.getUsername());
        map.put("fullName", user.getFullName());
        map.put("email", user.getEmail());
        map.put("phone", user.getPhone());
        return map;
    }
}

