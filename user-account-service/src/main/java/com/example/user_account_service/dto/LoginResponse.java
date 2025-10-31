package com.example.user_account_service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder // Dùng Builder pattern để tạo đối tượng dễ dàng
public class LoginResponse {
    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private String role;
}