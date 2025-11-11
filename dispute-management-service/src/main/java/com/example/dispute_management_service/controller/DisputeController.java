package com.example.dispute_management_service.controller;

import com.example.dispute_management_service.config.CustomUserDetails; // <-- THÊM IMPORT
import com.example.dispute_management_service.dto.CreateDisputeRequest; // <-- THÊM IMPORT
import com.example.dispute_management_service.entity.Dispute;
import com.example.dispute_management_service.service.DisputeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // <-- THÊM IMPORT
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/disputes")
@CrossOrigin(origins = "http://localhost:8080")
public class DisputeController {

    @Autowired
    private DisputeService disputeService;

    /**
     * NÂNG CẤP: Tự động lấy User ID từ JWT Token
     * URL: GET http://localhost:8083/api/disputes/my-disputes
     */
    @GetMapping("/my-disputes")
    public ResponseEntity<List<Dispute>> getMyDisputes(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // Lấy ID từ đối tượng User đã được xác thực
        Long userId = userDetails.getUserId();
        List<Dispute> disputes = disputeService.getDisputesByUserId(userId);
        return ResponseEntity.ok(disputes);
    }

    /**
     * NÂNG CẤP: Tự động lấy User ID từ JWT Token và dùng DTO
     * URL: POST http://localhost:8083/api/disputes
     */
    @PostMapping
    public ResponseEntity<Dispute> createDispute(@RequestBody CreateDisputeRequest request,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();

        Dispute createdDispute = disputeService.createDispute(request, userId);
        return new ResponseEntity<>(createdDispute, HttpStatus.CREATED);
    }

    /**
     * API Lấy chi tiết một tranh chấp (Giữ nguyên)
     * URL: GET http://localhost:8083/api/disputes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Dispute> getDisputeById(@PathVariable Long id) {
        Optional<Dispute> dispute = disputeService.getDisputeById(id);
        return dispute.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- CÁC API DÀNH CHO ADMIN ---
    // (Chúng ta nên chuyển các API này sang AdminController riêng)

    /**
     * API Lấy tất cả tranh chấp (Admin)
     * URL: GET http://localhost:8083/api/disputes/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<Dispute>> getAllDisputes() {
        // Cần thêm logic bảo mật để chỉ Admin mới gọi được
        List<Dispute> allDisputes = disputeService.getAllDisputes();
        return ResponseEntity.ok(allDisputes);
    }

    /**
     * API Cập nhật trạng thái tranh chấp (Admin)
     * URL: PUT http://localhost:8083/api/disputes/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Dispute> updateStatus(@PathVariable Long id, @RequestParam String status) {
        // Cần thêm logic bảo mật để chỉ Admin mới gọi được
        Optional<Dispute> updatedDispute = disputeService.updateDisputeStatus(id, status);

        return updatedDispute.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}