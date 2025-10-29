package com.example.ui_service.repository;

import com.example.ui_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm bằng email
    Optional<User> findByEmail(String email);

    // MỚI: Thêm hàm tìm bằng Tên đăng nhập (đã lưu vào cột full_name)
    Optional<User> findByFullName(String fullName);
}