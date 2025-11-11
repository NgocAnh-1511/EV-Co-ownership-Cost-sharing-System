package com.example.dispute_management_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "Disputes") // Tên của bảng trong CSDL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispute_id")
    private Long disputeId; // Khóa chính

    // --- THÔNG TIN LIÊN KẾT ---

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "accused_user_id")
    private Long accusedUserId;

    // --- THÔNG TIN TRANH CHẤP ---

    @Column(name = "subject", nullable = false, length = 255)
    private String subject; // Chủ đề

    @Lob
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description; // Mô tả chi tiết

    // TRẠNG THÁI: PENDING, PROCESSING, WAITING_RESPONSE, RESOLVED, CLOSED
    @Column(name = "status", nullable = false)
    private String status;

    // --- THỜI GIAN ---

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "resolved_at")
    private Timestamp resolvedAt; // Ngày giải quyết

    /**
     * Tự động gán giá trị khi tạo mới
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Timestamp.from(Instant.now());
        }
        if (this.status == null) {
            this.status = "PENDING"; // Mặc định là "Đang chờ"
        }
        this.updatedAt = Timestamp.from(Instant.now());
    }

    /**
     * Tự động cập nhật thời gian khi cập nhật
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Timestamp.from(Instant.now());
    }
}