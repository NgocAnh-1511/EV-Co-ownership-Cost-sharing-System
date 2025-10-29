package com.example.ui_service.service;

import com.example.ui_service.model.User;
import com.example.ui_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Import mới
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List; // Import mới

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // "username" ở đây là giá trị từ form (Tên đăng nhập hoặc Email)

        // 1. Thử tìm bằng email trước
        User user = userRepository.findByEmail(username)
                // 2. Nếu không thấy email, thử tìm bằng Tên đăng nhập (lưu ở cột fullName)
                .orElseGet(() -> userRepository.findByFullName(username)
                        // 3. Nếu không tìm thấy, ném lỗi
                        .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username)));

        // --- PHẦN NÂNG CẤP ---
        // 4. Tạo một quyền (authority) từ cột 'role' trong CSDL
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());

        // 5. Trả về UserDetails với danh sách quyền (chứa 1 quyền)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // Tên người dùng (chính)
                user.getPasswordHash(), // Mật khẩu đã mã hóa
                List.of(authority) // <-- Đã được nâng cấp từ danh sách rỗng
        );
        // --- KẾT THÚC NÂNG CẤP ---
    }
}