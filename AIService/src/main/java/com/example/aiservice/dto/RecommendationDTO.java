package com.example.aiservice.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO cho AI recommendation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationDTO {
    
    private Long recommendationId;
    private String type;
    private String title;
    private String description;
    private String severity;
    private Long targetUserId;
    private String targetUserName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}


















