package com.example.user_account_service.service;

import com.example.user_account_service.dto.LoginRequest;
import com.example.user_account_service.dto.LoginResponse;
import com.example.user_account_service.dto.RegisterRequest;
import com.example.user_account_service.dto.UserProfileUpdateRequest;
import com.example.user_account_service.entity.User;
import com.example.user_account_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List; // <-- Import cho List
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;

    // Tìm user bằng email (hỗ trợ Controller và Security)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Logic Đăng ký người dùng mới.
     * Mặc định gán ROLE dựa trên logic đơn giản (ví dụ: email chứa '@admin.com').
     */
    public User registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Lỗi: Email đã được đăng ký!");
        }

        String assignedRole = request.getEmail().contains("@admin.com") ? "ROLE_ADMIN" : "ROLE_USER";

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .isVerified(false)
                // createdAt và profileStatus sẽ được tự động gán bởi @PrePersist
                .build();

        return userRepository.save(user);
    }

    /**
     * Logic Đăng nhập: Xác thực và trả về Role.
     */
    public LoginResponse loginUser(LoginRequest request) {
        // 1. Xác thực người dùng
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Lấy thông tin user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Lỗi không xác định sau khi xác thực"));

        // 3. Tạo JWT Token
        String token = jwtService.generateToken(user);

        // 4. Trả về Response có kèm Role
        return LoginResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    /**
     * Logic Cập nhật hồ sơ (Onboarding)
     * Đặt lại trạng thái về PENDING để Admin duyệt.
     */
    public User updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng."));

        // Cập nhật các trường từ DTO
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());

        // Giấy tờ tùy thân
        if (request.getIdCardNumber() != null) user.setIdCardNumber(request.getIdCardNumber());
        if (request.getIdCardIssueDate() != null) user.setIdCardIssueDate(request.getIdCardIssueDate());
        if (request.getIdCardIssuePlace() != null) user.setIdCardIssuePlace(request.getIdCardIssuePlace());

        // Giấy phép lái xe
        if (request.getLicenseNumber() != null) user.setLicenseNumber(request.getLicenseNumber());
        if (request.getLicenseClass() != null) user.setLicenseClass(request.getLicenseClass());
        if (request.getLicenseIssueDate() != null) user.setLicenseIssueDate(request.getLicenseIssueDate());
        if (request.getLicenseExpiryDate() != null) user.setLicenseExpiryDate(request.getLicenseExpiryDate());

        // URL Hình ảnh (Giả định URL đã được upload thành công từ API riêng)
        if (request.getIdCardFrontUrl() != null) user.setIdCardFrontUrl(request.getIdCardFrontUrl());
        if (request.getIdCardBackUrl() != null) user.setIdCardBackUrl(request.getIdCardBackUrl());
        if (request.getLicenseImageUrl() != null) user.setLicenseImageUrl(request.getLicenseImageUrl());
        if (request.getPortraitImageUrl() != null) user.setPortraitImageUrl(request.getPortraitImageUrl());

        // Đặt lại trạng thái hồ sơ về "Đang chờ" để Admin duyệt lại
        user.setProfileStatus("PENDING");
        user.setVerified(false);

        return userRepository.save(user);
    }

    // --- CÁC HÀM DÀNH CHO ADMIN ---

    /**
     * (ADMIN) Lấy tất cả hồ sơ đang chờ duyệt
     */
    public List<User> getPendingProfiles() {
        return userRepository.findByProfileStatus("PENDING");
    }

    /**
     * (ADMIN) Duyệt (Approve) hồ sơ
     */
    public User approveProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng."));

        user.setProfileStatus("APPROVED");
        user.setVerified(true); // Đánh dấu đã xác thực
        return userRepository.save(user);
    }

    /**
     * (ADMIN) Từ chối (Reject) hồ sơ
     */
    public User rejectProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng."));

        user.setProfileStatus("REJECTED");
        user.setVerified(false); // Đánh dấu chưa xác thực
        return userRepository.save(user);
    }
}