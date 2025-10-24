package com.example.ui_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostSplitDto {
    private Integer shareId;
    private Integer costId;
    private Integer userId;
    private Double percent;
    private Double amountShare;
}