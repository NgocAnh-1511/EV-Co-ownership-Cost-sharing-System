package com.example.user_account_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "Contracts") // Bảng mới trong CoOwnershipDB
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contractId;

    @Column(nullable = false)
    private String title;

    // ID của User (trong cùng CSDL này)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ID của Vehicle (từ dịch vụ bên ngoài)
    @Column(name = "external_vehicle_id", nullable = false)
    private String vehicleId;

    // PENDING, ACTIVE, EXPIRED
    @Column(nullable = false)
    private String status;

    private LocalDate signDate;
    private LocalDate expiryDate;

    @Column(updatable = false)
    private Timestamp createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = Timestamp.from(Instant.now());
        if (this.status == null) this.status = "PENDING";
    }
}