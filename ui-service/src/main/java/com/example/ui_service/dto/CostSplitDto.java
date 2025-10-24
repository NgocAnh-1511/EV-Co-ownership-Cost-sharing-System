package com.example.ui_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostSplitDto {
    private Long id;
    private Long costItemId;
    private String userId;
    private BigDecimal ownershipPercentage;
    private BigDecimal splitAmount;
    private BigDecimal paidAmount;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
