package com.example.user_account_service.service;

import com.example.user_account_service.dto.ContractCreationDto;
import com.example.user_account_service.dto.ContractListViewDto;
import com.example.user_account_service.dto.ContractSummaryDto;
import com.example.user_account_service.model.Contract; // <-- Import Model
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContractService {

    // SỬA Ở ĐÂY: Thay "void" bằng "Contract"
    Contract createContract(ContractCreationDto contractDto, Long currentUserId) throws Exception;

    // Các hàm khác giữ nguyên
    ContractSummaryDto getContractSummaryForUser(Long userId);
    Page<ContractListViewDto> getContractsForUser(Long userId, Pageable pageable);
}