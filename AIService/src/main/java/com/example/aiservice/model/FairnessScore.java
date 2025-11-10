package com.example.aiservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Điểm công bằng - so sánh mức sử dụng với tỷ lệ sở hữu
 */
@Entity
@Table(name = "fairness_score")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FairnessScore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scoreId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Long vehicleId;
    
    @Column(nullable = false)
    private Long groupId;
    
    /**
     * Tỷ lệ sở hữu (%)
     */
    @Column(name = "ownership_percentage")
    private Double ownershipPercentage;
    
    /**
     * Tỷ lệ sử dụng thực tế (%)
     */
    @Column(name = "usage_percentage")
    private Double usagePercentage;
    
    /**
     * Chênh lệch: usage - ownership
     * Dương = sử dụng nhiều hơn quyền sở hữu
     * Âm = sử dụng ít hơn quyền sở hữu
     */
    @Column(name = "difference")
    private Double difference;
    
    /**
     * Điểm công bằng (0-100)
     * 100 = hoàn toàn công bằng
     * < 100 = có chênh lệch
     */
    @Column(name = "fairness_score")
    private Double fairnessScore;
    
    /**
     * Mức độ ưu tiên cho lần đặt lịch tiếp theo
     * HIGH = cần ưu tiên vì dùng ít
     * NORMAL = bình thường
     * LOW = đã dùng nhiều, giảm ưu tiên
     */
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.NORMAL;
    
    /**
     * Kỳ phân tích bắt đầu
     */
    @Column(name = "period_start")
    private LocalDateTime periodStart;
    
    /**
     * Kỳ phân tích kết thúc
     */
    @Column(name = "period_end")
    private LocalDateTime periodEnd;
    
    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;
    
    public enum Priority {
        HIGH,    // Ưu tiên cao
        NORMAL,  // Bình thường
        LOW      // Ưu tiên thấp
    }
    
    @PrePersist
    protected void onCreate() {
        calculatedAt = LocalDateTime.now();
    }
}
















