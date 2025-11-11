package com.example.user_account_service.controller;

import com.example.user_account_service.dto.ContractDetailDTO;
import com.example.user_account_service.dto.VehicleDTO;
import com.example.user_account_service.entity.Contract;
import com.example.user_account_service.entity.User;
import com.example.user_account_service.service.ContractService;
import com.example.user_account_service.service.UserService;
import com.example.user_account_service.service.VehicleDataClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException; // <-- THÊM IMPORT
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@CrossOrigin(origins = "http://localhost:8080")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private UserService userService;

    @Autowired
    private VehicleDataClient vehicleDataClient;

    @GetMapping("/my-contracts")
    public ResponseEntity<List<ContractDetailDTO>> getMyContracts(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        List<ContractDetailDTO> contracts = contractService.getHydratedContractsByUserId(user.getUserId());
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/available-vehicles")
    public ResponseEntity<List<VehicleDTO>> getAvailableVehicles() {
        List<VehicleDTO> vehicles = vehicleDataClient.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping
    public ResponseEntity<?> createContract(
            @RequestParam String vehicleId,
            @RequestParam(defaultValue = "12") int durationMonths,
            Authentication authentication) {

        try {
            User user = getUserFromAuth(authentication);
            Contract newContract = contractService.createContract(user.getUserId(), vehicleId, durationMonths);
            return ResponseEntity.ok(newContract);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- ENDPOINT MỚI ĐỂ KÝ HỢP ĐỒNG ---
    /**
     * API Ký hợp đồng
     * URL: PUT http://localhost:8081/api/contracts/{contractId}/sign
     */
    @PutMapping("/{contractId}/sign")
    public ResponseEntity<?> signContract(
            @PathVariable Long contractId,
            Authentication authentication) {

        try {
            User user = getUserFromAuth(authentication);
            Contract signedContract = contractService.signContract(contractId, user.getUserId());
            return ResponseEntity.ok(signedContract);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage()); // Lỗi 403 Forbidden
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // Lỗi 400
        }
    }

    // --- Helper ---
    private User getUserFromAuth(Authentication authentication) {
        String email = authentication.getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không xác định"));
    }
}