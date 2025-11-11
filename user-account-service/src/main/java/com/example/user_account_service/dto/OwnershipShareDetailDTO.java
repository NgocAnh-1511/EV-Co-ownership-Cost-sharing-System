package com.example.user_account_service.dto;

import com.example.user_account_service.entity.OwnershipShare;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnershipShareDetailDTO {
    // Thông tin tỷ lệ
    private OwnershipShare share;
    // Thông tin xe (lấy từ VehicleService)
    private VehicleDTO vehicle;
}