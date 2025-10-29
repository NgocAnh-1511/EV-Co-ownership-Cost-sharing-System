package com.example.ui_service.model;

import com.example.ui_service.model.Asset; // Import Asset
import com.example.ui_service.model.Contract; // Import Contract
import com.example.ui_service.model.User; // Import User
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal; // Import BigDecimal cho tỷ lệ phần trăm

@Getter
@Setter
@Entity
@Table(name = "Ownership")
public class Ownership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ownership_id")
    private Long ownershipId;

    // --- Liên kết ManyToOne tới User ---
    @ManyToOne(fetch = FetchType.LAZY) // Lazy loading để tối ưu
    @JoinColumn(name = "user_id", nullable = false) // Tên cột khóa ngoại trong DB
    private User user;

    // --- Liên kết ManyToOne tới Asset ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false) // Tên cột khóa ngoại trong DB
    private Asset asset; // Tên trường này ("asset") phải khớp với mappedBy trong Asset.java

    // --- Liên kết ManyToOne tới Contract ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false) // Tên cột khóa ngoại trong DB
    private Contract contract;

    // Tỷ lệ sở hữu
    @Column(name = "ownership_percentage", nullable = false, precision = 5, scale = 2) // precision=tổng số chữ số, scale=số chữ số sau dấu phẩy
    private BigDecimal ownershipPercentage;

    // Constructors, Getters, Setters được Lombok tự động tạo
}