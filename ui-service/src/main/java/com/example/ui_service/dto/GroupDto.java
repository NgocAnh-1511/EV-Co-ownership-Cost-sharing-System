package com.example.ui_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {
    private Long id;
    private String groupName;
    private String description;
    private String vehicleId;
    private String groupAdminId;
    private String status;
    private Double totalOwnershipPercentage;
    private List<GroupMemberDto> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
