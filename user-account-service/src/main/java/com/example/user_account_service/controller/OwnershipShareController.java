package com.example.user_account_service.controller; // Sửa package nếu cần

import com.example.user_account_service.dto.*;
import com.example.user_account_service.entity.OwnershipShare;
import com.example.user_account_service.service.OwnershipShareService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ownerships")
@CrossOrigin(origins = {"http://localhost:8080"})
public class OwnershipShareController {
    private final OwnershipShareService service;
    public OwnershipShareController(OwnershipShareService service) { this.service = service; }

    // NÂNG CẤP: Dùng String vehicleId và trả về DTO
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
}