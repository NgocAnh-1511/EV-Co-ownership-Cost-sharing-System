package com.example.user_account_service.controller;

import com.example.user_account_service.dto.*;
import com.example.user_account_service.entity.OwnershipShare;
import com.example.user_account_service.entity.User; // <-- Thêm import
import com.example.user_account_service.service.OwnershipShareService;
import com.example.user_account_service.service.UserService; // <-- Thêm import
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // <-- Thêm import
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ownerships")
@CrossOrigin(origins = {"http://localhost:8080"})
public class OwnershipShareController {
    private final OwnershipShareService service;

    // NÂNG CẤP: Tiêm (Inject) UserService
    private final UserService userService;

    public OwnershipShareController(OwnershipShareService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    /**
     * NÂNG CẤP MỚI: API lấy tất cả tỷ lệ sở hữu của người dùng đã đăng nhập
     * URL: GET http://localhost:8081/api/ownerships/my-shares
     */
    @GetMapping("/my-shares")
    public ResponseEntity<List<OwnershipShareDetailDTO>> getMyShares(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        List<OwnershipShareDetailDTO> shares = service.listSharesByUserId(user.getUserId());
        return ResponseEntity.ok(shares);
    }

    @GetMapping
    public List<OwnershipShareDTO> list(@RequestParam String vehicleId) {
        return service.listByVehicle(vehicleId);
    }

    @PostMapping
    public OwnershipShare create(@Valid @RequestBody OwnershipShareCreateReq req) {
        return service.create(req);
    }

    @PutMapping
    public OwnershipShare update(@Valid @RequestBody OwnershipShareUpdateReq req) {
        return service.update(req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Helper ---
    private User getUserFromAuth(Authentication authentication) {
        String email = authentication.getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không xác định"));
    }
}