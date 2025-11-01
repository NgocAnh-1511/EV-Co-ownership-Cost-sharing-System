package com.example.ui_service.service;

import com.example.ui_service.model.VehicleDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class VehicleRestClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "http://localhost:8083/api/vehicles"; // Kiểm tra lại URL

    // 🔹 Lấy toàn bộ xe từ VehicleService
    public List<VehicleDTO> getAllVehicles() {
        try {
            VehicleDTO[] list = restTemplate.getForObject(BASE_URL, VehicleDTO[].class);
            return Arrays.asList(list != null ? list : new VehicleDTO[0]);
        } catch (Exception e) {
            System.out.println("⚠️ [VehicleService] Lỗi khi lấy danh sách xe: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // 🔹 Lấy danh sách xe đang hoạt động
    public List<VehicleDTO> getActiveVehicles() {
        return getAllVehicles().stream()
                .filter(v -> "AVAILABLE".equalsIgnoreCase(v.getStatus()) || "RENTED".equalsIgnoreCase(v.getStatus()))
                .toList();
    }

    // 🔹 Đếm số xe đang được thuê
    public int countActiveRentals() {
        return (int) getAllVehicles().stream()
                .filter(v -> "RENTED".equalsIgnoreCase(v.getStatus()))
                .count();
    }
}
