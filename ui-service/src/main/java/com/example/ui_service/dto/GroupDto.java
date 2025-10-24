package com.example.ui_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {
    private Integer groupId;
    private String groupName;
    private Integer adminId;
    private Integer vehicleId;
    private String status;
}