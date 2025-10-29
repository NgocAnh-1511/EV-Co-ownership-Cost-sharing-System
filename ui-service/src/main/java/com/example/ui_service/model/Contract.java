package com.example.ui_service.model;

import jakarta.persistence.*; // Import các annotation JPA
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate; // Import LocalDate cho ngày
import java.time.Instant; // Import Instant cho timestamp
import com.example.ui_service.model.Asset; // Import Asset để tạo liên kết
import org.hibernate.annotations.CreationTimestamp; // Import CreationTimestamp
import java.util.List; // <-- THÊM IMPORT NÀY
import jakarta.persistence.OneToMany; // <-- THÊM IMPORT NÀY
import jakarta.persistence.CascadeType; // <-- THÊM IMPORT NÀY (Tùy chọn)

@Getter
@Setter
@Entity // Đánh dấu đây là một Entity
@Table(name = "Contracts") // Map với bảng "Contracts" trong CSDL
public class Contract {

    @Id // Đánh dấu là khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tự động tăng (cho MySQL AUTO_INCREMENT)
    @Column(name = "contract_id")
    private Long contractId;

    // Liên kết ManyToOne tới Asset (Nhiều hợp đồng có thể thuộc về một tài sản)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false) // Map với cột khóa ngoại "asset_id"
    private Asset asset;

    @Column(name = "title", nullable = false, length = 255) // Tiêu đề hợp đồng, không được null
    private String title;

    @Column(name = "document_url", length = 512) // Đường dẫn file hợp đồng
    private String documentUrl;

    @Column(length = 50) // Trạng thái hợp đồng
    private String status;

    @Column(name = "start_date", nullable = false) // Ngày bắt đầu, không được null
    private LocalDate startDate;

    @Column(name = "end_date") // Ngày kết thúc, có thể null
    private LocalDate endDate;

    @CreationTimestamp // Tự động gán thời gian khi tạo mới bản ghi
    @Column(name = "created_at", updatable = false) // Không cho phép cập nhật cột này
    private Instant createdAt;

    // --- THÊM LIÊN KẾT NGƯỢC ---
    // Một hợp đồng có thể liên quan đến nhiều bản ghi Ownership
    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY, cascade = CascadeType.ALL) // mappedBy trỏ đến trường "contract" trong Ownership.java
    private List<Ownership> ownerships;
    // --- KẾT THÚC THÊM LIÊN KẾT ---

    // Constructors, Getters, Setters được Lombok tự động tạo
}