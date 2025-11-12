package com.example.aiservice.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Request để xin gợi ý thời gian đặt lịch phù hợp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleSuggestionRequest {
    
    private Long userId;
    private Long vehicleId;
    private Long groupId;
    
    /**
     * Thời gian muốn bắt đầu sử dụng
     */
    private LocalDateTime desiredStartTime;
    
    /**
     * Số giờ dự kiến sử dụng
     */
    private Double estimatedHours;
}












