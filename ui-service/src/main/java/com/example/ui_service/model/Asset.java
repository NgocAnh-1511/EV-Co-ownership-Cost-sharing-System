package com.example.ui_service.model;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

// LƯU Ý: KHÔNG CÓ @Entity, @Table, @Id, @Column...
// Đây chỉ là một lớp POJO (khuôn chứa dữ liệu)
@Getter
@Setter
public class Asset {
    // Giữ lại các trường dữ liệu khớp với API backend
    private Long assetId;
    private String assetName;
    private String identifier; // Ví dụ: Biển số xe
    private String description;
    private BigDecimal totalValue;
    private String imageUrl;
}