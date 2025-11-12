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
                    
                    // Parse vehicleName (tên xe từ cột vehiclename trong bảng vehicle)
                    // Hỗ trợ cả camelCase "vehicleName" và các biến thể khác
                    Object vehicleNameObj = vehicle.get("vehicleName");
                    if (vehicleNameObj == null) {
                        vehicleNameObj = vehicle.get("vehiclename"); // lowercase
                    }
                    if (vehicleNameObj != null && !vehicleNameObj.toString().trim().isEmpty()) {
                        dto.setName(vehicleNameObj.toString().trim());
                        System.out.println("   - Tìm thấy vehicleName: " + dto.getName());
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
                    
                    // Parse group information
                    Object groupObj = vehicle.get("group");
                    if (groupObj instanceof Map) {
                        Map<String, Object> groupMap = (Map<String, Object>) groupObj;
                        Object groupIdObj = groupMap.get("groupId");
                        if (groupIdObj != null) {
                            dto.setGroupId(groupIdObj.toString());
                        }
                        
                        // Chỉ sử dụng group name nếu vehicleName chưa được set
                        // (vehicleName đã được parse trước đó, nếu có)
                        Object groupNameObj = groupMap.get("name");
                        if (groupNameObj != null && (dto.getName() == null || dto.getName().trim().isEmpty())) {
                            dto.setName(groupNameObj.toString());
                        }
                    }
                    
                    // Parse lastServiceDate if available
                    // Note: API might return date as string or timestamp
                    Object lastServiceDateObj = vehicle.get("lastServiceDate");
                    if (lastServiceDateObj != null) {
                        // Handle different date formats
                        try {
                            if (lastServiceDateObj instanceof String) {
                                // Try parsing ISO format
                                dto.setLastServiceDate(java.time.LocalDateTime.parse(
                                    lastServiceDateObj.toString().replace("Z", ""),
                                    java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
                                ));
                            }
                        } catch (Exception e) {
                            // If parsing fails, leave it null
                            System.out.println("   - Không thể parse lastServiceDate: " + lastServiceDateObj);
                        }
                    }
                    
                    // Ưu tiên hiển thị: vehicleName > vehicleNumber > vehicleId
                    // Nếu vehicleName đã được set từ API, giữ nguyên
                    // Nếu chưa có, thử dùng vehicleNumber hoặc vehicleId
                    if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                        if (dto.getVehicleNumber() != null && !dto.getVehicleNumber().trim().isEmpty()) {
                            dto.setName(dto.getVehicleNumber());
                        } else if (dto.getVehicleId() != null) {
                            dto.setName(dto.getVehicleId());
                        } else {
                            dto.setName("Xe chưa có tên");
                        }
                    }
                    
                    vehicleDTOList.add(dto);
                    System.out.println("   - Đã parse xe: " + dto.getVehicleId() + 
                                     " - Tên: " + dto.getName() + 
                                     " - Biển số: " + dto.getVehicleNumber() + 
                                     " (" + dto.getType() + ") - " + dto.getStatus());
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

    /**
     * Lấy chi tiết xe theo ID
     * @param vehicleId ID của xe
     * @return VehicleDTO hoặc null nếu không tìm thấy
     */
    public VehicleDTO getVehicleById(String vehicleId) {
        try {
            String url = BASE_URL + "/" + vehicleId;
            org.springframework.http.ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> vehicle = response.getBody();
                VehicleDTO dto = new VehicleDTO();
                
                // Parse các field tương tự như getAllVehicles()
                if (vehicle.get("vehicleId") != null) {
                    dto.setVehicleId(vehicle.get("vehicleId").toString());
                }
                if (vehicle.get("vehicleNumber") != null) {
                    dto.setVehicleNumber(vehicle.get("vehicleNumber").toString());
                }
                Object vehicleNameObj = vehicle.get("vehicleName");
                if (vehicleNameObj == null) {
                    vehicleNameObj = vehicle.get("vehiclename");
                }
                if (vehicleNameObj != null && !vehicleNameObj.toString().trim().isEmpty()) {
                    dto.setName(vehicleNameObj.toString().trim());
                }
                Object vehicleTypeObj = vehicle.get("vehicleType");
                if (vehicleTypeObj == null) {
                    vehicleTypeObj = vehicle.get("type");
                }
                if (vehicleTypeObj != null) {
                    dto.setType(vehicleTypeObj.toString());
                }
                if (vehicle.get("status") != null) {
                    dto.setStatus(vehicle.get("status").toString());
                }
                Object groupObj = vehicle.get("group");
                if (groupObj instanceof Map) {
                    Map<String, Object> groupMap = (Map<String, Object>) groupObj;
                    if (groupMap.get("groupId") != null) {
                        dto.setGroupId(groupMap.get("groupId").toString());
                    }
                }
                
                return dto;
            }
            return null;
        } catch (Exception e) {
            System.err.println("❌ [VehicleRestClient] Lỗi khi lấy chi tiết xe: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Thêm xe mới
     * @param requestData Map chứa groupId và vehicle data
     * @return Object response từ API (có thể là List hoặc Map)
     */
    public Object addVehicle(Map<String, Object> requestData) {
        try {
            org.springframework.http.ResponseEntity<Object> response = restTemplate.postForEntity(
                BASE_URL + "/batch", 
                requestData, 
                Object.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            return null;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ [VehicleRestClient] Lỗi khi thêm xe: " + e.getStatusCode() + " - " + e.getMessage());
            String errorBody = e.getResponseBodyAsString();
            System.err.println("   - Response body: " + errorBody);
            throw new RuntimeException("Lỗi khi thêm xe: " + (errorBody != null ? errorBody : e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [VehicleRestClient] Lỗi khi thêm xe: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi thêm xe: " + e.getMessage());
        }
    }

    /**
     * Cập nhật xe
     * @param vehicleId ID của xe
     * @param vehicleData Map chứa thông tin cần cập nhật
     * @return Map response từ API
     */
    public Map<String, Object> updateVehicle(String vehicleId, Map<String, Object> vehicleData) {
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<Map<String, Object>> request = new org.springframework.http.HttpEntity<>(vehicleData, headers);
            
            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                BASE_URL + "/" + vehicleId,
                org.springframework.http.HttpMethod.PUT,
                request,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            return null;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ [VehicleRestClient] Lỗi khi cập nhật xe: " + e.getStatusCode() + " - " + e.getMessage());
            System.err.println("   - Response body: " + e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi khi cập nhật xe: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("❌ [VehicleRestClient] Lỗi khi cập nhật xe: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi cập nhật xe: " + e.getMessage());
        }
    }

    /**
     * Xóa xe
     * @param vehicleId ID của xe
     * @return true nếu xóa thành công
     */
    public boolean deleteVehicle(String vehicleId) {
        try {
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/" + vehicleId,
                org.springframework.http.HttpMethod.DELETE,
                null,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ [VehicleRestClient] Đã xóa xe " + vehicleId + " thành công");
                return true;
            } else {
                String errorMessage = response.getBody() != null ? response.getBody() : "Không thể xóa xe";
                throw new RuntimeException(errorMessage);
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ [VehicleRestClient] Lỗi khi xóa xe: " + e.getStatusCode() + " - " + e.getMessage());
            String errorBody = e.getResponseBodyAsString();
            System.err.println("   - Response body: " + errorBody);
            
            // Backend API trả về String message trong response body
            String errorMessage = errorBody != null && !errorBody.isEmpty() 
                ? errorBody 
                : "Lỗi khi xóa xe: " + e.getStatusCode();
            throw new RuntimeException(errorMessage);
        } catch (Exception e) {
            System.err.println("❌ [VehicleRestClient] Lỗi khi xóa xe: " + e.getMessage());
            e.printStackTrace();
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("Lỗi khi xóa xe: " + e.getMessage());
        }
    }
}
