package com.example.aiservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Phân tích sử dụng xe của từng co-owner
 */
@Entity
@Table(name = "usage_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long analysisId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Long vehicleId;
    
    @Column(nullable = false)
    private Long groupId;
    
    /**
     * Tổng số giờ sử dụng trong kỳ phân tích
     */
    @Column(name = "total_hours_used")
    private Double totalHoursUsed;
    
    /**
     * Tổng số km đã đi (nếu có tracking)
     */
    @Column(name = "total_kilometers")
    private Double totalKilometers;
    
    /**
     * Số lần đặt lịch
     */
    @Column(name = "booking_count")
    private Integer bookingCount;
    
    /**
     * Số lần hủy lịch
     */
    @Column(name = "cancellation_count")
    private Integer cancellationCount;
    
    /**
     * Tỷ lệ sử dụng thực tế (%)
     */
    @Column(name = "usage_percentage")
    private Double usagePercentage;
    
    /**
     * Chi phí phát sinh do user này (điện, bảo dưỡng...)
     */
    @Column(name = "cost_incurred")
    private Double costIncurred;
    
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
    
    /**
     * Ngày phân tích
     */
    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;
    
    @PrePersist
    protected void onCreate() {
        analyzedAt = LocalDateTime.now();
    }
}



