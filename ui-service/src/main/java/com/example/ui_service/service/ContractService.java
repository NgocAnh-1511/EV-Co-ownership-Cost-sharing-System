package com.example.ui_service.service;

import com.example.ui_service.dto.ContractCreationDto; // <-- Import DTO mới
import com.example.ui_service.dto.ContractListViewDto;
import com.example.ui_service.dto.ContractSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContractService {
    ContractSummaryDto getContractSummaryForUser(Long userId);
    Page<ContractListViewDto> getContractsForUser(Long userId, Pageable pageable);

    // --- PHƯƠNG THỨC MỚI ---
    void createContract(ContractCreationDto dto, Long userId) throws Exception;
    // --- KẾT THÚC ---
}