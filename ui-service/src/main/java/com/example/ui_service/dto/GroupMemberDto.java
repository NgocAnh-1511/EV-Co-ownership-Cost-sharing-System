package com.example.ui_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberDto {
    private Long id;
    private String userId;
    private Long groupId;
    private Double ownershipPercentage;
    private String role;
    private String status;
    private Double totalContribution;
    private Double totalUsage;
    private LocalDateTime joinedAt;
    private LocalDateTime updatedAt;
}
