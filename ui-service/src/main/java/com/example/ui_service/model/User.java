package com.example.ui_service.model; // <-- Phải khớp với package bị lỗi

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;

// LƯU Ý: KHÔNG CÓ @Entity, @Table, @Id, @Column...
// Đây chỉ là một lớp POJO (khuôn chứa dữ liệu)
@Getter
@Setter
public class User {

    // Giữ lại TẤT CẢ các trường
    private Long userId;
    private String email;
    private String passwordHash; // Cần cho UserDetailsServiceImp
    private String fullName;
    private String phoneNumber;
    private boolean isVerified;
    private Instant createdAt;
    private String role; // Cần cho UserDetailsServiceImp

    // Các trường hồ sơ
    private LocalDate dateOfBirth;
    private String idCardNumber;
    private LocalDate idCardIssueDate;
    private String idCardIssuePlace;
    private String licenseNumber;
    private String licenseClass;
    private LocalDate licenseIssueDate;
    private LocalDate licenseExpiryDate;
    private String idCardFrontUrl;
    private String idCardBackUrl;
    private String licenseImageUrl;
    private String portraitImageUrl;
}