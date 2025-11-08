package com.example.user_account_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponse {
    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private String role; // <-- ĐÃ CÓ TRƯỜNG ROLE
}