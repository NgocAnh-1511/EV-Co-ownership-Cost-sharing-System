package com.example.user_account_service.controller;

import com.example.user_account_service.dto.ContractDetailDTO;
import com.example.user_account_service.dto.VehicleDTO; // <-- Dùng DTO
import com.example.user_account_service.entity.Contract;
import com.example.user_account_service.entity.User;
import com.example.user_account_service.service.ContractService;
import com.example.user_account_service.service.UserService;
import com.example.user_account_service.service.VehicleDataClient; // <-- Dùng Client

// (Đã xóa các import trỏ đến VehicleServiceManagementService)

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@CrossOrigin(origins = "http://localhost:8080")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private UserService userService;

    // Hoàn tác: Dùng lại VehicleDataClient
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
        // Gọi API qua client
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

    private User getUserFromAuth(Authentication authentication) {
        String email = authentication.getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không xác định"));
    }
}