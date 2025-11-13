package com.example.user_account_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Thêm Builder để dễ dàng tạo đối tượng trong UserService
public class User {

    // Thông tin cơ bản & Xác thực
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    // CẬP NHẬT: Thêm @Builder.Default để sửa cảnh báo
    @Builder.Default
    @Column(name = "is_verified")
    private boolean isVerified = false;

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @Column(name = "role", nullable = false)
    private String role;

    // CẬP NHẬT: Thêm trường group_id để liên kết với VehicleGroup
    /**
     * ID của nhóm (vehiclegroup) mà User này thuộc về.
     * Đây là ID tham chiếu đến bảng 'vehiclegroup' trong CSDL 'vehicle_management'.
     * Dùng kiểu String để khớp với kiểu dữ liệu của 'group_id' (GRP001, GRP002...)
     */
    @Column(name = "group_id")
    private String groupId;

    // THÊM TRƯỜNG MỚI ĐỂ THEO DÕI TRẠNG THÁI
    @Column(name = "profile_status")
    private String profileStatus; // Ví dụ: PENDING, APPROVED, REJECTED

    // Thông tin cá nhân
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    // Thông tin CMND/CCCD
    @Column(name = "id_card_number", unique = true)
    private String idCardNumber;

    @Column(name = "id_card_issue_date")
    private LocalDate idCardIssueDate;

    @Column(name = "id_card_issue_place")
    private String idCardIssuePlace;

    // Thông tin Giấy phép lái xe
    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    @Column(name = "license_class")
    private String licenseClass;

    @Column(name = "license_issue_date")
    private LocalDate licenseIssueDate;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    // URL Hình ảnh
    @Column(name = "id_card_front_url", length = 512)
    private String idCardFrontUrl;

    @Column(name = "id_card_back_url", length = 512)
    private String idCardBackUrl;

    @Column(name = "license_image_url", length = 512)
    private String licenseImageUrl;

    @Column(name = "portrait_image_url", length = 512)
    private String portraitImageUrl;

    // CẬP NHẬT: Thêm @PrePersist để đặt giá trị mặc định khi tạo mới
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Timestamp.from(Instant.now());
        }
        if (this.profileStatus == null) {
            this.profileStatus = "PENDING"; // Mặc định là "Đang chờ"
        }
    }
}