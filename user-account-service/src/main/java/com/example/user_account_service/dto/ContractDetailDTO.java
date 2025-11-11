package com.example.user_account_service.dto;

import com.example.user_account_service.entity.Contract;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractDetailDTO {

    // Thông tin từ Contract (Project 1)
    private Contract contract;

    // Thông tin từ Vehicle (Project 2)
    private VehicleDTO vehicle;
}