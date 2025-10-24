package com.example.ui_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteDto {
    private Long id;
    private Long groupId;
    private String title;
    private String description;
    private String voteType;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer requiredPercentage;
    private List<VoteOptionDto> options;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
