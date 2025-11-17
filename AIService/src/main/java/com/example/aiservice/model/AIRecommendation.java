package com.example.aiservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Gợi ý từ AI cho việc sử dụng xe công bằng
 */
@Entity
@Table(name = "ai_recommendations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recommendationId;
    
    @Column(nullable = false)
    private Long groupId;
    
    @Column(nullable = false)
    private Long vehicleId;
    
    /**
     * Loại gợi ý
     */
    @Enumerated(EnumType.STRING)
    private RecommendationType type;
    
    /**
     * Tiêu đề gợi ý
     */
    @Column(length = 500)
    private String title;
    
    /**
     * Nội dung chi tiết gợi ý
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * Mức độ ưu tiên
     */
    @Enumerated(EnumType.STRING)
    private Severity severity = Severity.INFO;
    
    /**
     * User ID liên quan (nếu gợi ý cho 1 user cụ thể)
     */
    private Long targetUserId;
    
    /**
     * Trạng thái gợi ý
     */
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;
    
    /**
     * Kỳ phân tích
     */
    @Column(name = "period_start")
    private LocalDateTime periodStart;
    
    @Column(name = "period_end")
    private LocalDateTime periodEnd;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    public enum RecommendationType {
        FAIRNESS_ALERT,         // Cảnh báo công bằng
        PRIORITY_SUGGESTION,    // Gợi ý ưu tiên
        USAGE_PATTERN,          // Mẫu sử dụng
        USAGE_BALANCE,          // Cân bằng sử dụng
        COST_OPTIMIZATION,      // Tối ưu chi phí
        COST_SHARING,           // Chia sẻ chi phí
        MAINTENANCE_ALERT,      // Cảnh báo bảo dưỡng
        SCHEDULE_CONFLICT,      // Xung đột lịch
        GENERAL_ADVICE,         // Lời khuyên chung
        // Các giá trị từ dữ liệu mẫu
        USAGE_FAIRNESS,         // Khuyến nghị công bằng sử dụng
        BALANCE_USAGE,          // Cân bằng sử dụng giữa các thành viên
        OVERUSAGE_WARNING,      // Cảnh báo sử dụng quá mức
        MAINTENANCE_REMINDER,   // Nhắc nhở bảo dưỡng định kỳ
        VEHICLE_STATUS          // Trạng thái xe
    }
    
    public enum Severity {
        INFO,       // Thông tin
        WARNING,    // Cảnh báo
        CRITICAL    // Quan trọng
    }
    
    public enum Status {
        ACTIVE,     // Đang hoạt động
        READ,       // Đã đọc
        RESOLVED,   // Đã giải quyết
        DISMISSED   // Đã bỏ qua
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}


