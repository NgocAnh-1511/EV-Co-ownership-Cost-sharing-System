package com.example.user_account_service.dto;

import lombok.Data;

@Data
public class VehicleDTO {
    // Các trường này ĐÃ ĐƯỢC CẬP NHẬT để khớp với Vehicle.java

    private String vehicleId;

    // ĐỔI TÊN: (Đây là biển số xe)
    private String vehicleNumber;

    // ĐỔI TÊN: (Đây là tên xe)
    private String vehicleName;

    // ĐỔI TÊN: (Đây là loại xe)
    private String vehicleType;

    private String status;

    // (Chúng ta xóa brand, model, imageUrl vì chúng không tồn tại trong Vehicle.java)
}