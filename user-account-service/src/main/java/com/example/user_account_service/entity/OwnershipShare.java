package com.example.user_account_service.entity;

// NÂNG CẤP: Thêm import cho Lombok
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

// NÂNG CẤP: Thêm @Getter và @Setter
@Entity
@Table(name = "ownership_share", uniqueConstraints = {
        @UniqueConstraint(name = "uq_vehicle_user", columnNames = {"vehicle_id", "user_id"})
})
@Getter
@Setter
public class OwnershipShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId; // <-- Đã là String (chính xác)

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage; // 0.00 – 100.00

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @Column(columnDefinition = "timestamp default current_timestamp")
    private OffsetDateTime createdAt;

    @Column(columnDefinition = "timestamp default current_timestamp on update current_timestamp")
    private OffsetDateTime updatedAt;

    // Không cần viết getters/setters thủ công nữa
}