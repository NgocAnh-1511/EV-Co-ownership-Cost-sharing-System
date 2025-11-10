package com.example.aiservice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response với gợi ý thời gian đặt lịch
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleSuggestionResponse {
    
    private Long userId;
    private Long vehicleId;
    
    /**
     * Có được phép đặt lịch không (dựa trên fairness)
     */
    private Boolean approved;
    
    /**
     * Lý do
     */
    private String reason;
    
    /**
     * Danh sách thời gian được gợi ý
     */
    private List<TimeSlot> suggestedTimeSlots;
    
    /**
     * Điểm ưu tiên hiện tại
     */
    private String currentPriority;
    
    /**
     * Tỷ lệ sử dụng hiện tại
     */
    private Double currentUsagePercentage;
    
    /**
     * Tỷ lệ sở hữu
     */
    private Double ownershipPercentage;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSlot {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String description;
        private Integer priority; // 1 = cao nhất, 2, 3...
    }
}
















