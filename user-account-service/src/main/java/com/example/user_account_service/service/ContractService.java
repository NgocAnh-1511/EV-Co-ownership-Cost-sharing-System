package com.example.user_account_service.service;

import com.example.user_account_service.dto.ContractDetailDTO;
import com.example.user_account_service.dto.VehicleDTO;
import com.example.user_account_service.entity.Contract;
import com.example.user_account_service.repository.ContractRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.nio.file.AccessDeniedException; // <-- THÊM IMPORT
import java.time.LocalDate;
import java.util.List;
import java.util.Objects; // <-- THÊM IMPORT
import java.util.stream.Collectors;

@Service
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private VehicleDataClient vehicleDataClient;

    public List<ContractDetailDTO> getHydratedContractsByUserId(Long userId) {
        List<Contract> contracts = contractRepository.findByUserId(userId);

        return contracts.stream()
                .map(contract -> {
                    VehicleDTO vehicle = vehicleDataClient.getVehicleById(contract.getVehicleId());
                    return new ContractDetailDTO(contract, vehicle);
                })
                .collect(Collectors.toList());
    }

    public Contract createContract(Long userId, String vehicleId, int durationMonths) {
        VehicleDTO vehicle = vehicleDataClient.getVehicleById(vehicleId);
        if (vehicle == null) {
            throw new RuntimeException("Không tìm thấy xe với ID: " + vehicleId + " (hoặc VehicleService đang tắt)");
        }

        Contract contract = Contract.builder()
                .userId(userId)
                .vehicleId(vehicleId)
                .title("HĐ Đồng sở hữu " + (vehicle.getVehicleName() != null ? vehicle.getVehicleName() : "Xe " + vehicle.getVehicleId()))
                .status("PENDING")
                .signDate(LocalDate.now()) // Ngày ký là ngày tạo
                .expiryDate(LocalDate.now().plusMonths(durationMonths))
                .build();

        return contractRepository.save(contract);
    }

    // --- HÀM MỚI ĐỂ KÝ HỢP ĐỒNG ---
    /**
     * Ký hợp đồng: Chuyển trạng thái từ PENDING sang ACTIVE
     * @param contractId ID của hợp đồng (trong CoOwnershipDB)
     * @param userId ID của người dùng đang ký
     * @return Hợp đồng đã cập nhật
     */
    public Contract signContract(Long contractId, Long userId) throws AccessDeniedException {
        // 1. Tìm hợp đồng
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng #" + contractId));

        // 2. Kiểm tra quyền sở hữu
        if (!Objects.equals(contract.getUserId(), userId)) {
            throw new AccessDeniedException("Bạn không có quyền ký hợp đồng này.");
        }

        // 3. Kiểm tra trạng thái (Chỉ ký được khi đang PENDING)
        if (!"PENDING".equals(contract.getStatus())) {
            throw new RuntimeException("Hợp đồng này không ở trạng thái 'Chờ ký'.");
        }

        // 4. Cập nhật trạng thái
        contract.setStatus("ACTIVE");
        contract.setSignDate(LocalDate.now()); // Cập nhật lại ngày ký

        // (Sau này, bạn có thể thêm logic gọi LegalContractService (Cổng 8084) tại đây)

        // 5. Lưu lại
        return contractRepository.save(contract);
    }
}