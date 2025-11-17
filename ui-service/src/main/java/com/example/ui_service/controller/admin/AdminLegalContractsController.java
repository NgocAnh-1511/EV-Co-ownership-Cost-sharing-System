package com.example.ui_service.controller.admin;

import com.example.ui_service.external.model.LegalContractDTO;
import com.example.ui_service.external.service.LegalContractRestClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/legal-contracts")
public class AdminLegalContractsController {

    private final LegalContractRestClient legalContractRestClient;

    public AdminLegalContractsController(LegalContractRestClient legalContractRestClient) {
        this.legalContractRestClient = legalContractRestClient;
    }

    @GetMapping
    public String legalContractsPage(
            Model model,
            @RequestParam(value = "searchQuery", required = false, defaultValue = "") String searchQuery,
            @RequestParam(value = "statusFilter", required = false, defaultValue = "all") String statusFilter,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        
        model.addAttribute("pageTitle", "Quản lý hợp đồng pháp lý điện tử");
        model.addAttribute("pageSubtitle", "Quản lý và theo dõi các hợp đồng pháp lý điện tử trong hệ thống");
        model.addAttribute("activePage", "legal-contracts");

        System.out.println("🔵 [AdminLegalContractsController] Loading contracts page...");
        List<LegalContractDTO> allContracts = legalContractRestClient.getAllContracts();
        System.out.println("✅ [AdminLegalContractsController] Loaded " + allContracts.size() + " contracts");

        List<LegalContractDTO> filteredContracts = allContracts.stream()
                .filter(contract -> {
                    boolean matchesSearch = searchQuery.isEmpty()
                            || (contract.getContractCode() != null && contract.getContractCode().toLowerCase().contains(searchQuery.toLowerCase()))
                            || (contract.getContractId() != null && contract.getContractId().toString().contains(searchQuery));

                    boolean matchesStatus = "all".equals(statusFilter)
                            || (contract.getContractStatus() != null && contract.getContractStatus().equalsIgnoreCase(statusFilter));

                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toList());

        long totalContracts = allContracts.size();
        long pendingContracts = allContracts.stream().filter(c -> "pending".equalsIgnoreCase(c.getContractStatus())).count();
        long signedContracts = allContracts.stream().filter(c -> "signed".equalsIgnoreCase(c.getContractStatus())).count();
        long archivedContracts = allContracts.stream().filter(c -> "archived".equalsIgnoreCase(c.getContractStatus())).count();

        int totalPages = filteredContracts.isEmpty() ? 1 : (int) Math.ceil((double) filteredContracts.size() / size);
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, filteredContracts.size());
        List<LegalContractDTO> pagedContracts = filteredContracts.isEmpty()
                ? filteredContracts : filteredContracts.subList(startIndex, endIndex);

        model.addAttribute("contracts", pagedContracts);
        model.addAttribute("totalContracts", totalContracts);
        model.addAttribute("pendingContracts", pendingContracts);
        model.addAttribute("signedContracts", signedContracts);
        model.addAttribute("archivedContracts", archivedContracts);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalFiltered", filteredContracts.size());
        model.addAttribute("startIndex", filteredContracts.isEmpty() ? 0 : startIndex + 1);
        model.addAttribute("endIndex", endIndex);

        return "admin-legal-contracts";
    }

    @GetMapping("/api/{contractId}")
    @ResponseBody
    public Map<String, Object> getContractDetails(@PathVariable Integer contractId) {
        Map<String, Object> response = new HashMap<>();
        LegalContractDTO contract = legalContractRestClient.getContractById(contractId);
        if (contract == null) {
            response.put("error", "Không tìm thấy hợp đồng với ID: " + contractId);
            return response;
        }
        List<Map<String, Object>> history = legalContractRestClient.getContractHistory(contractId);
        response.put("contract", contract);
        response.put("history", history);
        return response;
    }

    @PostMapping("/api/create")
    @ResponseBody
    public Map<String, Object> createContract(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        LegalContractDTO created = legalContractRestClient.createContract(requestData);
        if (created != null) {
            response.put("success", true);
            response.put("message", "Tạo hợp đồng thành công!");
            response.put("data", created);
        } else {
            response.put("success", false);
            response.put("message", "Không thể tạo hợp đồng. Vui lòng thử lại.");
        }
        return response;
    }

    @PutMapping("/api/update/{contractId}")
    @ResponseBody
    public Map<String, Object> updateContract(@PathVariable Integer contractId,
                                             @RequestBody Map<String, Object> contractData) {
        Map<String, Object> response = new HashMap<>();
        LegalContractDTO updated = legalContractRestClient.updateContract(contractId, contractData);
        if (updated != null) {
            response.put("success", true);
            response.put("message", "Cập nhật hợp đồng thành công!");
            response.put("data", updated);
        } else {
            response.put("success", false);
            response.put("message", "Không thể cập nhật hợp đồng. Vui lòng thử lại.");
        }
        return response;
    }

    @PutMapping("/api/sign/{contractId}")
    @ResponseBody
    public Map<String, Object> signContract(@PathVariable Integer contractId) {
        Map<String, Object> response = new HashMap<>();
        LegalContractDTO signed = legalContractRestClient.signContract(contractId);
        if (signed != null) {
            response.put("success", true);
            response.put("message", "Ký hợp đồng thành công!");
            response.put("data", signed);
        } else {
            response.put("success", false);
            response.put("message", "Không thể ký hợp đồng. Vui lòng thử lại.");
        }
        return response;
    }

    @PutMapping("/api/archive/{contractId}")
    @ResponseBody
    public Map<String, Object> archiveContract(@PathVariable Integer contractId) {
        Map<String, Object> response = new HashMap<>();
        LegalContractDTO archived = legalContractRestClient.archiveContract(contractId);
        if (archived != null) {
            response.put("success", true);
            response.put("message", "Lưu trữ hợp đồng thành công!");
            response.put("data", archived);
        } else {
            response.put("success", false);
            response.put("message", "Không thể lưu trữ hợp đồng. Vui lòng thử lại.");
        }
        return response;
    }

    @DeleteMapping("/api/delete/{contractId}")
    @ResponseBody
    public Map<String, Object> deleteContract(@PathVariable Integer contractId) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔵 [AdminLegalContractsController] ===== DELETE REQUEST NHẬN ĐƯỢC =====");
        System.out.println("   Contract ID: " + contractId);
        System.out.println("   Timestamp: " + java.time.LocalDateTime.now());
        System.out.println("   Thread: " + Thread.currentThread().getName());
        System.out.println("=".repeat(80) + "\n");
        
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("🔵 [AdminLegalContractsController] Đang gọi legalContractRestClient.deleteContract(" + contractId + ")");
        boolean deleted = legalContractRestClient.deleteContract(contractId);
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("🔵 [AdminLegalContractsController] ===== KẾT QUẢ TỪ REST CLIENT =====");
            System.out.println("   Contract ID: " + contractId);
            System.out.println("   Deleted: " + deleted);
            System.out.println("   Deleted Type: " + (deleted ? "boolean true" : "boolean false"));
            System.out.println("=".repeat(80) + "\n");
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Xóa hợp đồng thành công!");
                System.out.println("✅ [AdminLegalContractsController] Trả về response thành công");
                System.out.println("   Response: " + response);
            } else {
                response.put("success", false);
                response.put("message", "Không thể xóa hợp đồng. Vui lòng kiểm tra lại hợp đồng có tồn tại không hoặc liên hệ quản trị viên.");
                System.err.println("❌ [AdminLegalContractsController] Trả về response thất bại");
                System.err.println("   Response: " + response);
            }
        } catch (Exception e) {
            System.err.println("\n" + "=".repeat(80));
            System.err.println("❌ [AdminLegalContractsController] ===== EXCEPTION KHI XÓA HỢP ĐỒNG =====");
            System.err.println("   Contract ID: " + contractId);
            System.err.println("   Error Type: " + e.getClass().getName());
            System.err.println("   Error Message: " + e.getMessage());
            System.err.println("   Stack Trace:");
            e.printStackTrace();
            System.err.println("=".repeat(80) + "\n");
            
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi khi xóa hợp đồng: " + e.getMessage());
            response.put("error", e.getClass().getName() + ": " + e.getMessage());
        }
        
        System.out.println("🔵 [AdminLegalContractsController] Trả về response cho client");
        System.out.println("   Final Response: " + response);
        System.out.println("=".repeat(80) + "\n");
        
        return response;
    }
}

