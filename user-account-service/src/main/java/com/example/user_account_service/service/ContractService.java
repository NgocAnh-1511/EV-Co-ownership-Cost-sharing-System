package com.example.user_account_service.service;

import com.example.user_account_service.dto.ContractDetailDTO;
import com.example.user_account_service.dto.VehicleDTO;
import com.example.user_account_service.entity.Contract;
import com.example.user_account_service.repository.ContractRepository;

// (Xóa các import trỏ đến VehicleServiceManagementService)

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    // Hoàn tác: Dùng lại VehicleDataClient
    @Autowired
    private VehicleDataClient vehicleDataClient;

    public List<ContractDetailDTO> getHydratedContractsByUserId(Long userId) {
        List<Contract> contracts = contractRepository.findByUserId(userId);

        return contracts.stream()
                .map(contract -> {
                    // Gọi API qua client
                    VehicleDTO vehicle = vehicleDataClient.getVehicleById(contract.getVehicleId());
                    return new ContractDetailDTO(contract, vehicle);
                })
                .collect(Collectors.toList());
    }

    public Contract createContract(Long userId, String vehicleId, int durationMonths) {
        // Gọi API qua client để kiểm tra xe
        VehicleDTO vehicle = vehicleDataClient.getVehicleById(vehicleId);
        if (vehicle == null) {
            throw new RuntimeException("Không tìm thấy xe với ID: " + vehicleId + " (hoặc VehicleService đang tắt)");
        }

        Contract contract = Contract.builder()
                .userId(userId)
                .vehicleId(vehicleId)
                // SỬA LỖI: Dùng vehicleName (từ DTO)
                .title("HĐ Đồng sở hữu " + (vehicle.getVehicleName() != null ? vehicle.getVehicleName() : "Xe " + vehicle.getVehicleId()))
                .status("PENDING")
                .signDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusMonths(durationMonths))
                .build();

        return contractRepository.save(contract);
    }
}