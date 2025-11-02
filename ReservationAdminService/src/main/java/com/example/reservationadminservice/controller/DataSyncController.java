package com.example.reservationadminservice.controller;

import com.example.reservationadminservice.service.DataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller để test đồng bộ dữ liệu thủ công
 */
@RestController
@RequestMapping("/api/admin/sync")
@RequiredArgsConstructor
public class DataSyncController {

    private final DataSyncService dataSyncService;

    /**
     * Trigger đồng bộ dữ liệu thủ công
     * URL: POST http://localhost:8082/api/admin/sync/trigger
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> triggerSync() {
        dataSyncService.syncManually();
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Đồng bộ dữ liệu thành công!");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "running");
        response.put("message", "Data sync service đang hoạt động");
        
        return ResponseEntity.ok(response);
    }
}













