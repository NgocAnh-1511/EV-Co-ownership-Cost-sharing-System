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
            System.out.println("🔍 [VehicleRestClient] Đang gọi API: " + BASE_URL + " để lấy danh sách xe từ bảng vehicle");
            
            // Sử dụng ResponseEntity để có thêm thông tin về response
            org.springframework.http.ResponseEntity<Map[]> response = restTemplate.getForEntity(BASE_URL, Map[].class);
            
            System.out.println("   - Response status: " + response.getStatusCode());
            
            Map[] vehicles = response.getBody();
            if (vehicles == null || vehicles.length == 0) {
                System.out.println("⚠️ [VehicleRestClient] API trả về null hoặc rỗng");
                System.out.println("   - Response body: " + response.getBody());
                return Collections.emptyList();
            }
            
            System.out.println("   - Nhận được " + vehicles.length + " xe từ API");
            
            // Log chi tiết xe đầu tiên để debug
            if (vehicles.length > 0) {
                Map<String, Object> firstVehicle = vehicles[0];
                System.out.println("   - Xe đầu tiên keys: " + firstVehicle.keySet());
                System.out.println("   - Xe đầu tiên data: " + firstVehicle);
            }
            
            List<VehicleDTO> vehicleDTOList = new ArrayList<>();
            for (Map<String, Object> vehicle : vehicles) {
                try {
                    VehicleDTO dto = new VehicleDTO();
                    
                    // Parse vehicleId
                    Object vehicleIdObj = vehicle.get("vehicleId");
                    if (vehicleIdObj != null) {
                        dto.setVehicleId(vehicleIdObj.toString());
                    }
                    
                    // Parse vehicleNumber
                    Object vehicleNumberObj = vehicle.get("vehicleNumber");
                    if (vehicleNumberObj != null) {
                        dto.setVehicleNumber(vehicleNumberObj.toString());
                    }
                    
                    // Parse vehicleType (có thể là "vehicleType" hoặc "type")
                    Object vehicleTypeObj = vehicle.get("vehicleType");
                    if (vehicleTypeObj == null) {
                        vehicleTypeObj = vehicle.get("type");
                    }
                    if (vehicleTypeObj != null) {
                        dto.setType(vehicleTypeObj.toString());
                    }
                    
                    // Parse status
                    Object statusObj = vehicle.get("status");
                    if (statusObj != null) {
                        dto.setStatus(statusObj.toString());
                    }
                    
                    vehicleDTOList.add(dto);
                    System.out.println("   - Đã parse xe: " + dto.getVehicleId() + " - " + dto.getVehicleNumber() + " (" + dto.getType() + ")");
                } catch (Exception e) {
                    System.err.println("   ⚠️ Lỗi khi parse xe: " + vehicle + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("✅ [VehicleRestClient] Đã lấy " + vehicleDTOList.size() + " xe từ bảng vehicle");
            return vehicleDTOList;
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            System.err.println("❌ [VehicleRestClient] Không thể kết nối đến backend service: " + e.getMessage());
            System.err.println("   - Đảm bảo VehicleServiceManagementService đang chạy trên port 8083");
            e.printStackTrace();
            return Collections.emptyList();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ [VehicleRestClient] Lỗi HTTP khi gọi API: " + e.getStatusCode() + " - " + e.getMessage());
            System.err.println("   - Response body: " + e.getResponseBodyAsString());
            e.printStackTrace();
            return Collections.emptyList();
        } catch (Exception e) {
            System.err.println("❌ [VehicleRestClient] Lỗi không xác định khi lấy danh sách xe: " + e.getMessage());
            System.err.println("   - Error type: " + e.getClass().getName());
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
