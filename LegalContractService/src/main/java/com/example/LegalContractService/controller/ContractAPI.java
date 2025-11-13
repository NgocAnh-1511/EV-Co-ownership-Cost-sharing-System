package com.example.LegalContractService.controller;

import com.example.LegalContractService.dto.ApiResponse;
import com.example.LegalContractService.model.Legalcontract;
import com.example.LegalContractService.model.Contracthistory;
import com.example.LegalContractService.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Legal Contract Management
 */
@RestController
@RequestMapping("/api/legalcontracts")
@CrossOrigin(origins = "*")
public class ContractAPI {

    @Autowired
    private ContractService contractService;

    /**
     * Get all contracts
     * GET /api/legalcontracts/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Legalcontract>>> getAllContracts() {
        try {
            List<Legalcontract> contracts = contractService.getAllContracts();
            return ResponseEntity.ok(ApiResponse.contractsSuccess(contracts));
        } catch (Exception e) {
            System.err.println("❌ [GET ALL CONTRACTS] Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi khi lấy danh sách hợp đồng", e.getMessage()));
        }
    }

    /**
     * Get contract by ID
     * GET /api/legalcontracts/{contractId}
     */
    @GetMapping("/{contractId}")
    public ResponseEntity<ApiResponse<Legalcontract>> getContractById(@PathVariable String contractId) {
        try {
            return contractService.getContractById(contractId)
                    .map(contract -> ResponseEntity.ok(ApiResponse.contractSuccess(contract)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Không tìm thấy hợp đồng với ID: " + contractId)));
        } catch (Exception e) {
            System.err.println("❌ [GET CONTRACT BY ID] Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi khi lấy hợp đồng", e.getMessage()));
        }
    }

    /**
     * Get contracts by group ID
     * GET /api/legalcontracts/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<List<Legalcontract>>> getContractsByGroupId(@PathVariable String groupId) {
        try {
            List<Legalcontract> contracts = contractService.getContractsByGroupId(groupId);
            return ResponseEntity.ok(ApiResponse.contractsSuccess(contracts));
        } catch (Exception e) {
            System.err.println("❌ [GET CONTRACTS BY GROUP] Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi khi lấy danh sách hợp đồng theo group", e.getMessage()));
        }
    }

    /**
     * Get contracts by status
     * GET /api/legalcontracts/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Legalcontract>>> getContractsByStatus(@PathVariable String status) {
        try {
            List<Legalcontract> contracts = contractService.getContractsByStatus(status);
            return ResponseEntity.ok(ApiResponse.contractsSuccess(contracts));
        } catch (Exception e) {
            System.err.println("❌ [GET CONTRACTS BY STATUS] Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi khi lấy danh sách hợp đồng theo trạng thái", e.getMessage()));
        }
    }

    /**
     * Get contract history
     * GET /api/legalcontracts/{contractId}/history
     */
    @GetMapping("/{contractId}/history")
    public ResponseEntity<ApiResponse<List<Contracthistory>>> getContractHistory(@PathVariable String contractId) {
        try {
            List<Contracthistory> history = contractService.getContractHistory(contractId);
            return ResponseEntity.ok(ApiResponse.success(history));
        } catch (Exception e) {
            System.err.println("❌ [GET CONTRACT HISTORY] Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi khi lấy lịch sử hợp đồng", e.getMessage()));
        }
    }

    /**
     * Create a new contract
     * POST /api/legalcontracts/create
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Legalcontract>> createContract(@RequestBody Map<String, Object> requestData) {
        try {
            System.out.println("🔵 [CREATE CONTRACT] Request data: " + requestData);

            Legalcontract contract = new Legalcontract();

            // Map các trường từ request
            if (requestData.containsKey("contractId")) {
                contract.setContractId((String) requestData.get("contractId"));
            }
            if (requestData.containsKey("contractCode")) {
                contract.setContractCode((String) requestData.get("contractCode"));
            }
            if (requestData.containsKey("contractType")) {
                contract.setContractType((String) requestData.get("contractType"));
            }
            if (requestData.containsKey("contractStatus")) {
                contract.setContractStatus((String) requestData.get("contractStatus"));
            }
            if (requestData.containsKey("description")) {
                Object desc = requestData.get("description");
                contract.setDescription(desc != null ? desc.toString() : null);
            }
            if (requestData.containsKey("parties")) {
                Object parties = requestData.get("parties");
                if (parties instanceof String) {
                    contract.setParties((String) parties);
                } else if (parties != null) {
                    contract.setParties(parties.toString());
                }
            }
            if (requestData.containsKey("groupId")) {
                Object groupId = requestData.get("groupId");
                contract.setGroupId(groupId != null ? groupId.toString() : null);
            }

            Legalcontract createdContract = contractService.createContract(contract);
            System.out.println("✅ [CREATE CONTRACT] Đã tạo hợp đồng: " + createdContract.getContractId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.contractSuccess(createdContract));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [CREATE CONTRACT] Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi khi tạo hợp đồng", e.getMessage()));
        }
    }

    /**
     * Update an existing contract
     * PUT /api/legalcontracts/update/{contractId}
     */
    @PutMapping("/update/{contractId}")
    public ResponseEntity<ApiResponse<Legalcontract>> updateContract(
            @PathVariable String contractId,
            @RequestBody Map<String, Object> requestData) {
        try {
            System.out.println("🔵 [UPDATE CONTRACT] Contract ID: " + contractId);
            System.out.println("   Request data: " + requestData);

            Legalcontract contractData = new Legalcontract();

            // Map các trường từ request
            if (requestData.containsKey("contractCode")) {
                contractData.setContractCode((String) requestData.get("contractCode"));
            }
            if (requestData.containsKey("contractType")) {
                contractData.setContractType((String) requestData.get("contractType"));
            }
            if (requestData.containsKey("contractStatus")) {
                contractData.setContractStatus((String) requestData.get("contractStatus"));
            }
            if (requestData.containsKey("description")) {
                Object desc = requestData.get("description");
                contractData.setDescription(desc != null ? desc.toString() : null);
            }
            if (requestData.containsKey("parties")) {
                Object parties = requestData.get("parties");
                if (parties instanceof String) {
                    contractData.setParties((String) parties);
                } else if (parties != null) {
                    contractData.setParties(parties.toString());
                }
            }
            if (requestData.containsKey("groupId")) {
                Object groupId = requestData.get("groupId");
                contractData.setGroupId(groupId != null ? groupId.toString() : null);
            }

            Legalcontract updatedContract = contractService.updateContract(contractId, contractData);
            System.out.println("✅ [UPDATE CONTRACT] Đã cập nhật hợp đồng: " + contractId);

            return ResponseEntity.ok(ApiResponse.contractSuccess(updatedContract));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [UPDATE CONTRACT] Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi khi cập nhật hợp đồng", e.getMessage()));
        }
    }

    /**
     * Sign a contract
     * PUT /api/legalcontracts/sign/{contractId}
     */
    @PutMapping("/sign/{contractId}")
    public ResponseEntity<ApiResponse<Legalcontract>> signContract(
            @PathVariable String contractId,
            @RequestBody(required = false) Map<String, Object> requestData) {
        try {
            System.out.println("🔵 [SIGN CONTRACT] Contract ID: " + contractId);

            String signerId = null;
            String signatureData = null;

            if (requestData != null) {
                if (requestData.containsKey("signerId")) {
                    signerId = (String) requestData.get("signerId");
                }
                if (requestData.containsKey("signatureData")) {
                    signatureData = (String) requestData.get("signatureData");
                }
            }

            Legalcontract signedContract = contractService.signContract(contractId, signerId, signatureData);
            System.out.println("✅ [SIGN CONTRACT] Đã ký hợp đồng: " + contractId);

            return ResponseEntity.ok(ApiResponse.contractSuccess(signedContract));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [SIGN CONTRACT] Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi khi ký hợp đồng", e.getMessage()));
        }
    }

    /**
     * Archive a contract
     * PUT /api/legalcontracts/archive/{contractId}
     */
    @PutMapping("/archive/{contractId}")
    public ResponseEntity<ApiResponse<Legalcontract>> archiveContract(@PathVariable String contractId) {
        try {
            System.out.println("🔵 [ARCHIVE CONTRACT] Contract ID: " + contractId);

            Legalcontract archivedContract = contractService.archiveContract(contractId);
            System.out.println("✅ [ARCHIVE CONTRACT] Đã lưu trữ hợp đồng: " + contractId);

            return ResponseEntity.ok(ApiResponse.contractSuccess(archivedContract));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [ARCHIVE CONTRACT] Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi khi lưu trữ hợp đồng", e.getMessage()));
        }
    }

    /**
     * Delete a contract
     * DELETE /api/legalcontracts/{contractId}
     */
    @DeleteMapping("/{contractId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteContract(@PathVariable String contractId) {
        try {
            System.out.println("🔵 [DELETE CONTRACT] Contract ID: " + contractId);

            contractService.deleteContract(contractId);
            System.out.println("✅ [DELETE CONTRACT] Đã xóa hợp đồng: " + contractId);

            Map<String, Object> response = new HashMap<>();
            response.put("contractId", contractId);
            response.put("deleted", true);

            return ResponseEntity.ok(ApiResponse.success("Đã xóa hợp đồng thành công", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [DELETE CONTRACT] Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi khi xóa hợp đồng", e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     * GET /api/legalcontracts/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "LegalContractService");
        return ResponseEntity.ok(response);
    }
}
