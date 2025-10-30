package com.example.user_account_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractSummaryDto {
    private long totalContracts;
    private long activeContracts;
    private long pendingContracts; // "Chờ ký"
    private long finishedContracts; // "Đã kết thúc"
}