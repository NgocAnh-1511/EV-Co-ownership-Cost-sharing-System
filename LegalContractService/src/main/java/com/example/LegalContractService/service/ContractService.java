package com.example.LegalContractService.service;

import com.example.LegalContractService.model.Legalcontract;
import com.example.LegalContractService.model.Contracthistory;
import com.example.LegalContractService.repository.ContractRepository;
import com.example.LegalContractService.repository.ContractHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractHistoryRepository contractHistoryRepository;

    @Autowired
    public ContractService(ContractRepository contractRepository, ContractHistoryRepository contractHistoryRepository) {
        this.contractRepository = contractRepository;
        this.contractHistoryRepository = contractHistoryRepository;
    }

    public List<Legalcontract> getAllContracts() {
        return contractRepository.findAll();
    }

    public Optional<Legalcontract> getContractById(String contractId) {
        return contractRepository.findById(contractId);
    }

    public List<Legalcontract> getContractsByGroupId(String groupId) {
        return contractRepository.findByGroupId(groupId);
    }

    @Transactional
    public Legalcontract createContract(Legalcontract contract) {
        // Tự động tạo contract_id nếu chưa có
        if (contract.getContractId() == null || contract.getContractId().trim().isEmpty()) {
            contract.setContractId("CONTRACT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // Set creation date nếu chưa có
        if (contract.getCreationDate() == null) {
            contract.setCreationDate(Instant.now());
        }

        // Set status mặc định nếu chưa có
        if (contract.getContractStatus() == null || contract.getContractStatus().trim().isEmpty()) {
            contract.setContractStatus("draft");
        }

        Legalcontract savedContract = contractRepository.save(contract);

        // Lưu lịch sử
        saveContractHistory(savedContract.getContractId(), "Tạo hợp đồng mới");

        return savedContract;
    }

    @Transactional
    public Legalcontract updateContract(String contractId, Legalcontract contractData) {
        Optional<Legalcontract> contractOpt = contractRepository.findById(contractId);
        if (contractOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy hợp đồng với ID: " + contractId);
        }

        Legalcontract contract = contractOpt.get();

        // Cập nhật các trường
        if (contractData.getContractCode() != null) {
            contract.setContractCode(contractData.getContractCode());
        }
        if (contractData.getContractType() != null) {
            contract.setContractType(contractData.getContractType());
        }
        if (contractData.getContractStatus() != null) {
            contract.setContractStatus(contractData.getContractStatus());
        }
        if (contractData.getDescription() != null) {
            contract.setDescription(contractData.getDescription());
        }
        if (contractData.getParties() != null) {
            contract.setParties(contractData.getParties());
        }
        if (contractData.getGroupId() != null) {
            contract.setGroupId(contractData.getGroupId());
        }

        Legalcontract updatedContract = contractRepository.save(contract);

        // Lưu lịch sử
        saveContractHistory(contractId, "Cập nhật hợp đồng");

        return updatedContract;
    }

    @Transactional
    public Legalcontract signContract(String contractId, String signerId, String signatureData) {
        Optional<Legalcontract> contractOpt = contractRepository.findById(contractId);
        if (contractOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy hợp đồng với ID: " + contractId);
        }

        Legalcontract contract = contractOpt.get();

        // Kiểm tra trạng thái hợp đồng
        if (!"pending".equalsIgnoreCase(contract.getContractStatus()) && 
            !"draft".equalsIgnoreCase(contract.getContractStatus())) {
            throw new IllegalStateException("Hợp đồng không thể ký. Trạng thái hiện tại: " + contract.getContractStatus());
        }

        // Cập nhật trạng thái và thông tin ký
        contract.setContractStatus("signed");
        contract.setSignedDate(Instant.now());
        if (signerId != null) {
            contract.setSignerId(signerId);
        }
        if (signatureData != null) {
            contract.setSignatureData(signatureData);
        }

        Legalcontract signedContract = contractRepository.save(contract);

        // Lưu lịch sử
        saveContractHistory(contractId, "Ký hợp đồng");

        return signedContract;
    }

    @Transactional
    public Legalcontract archiveContract(String contractId) {
        Optional<Legalcontract> contractOpt = contractRepository.findById(contractId);
        if (contractOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy hợp đồng với ID: " + contractId);
        }

        Legalcontract contract = contractOpt.get();
        contract.setContractStatus("archived");

        Legalcontract archivedContract = contractRepository.save(contract);

        // Lưu lịch sử
        saveContractHistory(contractId, "Lưu trữ hợp đồng");

        return archivedContract;
    }

    @Transactional
    public void deleteContract(String contractId) {
        if (!contractRepository.existsById(contractId)) {
            throw new IllegalArgumentException("Không tìm thấy hợp đồng với ID: " + contractId);
        }

        // Lưu lịch sử trước khi xóa
        saveContractHistory(contractId, "Xóa hợp đồng");

        contractRepository.deleteById(contractId);
    }

    /**
     * Lưu lịch sử hợp đồng
     */
    private void saveContractHistory(String contractId, String action) {
        try {
            Contracthistory history = new Contracthistory();
            history.setHistoryId("HIST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            
            Legalcontract contract = new Legalcontract();
            contract.setContractId(contractId);
            history.setContract(contract);
            
            history.setAction(action);
            history.setActionDate(Instant.now());
            
            contractHistoryRepository.save(history);
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu lịch sử hợp đồng: " + e.getMessage());
            // Không throw exception để không ảnh hưởng đến luồng chính
        }
    }
}
