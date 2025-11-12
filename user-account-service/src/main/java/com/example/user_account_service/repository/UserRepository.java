package com.example.user_account_service.repository;

import com.example.user_account_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // <-- THÊM IMPORT
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByIdCardNumber(String idCardNumber);

    // CẬP NHẬT: Thêm phương thức tìm kiếm theo trạng thái
    // Tự động tạo query: "SELECT * FROM Users WHERE profile_status = ?"
    List<User> findByProfileStatus(String status);
}