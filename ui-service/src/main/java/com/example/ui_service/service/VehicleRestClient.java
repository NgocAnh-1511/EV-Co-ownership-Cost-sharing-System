package com.example.ui_service.service;

import com.example.ui_service.model.VehicleDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class VehicleRestClient {

    private final RestTemplate restTemplate;
    private final String BASE_URL = "http://localhost:8083/api/vehicles";

    public VehicleRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Lấy toàn bộ xe từ bảng vehicle trong database
     * Gọi API: GET http://localhost:8083/api/vehicles
     * API này sẽ query từ bảng vehicle trong database vehicle_management
     */
    public List<VehicleDTO> getAllVehicles() {
        try {
            System.out.println("🔍 Đang gọi API: " + BASE_URL + " để lấy danh sách xe từ bảng vehicle");
            Map[] vehicles = restTemplate.getForObject(BASE_URL, Map[].class);
            if (vehicles == null || vehicles.length == 0) {
                System.out.println("⚠️ Không có xe nào trong database");
                return Collections.emptyList();
            }
            
            List<VehicleDTO> vehicleDTOList = new ArrayList<>();
            for (Map<String, Object> vehicle : vehicles) {
                VehicleDTO dto = new VehicleDTO();
                dto.setVehicleId((String) vehicle.get("vehicleId"));
                dto.setVehicleNumber((String) vehicle.get("vehicleNumber"));
                dto.setType((String) vehicle.get("vehicleType")); // Map vehicleType từ API
                dto.setStatus((String) vehicle.get("status"));
                vehicleDTOList.add(dto);
            }
            System.out.println("✅ Đã lấy " + vehicleDTOList.size() + " xe từ bảng vehicle");
            return vehicleDTOList;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách xe từ bảng vehicle: " + e.getMessage());
            e.printStackTrace();
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
