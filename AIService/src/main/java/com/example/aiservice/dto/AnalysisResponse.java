package com.example.aiservice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response chứa kết quả phân tích và gợi ý
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResponse {
    
    private Long groupId;
    private Long vehicleId;
    private String vehicleName;
    
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime analyzedAt;
    
    /**
     * Danh sách thống kê của từng owner
     */
    private List<UsageStatsDTO> ownerStats;
    
    /**
     * Điểm công bằng chung của nhóm (0-100)
     */
    private Double groupFairnessScore;
    
    /**
     * Tổng chi phí của nhóm trong kỳ
     */
    private Double totalGroupCost;
    
    /**
     * Danh sách gợi ý từ AI
     */
    private List<RecommendationDTO> recommendations;
    
    /**
     * Tóm tắt tình hình
     */
    private String summary;
}












