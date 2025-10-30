package com.example.user_account_service.model;

import jakarta.persistence.*;
import lombok.Getter; // <-- ĐẢM BẢO CÓ IMPORT NÀY
import lombok.Setter;
import java.util.List;
// import com.example.ui_service.model.Ownership; // Bỏ comment nếu dùng liên kết ownerships

@Getter // <-- ĐẢM BẢO CÓ CHÚ THÍCH NÀY
@Setter
@Entity
@Table(name = "Assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private Long assetId;

    // --- KIỂM TRA TÊN TRƯỜNG ---
    @Column(name = "asset_name", nullable = false)
    private String assetName; // <-- Tên trường phải là 'assetName'

    @Column(unique = true)
    private String identifier; // <-- Tên trường phải là 'identifier'
    // --- KẾT THÚC KIỂM TRA ---

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_value", precision = 12, scale = 2)
    private java.math.BigDecimal totalValue;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    // @OneToMany(mappedBy = "asset", fetch = FetchType.LAZY)
    // private List<Ownership> ownerships;
}