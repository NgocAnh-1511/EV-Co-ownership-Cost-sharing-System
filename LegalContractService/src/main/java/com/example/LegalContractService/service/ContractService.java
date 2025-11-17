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
import jakarta.persistence.EntityNotFoundException;

/**
 * Service class for managing legal contracts
 */
@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractHistoryRepository contractHistoryRepository;

    @Autowired
    public ContractService(ContractRepository contractRepository, ContractHistoryRepository contractHistoryRepository) {
        this.contractRepository = contractRepository;
        this.contractHistoryRepository = contractHistoryRepository;
    }

    /**
     * Get all contracts
     */
    public List<Legalcontract> getAllContracts() {
        System.out.println("🔵 [ContractService] Getting all contracts...");
        try {
            long count = contractRepository.count();
            System.out.println("🔵 [ContractService] Total contracts in DB: " + count);
            List<Legalcontract> contracts = contractRepository.findAll();
            System.out.println("✅ [ContractService] Found " + contracts.size() + " contracts");
            if (contracts.size() > 0) {
                contracts.forEach(c -> System.out.println("   - Contract ID: " + c.getContractId() + ", Code: " + c.getContractCode()));
            } else if (count > 0) {
                System.err.println("⚠️ [ContractService] WARNING: Repository count = " + count + " but findAll() returned empty list!");
                System.err.println("   This might be a table name mapping issue.");
            }
            return contracts;
        } catch (Exception e) {
            System.err.println("❌ [ContractService] Error getting contracts: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Get contract by ID
     */
    public Optional<Legalcontract> getContractById(Integer contractId) {
        return contractRepository.findById(contractId);
    }

    /**
     * Get contracts by group ID
     */
    public List<Legalcontract> getContractsByGroupId(Integer groupId) {
        return contractRepository.findByGroupId(groupId);
    }

    /**
     * Get contracts by status
     */
    public List<Legalcontract> getContractsByStatus(String status) {
        return contractRepository.findByContractStatus(status);
    }

    /**
     * Get contract history
     */
    public List<Contracthistory> getContractHistory(Integer contractId) {
        return contractHistoryRepository.findByContractIdOrderByDateDesc(contractId);
    }

    /**
     * Create a new contract
     */
    @Transactional
    public Legalcontract createContract(Legalcontract contract) {
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

    /**
     * Update an existing contract
     */
    @Transactional
    public Legalcontract updateContract(Integer contractId, Legalcontract contractData) {
        Optional<Legalcontract> contractOpt = contractRepository.findById(contractId);
        if (contractOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy hợp đồng với ID: " + contractId);
        }

        Legalcontract contract = contractOpt.get();

        // Cập nhật các trường
        if (contractData.getContractCode() != null) {
            contract.setContractCode(contractData.getContractCode());
        }
        if (contractData.getContractStatus() != null) {
            contract.setContractStatus(contractData.getContractStatus());
        }
        if (contractData.getGroupId() != null) {
            contract.setGroupId(contractData.getGroupId());
        }

        Legalcontract updatedContract = contractRepository.save(contract);

        // Lưu lịch sử
        saveContractHistory(contractId, "Cập nhật hợp đồng");

        return updatedContract;
    }

    /**
     * Sign a contract
     */
    @Transactional
    public Legalcontract signContract(Integer contractId) {
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

        Legalcontract signedContract = contractRepository.save(contract);

        // Lưu lịch sử
        saveContractHistory(contractId, "Ký hợp đồng");

        return signedContract;
    }

    /**
     * Archive a contract
     */
    @Transactional
    public Legalcontract archiveContract(Integer contractId) {
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

    /**
     * Delete a contract
     */
    @Transactional
    public void deleteContract(Integer contractId) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔵 [ContractService] ===== BẮT ĐẦU XÓA HỢP ĐỒNG =====");
        System.out.println("   Contract ID: " + contractId);
        System.out.println("   Timestamp: " + java.time.LocalDateTime.now());
        System.out.println("   Thread: " + Thread.currentThread().getName());
        System.out.println("=".repeat(80) + "\n");
        
        try {
            System.out.println("🔵 [ContractService] Bước 1: Tìm hợp đồng trong database...");
            Optional<Legalcontract> contractOpt = contractRepository.findById(contractId);
            
            if (contractOpt.isEmpty()) {
                System.err.println("❌ [ContractService] Không tìm thấy hợp đồng với ID: " + contractId);
                System.err.println("   Số lượng hợp đồng trong DB: " + contractRepository.count());
            throw new IllegalArgumentException("Không tìm thấy hợp đồng với ID: " + contractId);
        }

            Legalcontract contract = contractOpt.get();
            System.out.println("✅ [ContractService] Tìm thấy hợp đồng:");
            System.out.println("   - Contract ID: " + contract.getContractId());
            System.out.println("   - Contract Code: " + contract.getContractCode());
            System.out.println("   - Status: " + contract.getContractStatus());
            System.out.println("   - Group ID: " + contract.getGroupId());
            System.out.println("   - Creation Date: " + contract.getCreationDate());

            // Lưu lịch sử trước khi xóa (nếu có thể)
            try {
                System.out.println("\n🔵 [ContractService] Bước 2: Lưu lịch sử xóa hợp đồng...");
        saveContractHistory(contractId, "Xóa hợp đồng");
                System.out.println("✅ [ContractService] Đã lưu lịch sử thành công");
            } catch (Exception e) {
                // Nếu lưu lịch sử thất bại, vẫn tiếp tục xóa hợp đồng
                System.err.println("⚠️ [ContractService] Không thể lưu lịch sử, nhưng vẫn tiếp tục xóa hợp đồng");
                System.err.println("   Error: " + e.getMessage());
                e.printStackTrace();
            }

            // Xóa hợp đồng
            System.out.println("\n🔵 [ContractService] Bước 3: Xóa hợp đồng khỏi database...");
            System.out.println("   Trước khi xóa - Số lượng hợp đồng: " + contractRepository.count());
        contractRepository.deleteById(contractId);
            System.out.println("✅ [ContractService] Đã gọi deleteById()");
            
            // Xác nhận đã xóa
            System.out.println("\n🔵 [ContractService] Bước 4: Xác nhận hợp đồng đã được xóa...");
            boolean stillExists = contractRepository.existsById(contractId);
            System.out.println("   Sau khi xóa - Hợp đồng vẫn tồn tại: " + stillExists);
            System.out.println("   Sau khi xóa - Số lượng hợp đồng: " + contractRepository.count());
            
            if (stillExists) {
                System.err.println("❌ [ContractService] CẢNH BÁO: Hợp đồng vẫn còn tồn tại sau khi xóa!");
                System.err.println("   Có thể do transaction chưa commit");
            } else {
                System.out.println("✅ [ContractService] Xác nhận: Hợp đồng đã được xóa thành công");
            }
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("✅ [ContractService] ===== XÓA HỢP ĐỒNG THÀNH CÔNG =====");
            System.out.println("   Contract ID: " + contractId);
            System.out.println("=".repeat(80) + "\n");
        } catch (IllegalArgumentException e) {
            System.err.println("\n" + "=".repeat(80));
            System.err.println("❌ [ContractService] ===== LỖI: HỢP ĐỒNG KHÔNG TỒN TẠI =====");
            System.err.println("   Error: " + e.getMessage());
            System.err.println("=".repeat(80) + "\n");
            throw e;
        } catch (Exception e) {
            System.err.println("\n" + "=".repeat(80));
            System.err.println("❌ [ContractService] ===== EXCEPTION KHI XÓA HỢP ĐỒNG =====");
            System.err.println("   Contract ID: " + contractId);
            System.err.println("   Error Type: " + e.getClass().getName());
            System.err.println("   Error Message: " + e.getMessage());
            System.err.println("   Stack Trace:");
            e.printStackTrace();
            System.err.println("=".repeat(80) + "\n");
            throw new RuntimeException("Không thể xóa hợp đồng: " + e.getMessage(), e);
        }
    }

    /**
     * Save contract history
     */
    private void saveContractHistory(Integer contractId, String action) {
        try {
            // Sử dụng findById thay vì getReferenceById để tránh lazy loading issues
            Optional<Legalcontract> contractOpt = contractRepository.findById(contractId);
            if (contractOpt.isEmpty()) {
                System.err.println("⚠️ [saveContractHistory] Không tìm thấy hợp đồng với ID: " + contractId);
                return;
            }
            
            Contracthistory history = new Contracthistory();
            history.setContract(contractOpt.get());
            history.setAction(action);
            history.setActionDate(Instant.now());
            contractHistoryRepository.save(history);
            System.out.println("✅ [saveContractHistory] Đã lưu lịch sử: " + action + " cho hợp đồng ID: " + contractId);
        } catch (EntityNotFoundException e) {
            System.err.println("⚠️ [saveContractHistory] Không thể lưu lịch sử vì không tìm thấy hợp đồng với ID: " + contractId);
        } catch (Exception e) {
            System.err.println("❌ [saveContractHistory] Lỗi khi lưu lịch sử hợp đồng: " + e.getMessage());
            e.printStackTrace();
            // Throw exception để caller có thể xử lý
            throw new RuntimeException("Không thể lưu lịch sử hợp đồng: " + e.getMessage(), e);
        }
    }
}

