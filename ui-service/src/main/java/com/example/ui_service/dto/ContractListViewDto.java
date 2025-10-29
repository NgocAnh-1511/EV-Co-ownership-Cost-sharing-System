package com.example.ui_service.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ContractListViewDto {
    private Long contractId;
    private String contractCode; // Ví dụ: HD001 (title?)
    private String vehicleInfo; // Ví dụ: "Toyota Camry 2023"
    private String vehicleIdentifier; // Ví dụ: "23A-12345"
    private String vehicleImageUrl;
    private LocalDateTime signingDateTime; // Ngày ký (kết hợp ngày + giờ?)
    private String duration; // Thời hạn (ví dụ: "24 tháng")
    private String status; // Trạng thái (ví dụ: "Đang hoạt động", "Chờ ký")

    // Constructor (hoặc dùng Builder pattern) để dễ tạo đối tượng
    public ContractListViewDto(Long contractId, String contractCode, String vehicleInfo, String vehicleIdentifier, String vehicleImageUrl, LocalDateTime signingDateTime, String duration, String status) {
        this.contractId = contractId;
        this.contractCode = contractCode;
        this.vehicleInfo = vehicleInfo;
        this.vehicleIdentifier = vehicleIdentifier;
        this.vehicleImageUrl = vehicleImageUrl;
        this.signingDateTime = signingDateTime;
        this.duration = duration;
        this.status = status;
    }
}