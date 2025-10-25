package com.example.ui_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostDto {
    private Integer costId;
    private Integer vehicleId;
    private String costType;
    private Double amount;
    private String description;
}
