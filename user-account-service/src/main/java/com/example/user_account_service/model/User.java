package com.example.user_account_service.model;

import jakarta.persistence.*;
import lombok.Getter; // <-- KIỂM TRA IMPORT NÀY
import lombok.Setter; // <-- KIỂM TRA IMPORT NÀY
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.time.LocalDate;

@Getter // <-- ĐẢM BẢO CÓ DÒNG NÀY
@Setter // <-- ĐẢM BẢO CÓ DÒNG NÀY
@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId; // -> Cần getUserId()

    @Column(unique = true, nullable = false, length = 255)
    private String email; // -> Cần getEmail(), setEmail()

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; // -> Cần setPasswordHash()

    @Column(name = "full_name", length = 255)
    private String fullName; // -> Cần setFullName()

    @Column(name = "phone_number", unique = true, length = 20)
    private String phoneNumber; // -> (Có thể cần getter/setter)

    @Column(name = "is_verified")
    private boolean isVerified = false; // -> (Có thể cần getter/setter)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt; // -> (Có thể cần getter/setter)

    @Column(name = "role", nullable = false)
    private String role; // -> Cần getRole(), setRole()

    // --- CÁC TRƯỜNG MỚI ---
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "id_card_number", length = 20, unique = true)
    private String idCardNumber;

    @Column(name = "id_card_issue_date")
    private LocalDate idCardIssueDate;

    @Column(name = "id_card_issue_place", length = 255)
    private String idCardIssuePlace;

    @Column(name = "license_number", length = 20, unique = true)
    private String licenseNumber;

    @Column(name = "license_class", length = 10)
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
    // --- KẾT THÚC TRƯỜNG MỚI ---
}