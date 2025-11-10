package com.example.LegalContractService.controller;

import com.example.LegalContractService.model.Legalcontract;
import com.example.LegalContractService.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/legalcontracts")
@CrossOrigin(origins = "*")
public class ContractAPI {

    @Autowired
    private ContractService contractService;

    /**
     * Lấy tất cả các hợp đồng
     */
    @GetMapping("/all")
    public ResponseEntity<List<Legalcontract>> getAllContracts() {
        try {
            List<Legalcontract> contracts = contractService.getAllContracts();
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách hợp đồng: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy hợp đồng theo ID
     */
    @GetMapping("/{contractId}")
    public ResponseEntity<?> getContractById(@PathVariable String contractId) {
        try {
            return contractService.getContractById(contractId)
                    .map(contract -> ResponseEntity.ok(contract))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Không tìm thấy hợp đồng với ID: " + contractId));
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy hợp đồng: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi lấy hợp đồng: " + e.getMessage());
        }
    }

    /**
     * Lấy hợp đồng theo groupId
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Legalcontract>> getContractsByGroupId(@PathVariable String groupId) {
        try {
            List<Legalcontract> contracts = contractService.getContractsByGroupId(groupId);
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách hợp đồng theo group: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Tạo hợp đồng mới
     */
    @PostMapping("/create")
    public ResponseEntity<?> createContract(@RequestBody Map<String, Object> requestData) {
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
                contract.setDescription((String) requestData.get("description"));
            }
            if (requestData.containsKey("parties")) {
                Object parties = requestData.get("parties");
                if (parties instanceof String) {
                    contract.setParties((String) parties);
                } else {
                    // Convert to JSON string if it's a list/object
                    contract.setParties(parties.toString());
                }
            }
            if (requestData.containsKey("groupId")) {
                contract.setGroupId((String) requestData.get("groupId"));
            }

            Legalcontract createdContract = contractService.createContract(contract);
            System.out.println("✅ [CREATE CONTRACT] Đã tạo hợp đồng: " + createdContract.getContractId());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdContract);
        } catch (Exception e) {
            System.err.println("❌ [CREATE CONTRACT] Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi tạo hợp đồng: " + e.getMessage());
        }
    }

    /**
     * Cập nhật hợp đồng
     */
    @PutMapping("/update/{contractId}")
    public ResponseEntity<?> updateContract(
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
                contractData.setDescription((String) requestData.get("description"));
            }
            if (requestData.containsKey("parties")) {
                Object parties = requestData.get("parties");
                if (parties instanceof String) {
                    contractData.setParties((String) parties);
                } else {
                    contractData.setParties(parties.toString());
                }
            }
            if (requestData.containsKey("groupId")) {
                contractData.setGroupId((String) requestData.get("groupId"));
            }

            Legalcontract updatedContract = contractService.updateContract(contractId, contractData);
            System.out.println("✅ [UPDATE CONTRACT] Đã cập nhật hợp đồng: " + contractId);

            return ResponseEntity.ok(updatedContract);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ [UPDATE CONTRACT] Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi cập nhật hợp đồng: " + e.getMessage());
        }
    }

    /**
     * Ký hợp đồng
     */
    @PutMapping("/sign/{contractId}")
    public ResponseEntity<?> signContract(
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

            return ResponseEntity.ok(signedContract);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ [SIGN CONTRACT] Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi ký hợp đồng: " + e.getMessage());
        }
    }

    /**
     * Lưu trữ hợp đồng
     */
    @PutMapping("/archive/{contractId}")
    public ResponseEntity<?> archiveContract(@PathVariable String contractId) {
        try {
            System.out.println("🔵 [ARCHIVE CONTRACT] Contract ID: " + contractId);

            Legalcontract archivedContract = contractService.archiveContract(contractId);
            System.out.println("✅ [ARCHIVE CONTRACT] Đã lưu trữ hợp đồng: " + contractId);

            return ResponseEntity.ok(archivedContract);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ [ARCHIVE CONTRACT] Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi lưu trữ hợp đồng: " + e.getMessage());
        }
    }

    /**
     * Xóa hợp đồng
     */
    @DeleteMapping("/{contractId}")
    public ResponseEntity<?> deleteContract(@PathVariable String contractId) {
        try {
            System.out.println("🔵 [DELETE CONTRACT] Contract ID: " + contractId);

            contractService.deleteContract(contractId);
            System.out.println("✅ [DELETE CONTRACT] Đã xóa hợp đồng: " + contractId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xóa hợp đồng thành công");
            response.put("contractId", contractId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ [DELETE CONTRACT] Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi xóa hợp đồng: " + e.getMessage());
        }
    }
}
