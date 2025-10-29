package com.example.ui_service.service;

import com.example.ui_service.dto.RegistrationDto;
import com.example.ui_service.model.User; // Import User
import java.util.List; // Import List

public interface UserService {
    void register(RegistrationDto registrationDto) throws Exception;

    // --- NEW METHODS ---
    List<User> findAllUsers(); // Lấy danh sách người dùng
    void promoteUserToAdmin(Long userId) throws Exception; // Nâng cấp vai trò
    // --- END NEW METHODS ---
}