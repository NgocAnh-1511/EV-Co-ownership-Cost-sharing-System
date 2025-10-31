package com.example.user_account_service.service;

import com.example.user_account_service.dto.LoginRequest;
import com.example.user_account_service.dto.LoginResponse;
import com.example.user_account_service.dto.RegisterRequest;
import com.example.user_account_service.entity.User;
import com.example.user_account_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // THÊM 2 DÒNG NÀY
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;

    public User registerUser(RegisterRequest request) {
        // (Code đăng ký của bạn giữ nguyên...)
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Lỗi: Email đã được đăng ký!");
        }
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPasswordHash(hashedPassword);
        user.setRole("ROLE_USER");
        user.setVerified(false);
        user.setCreatedAt(Timestamp.from(Instant.now()));
        return userRepository.save(user);
    }

    /**
     * THÊM PHƯƠNG THỨC NÀY VÀO
     */
    public LoginResponse loginUser(LoginRequest request) {
        // 1. Xác thực người dùng (email, password)
        // Spring Security sẽ tự động gọi UserDetailsService và PasswordEncoder
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Nếu xác thực thành công, lấy thông tin user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Lỗi không xác định sau khi xác thực"));

        // 3. Tạo JWT Token
        String token = jwtService.generateToken(user);

        // 4. Trả về Response
        return LoginResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}