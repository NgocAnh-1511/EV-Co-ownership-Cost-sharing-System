package com.example.reservationservice.controller;

import com.example.reservationservice.model.User;
import com.example.reservationservice.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:8080"}, allowCredentials = "true")
public class AuthController {
    
    private final UserRepository userRepository;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, 
                                   @RequestParam String password,
                                   HttpSession session) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tên đăng nhập không tồn tại"));
        }
        
        User user = userOpt.get();
        
        // Simple password check (in production, use BCrypt)
        if (!user.getPassword().equals(password)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu không đúng"));
        }
        
        // Store user in session
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("fullName", user.getFullName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", user.getUserId());
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("success", true));
    }
    
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }
        
        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("loggedIn", true);
        response.put("userId", user.getUserId());
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        
        return ResponseEntity.ok(response);
    }
}

