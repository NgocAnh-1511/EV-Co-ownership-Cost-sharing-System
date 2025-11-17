package com.example.aiservice.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Request để yêu cầu phân tích sử dụng xe
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisRequest {
    
    private Long groupId;
    private Long vehicleId;
    
    /**
     * Thời gian bắt đầu phân tích
     */
    private LocalDateTime periodStart;
    
    /**
     * Thời gian kết thúc phân tích
     */
    private LocalDateTime periodEnd;
    
    /**
     * Loại phân tích: USAGE, FAIRNESS, COST, ALL
     */
    private AnalysisType analysisType = AnalysisType.ALL;
    
    public enum AnalysisType {
        USAGE,      // Chỉ phân tích usage
        FAIRNESS,   // Chỉ phân tích fairness
        COST,       // Chỉ phân tích cost
        ALL         // Phân tích tất cả
    }
}


















