package com.example.user_account_service.repository;

import com.example.user_account_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
//                                     Và <User, Long>
public interface UserRepository extends JpaRepository<User, Long> {
// Quan trọng nhất là:              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    Optional<User> findByEmail(String email);

    // Bạn cũng có thể thêm các hàm này (từ schema)
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByIdCardNumber(String idCardNumber);
}