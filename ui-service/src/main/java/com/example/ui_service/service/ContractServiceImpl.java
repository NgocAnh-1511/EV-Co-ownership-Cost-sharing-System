package com.example.ui_service.service;

import com.example.ui_service.dto.ContractCreationDto; // <-- Import DTO mới
import com.example.ui_service.dto.ContractListViewDto;
import com.example.ui_service.dto.ContractSummaryDto;
import com.example.ui_service.model.Asset; // <-- Import Asset
import com.example.ui_service.model.Contract;
import com.example.ui_service.model.Ownership; // <-- Import Ownership
import com.example.ui_service.model.User;
import com.example.ui_service.repository.AssetRepository; // <-- Import AssetRepository
import com.example.ui_service.repository.ContractRepository;
import com.example.ui_service.repository.OwnershipRepository; // <-- Import OwnershipRepository
import com.example.ui_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException; // <-- Import Exception
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- Import Transactional

import java.math.BigDecimal; // <-- Import BigDecimal
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
public class ContractServiceImpl implements ContractService {

    @Autowired private ContractRepository contractRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AssetRepository assetRepository; // <-- Inject AssetRepository
    @Autowired private OwnershipRepository ownershipRepository; // <-- Inject OwnershipRepository

    // ... (getCurrentUserId, getContractSummaryForUser, getContractsForUser, mapContractToListViewDto, calculateDuration, mapStatus giữ nguyên) ...

    // --- PHƯƠNG THỨC MỚI ---
    @Override
    @Transactional // Đảm bảo tất cả lưu trữ thành công hoặc rollback nếu có lỗi
    public void createContract(ContractCreationDto dto, Long userId) throws Exception {
        // 1. Tìm User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với ID: " + userId));

        // 2. Tìm Asset
        Asset asset = assetRepository.findById(dto.getAssetId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài sản với ID: " + dto.getAssetId()));

        // 3. Tạo đối tượng Contract
        Contract newContract = new Contract();
        newContract.setTitle(dto.getTitle());
        newContract.setAsset(asset); // Liên kết với Asset đã tìm thấy
        newContract.setStartDate(dto.getStartDate());
        newContract.setEndDate(dto.getEndDate());
        newContract.setStatus("pending"); // Đặt trạng thái ban đầu là "Chờ ký"
        // createdAt sẽ tự động được gán bởi @CreationTimestamp

        // 4. Lưu Contract vào CSDL (để lấy contractId)
        Contract savedContract = contractRepository.save(newContract);

        // 5. Tạo đối tượng Ownership để liên kết User và Contract
        Ownership newOwnership = new Ownership();
        newOwnership.setUser(user);
        newOwnership.setContract(savedContract); // Liên kết với Contract vừa lưu
        newOwnership.setAsset(asset); // Liên kết với Asset
        newOwnership.setOwnershipPercentage(new BigDecimal("100.00")); // Tạm gán 100% cho người tạo

        // 6. Lưu Ownership vào CSDL
        ownershipRepository.save(newOwnership);
    }
    // --- KẾT THÚC ---

    // ... (getCurrentUserId, getContractSummaryForUser, ...)

    // Helper method để lấy User ID của người đang đăng nhập (Giữ nguyên)
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Người dùng chưa đăng nhập");
        }
        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + userEmail))
                .getUserId();
    }


    @Override
    public ContractSummaryDto getContractSummaryForUser(Long userId) {
        long total = contractRepository.countByUser(userId);
        long active = contractRepository.countByStatusAndUser(userId, "active");
        long pending = contractRepository.countByStatusAndUser(userId, "pending");
        long finished = contractRepository.countByStatusAndUser(userId, "expired"); // Hoặc cả "terminated"

        return new ContractSummaryDto(total, active, pending, finished);
    }

    @Override
    public Page<ContractListViewDto> getContractsForUser(Long userId, Pageable pageable) {
        Page<Contract> contractPage = contractRepository.findContractsByUserIdWithAsset(userId, pageable);
        return contractPage.map(this::mapContractToListViewDto);
    }

    private ContractListViewDto mapContractToListViewDto(Contract contract) {
        String duration = calculateDuration(contract.getStartDate(), contract.getEndDate());
        LocalDateTime signingDateTime = contract.getCreatedAt() != null ?
                LocalDateTime.ofInstant(contract.getCreatedAt(), ZoneId.systemDefault()) : null;

        return new ContractListViewDto(
                contract.getContractId(),
                contract.getTitle(),
                (contract.getAsset() != null) ? contract.getAsset().getAssetName() : "N/A",
                (contract.getAsset() != null) ? contract.getAsset().getIdentifier() : "N/A",
                (contract.getAsset() != null) ? contract.getAsset().getImageUrl() : null,
                signingDateTime,
                duration,
                mapStatus(contract.getStatus())
        );
    }

    private String calculateDuration(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "Không xác định";
        }
        Period period = Period.between(startDate, endDate);
        int totalMonths = period.getYears() * 12 + period.getMonths();
        if (totalMonths > 0) {
            return totalMonths + " tháng";
        } else {
            long days = ChronoUnit.DAYS.between(startDate, endDate);
            return (days >= 0 ? days + 1 : 0) + " ngày";
        }
    }

    private String mapStatus(String dbStatus) {
        if (dbStatus == null) return "Không rõ";
        switch (dbStatus.toLowerCase()) {
            case "active": return "Đang hoạt động";
            case "pending": return "Chờ ký";
            case "expired":
            case "terminated": return "Đã kết thúc";
            default: return dbStatus;
        }
    }
}