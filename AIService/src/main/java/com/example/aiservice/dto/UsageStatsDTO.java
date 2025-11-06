package com.example.aiservice.dto;

import lombok.*;

/**
 * Thống kê sử dụng của một user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageStatsDTO {
    
    private Long userId;
    private String userName;
    
    /**
     * Tỷ lệ sở hữu
     */
    private Double ownershipPercentage;
    
    /**
     * Tỷ lệ sử dụng thực tế
     */
    private Double usagePercentage;
    
    /**
     * Tổng giờ sử dụng
     */
    private Double totalHoursUsed;
    
    /**
     * Tổng km đã đi
     */
    private Double totalKilometers;
    
    /**
     * Số lần đặt lịch
     */
    private Integer bookingCount;
    
    /**
     * Số lần hủy
     */
    private Integer cancellationCount;
    
    /**
     * Chi phí phát sinh
     */
    private Double costIncurred;
    
    /**
     * Chênh lệch usage vs ownership
     */
    private Double difference;
    
    /**
     * Điểm công bằng
     */
    private Double fairnessScore;
    
    /**
     * Mức độ ưu tiên
     */
    private String priority;
}




