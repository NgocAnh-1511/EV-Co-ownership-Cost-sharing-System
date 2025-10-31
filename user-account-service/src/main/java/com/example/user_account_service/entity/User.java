package com.example.user_account_service.entity;

import jakarta.persistence.*;
// THÊM CÁC IMPORT CỦA LOMBOK
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.sql.Timestamp;

@Entity
@Table(name = "Users")
@Getter // <-- PHẢI CÓ
@Setter // <-- PHẢI CÓ
@NoArgsConstructor
@AllArgsConstructor
public class User {
    // (Toàn bộ code của class User mà tôi đã gửi ở tin nhắn trước)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // ... (và tất cả các trường khác) ...
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "is_verified")
    private boolean isVerified = false;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "role", nullable = false)
    private String role = "ROLE_USER";

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "id_card_number", unique = true)
    private String idCardNumber;

    @Column(name = "id_card_issue_date")
    private LocalDate idCardIssueDate;

    @Column(name = "id_card_issue_place")
    private String idCardIssuePlace;

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    @Column(name = "license_class")
    private String licenseClass;

    @Column(name = "license_issue_date")
    private LocalDate licenseIssueDate;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Column(name = "id_card_front_url", length = 512)
    private String idCardFrontUrl;

    @Column(name = "id_card_back_url", length = 512)
    private String idCardBackUrl;

    @Column(name = "license_image_url", length = 512)
    private String licenseImageUrl;

    @Column(name = "portrait_image_url", length = 512)
    private String portraitImageUrl;
}