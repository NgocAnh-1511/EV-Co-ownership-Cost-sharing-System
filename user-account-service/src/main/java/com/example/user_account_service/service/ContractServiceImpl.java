package com.example.user_account_service.service;

import com.example.user_account_service.dto.ContractCreationDto;
import com.example.user_account_service.dto.ContractListViewDto;
import com.example.user_account_service.dto.ContractSummaryDto;
import com.example.user_account_service.model.Asset;
import com.example.user_account_service.model.Contract;
import com.example.user_account_service.model.Ownership;
import com.example.user_account_service.model.User;
import com.example.user_account_service.repository.AssetRepository;
import com.example.user_account_service.repository.ContractRepository;
import com.example.user_account_service.repository.OwnershipRepository;
import com.example.user_account_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- Thêm

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class ContractServiceImpl implements ContractService {

    @Autowired private ContractRepository contractRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AssetRepository assetRepository;
    @Autowired private OwnershipRepository ownershipRepository;

    @Override
    @Transactional // <-- Quan trọng: Đảm bảo tất cả cùng thành công hoặc thất bại
    public Contract createContract(ContractCreationDto contractDto, Long currentUserId) throws Exception {

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new Exception("Người dùng không hợp lệ"));

        Asset selectedAsset = assetRepository.findById(contractDto.getAssetId())
                .orElseThrow(() -> new Exception("Tài sản không hợp lệ"));

        // 1. TẠO HỢP ĐỒNG
        Contract newContract = new Contract();
        newContract.setTitle(contractDto.getTitle());
        newContract.setAsset(selectedAsset);
        newContract.setStartDate(contractDto.getStartDate());
        newContract.setEndDate(contractDto.getEndDate());
        newContract.setStatus("pending"); // Trạng thái chờ duyệt/chờ ký
        newContract.setCreatedAt(Instant.now());

        // SỬA Ở ĐÂY: Lưu và nhận về đối tượng Contract đã có ID
        Contract savedContract = contractRepository.save(newContract);

        // 2. TẠO QUYỀN SỞ HỮU (OWNERSHIP)
        // Tạm thời gán 100% cho người tạo, logic chia % sẽ phức tạp hơn
        Ownership newOwnership = new Ownership();
        newOwnership.setUser(currentUser);
        newOwnership.setAsset(selectedAsset);
        newOwnership.setContract(savedContract); // Dùng hợp đồng vừa lưu
        newOwnership.setOwnershipPercentage(new BigDecimal("100.00"));

        ownershipRepository.save(newOwnership);

        // 3. TRẢ VỀ HỢP ĐỒNG ĐÃ TẠO
        return savedContract;
    }

    @Override
    public ContractSummaryDto getContractSummaryForUser(Long userId) {
        // (Logic của bạn để đếm hợp đồng dùng ContractRepository)
        // ...
        return new ContractSummaryDto(0, 0, 0, 0); // (Thay bằng logic thật)
    }

    @Override
    public Page<ContractListViewDto> getContractsForUser(Long userId, Pageable pageable) {
        // (Logic của bạn để lấy danh sách Pageable dùng ContractRepository)
        // ...
        return Page.empty(); // (Thay bằng logic thật)
    }
}