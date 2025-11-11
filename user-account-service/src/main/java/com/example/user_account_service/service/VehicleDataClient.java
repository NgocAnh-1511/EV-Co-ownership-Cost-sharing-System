package com.example.user_account_service.service;

import com.example.user_account_service.dto.VehicleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class VehicleDataClient {

    @Autowired
    private RestTemplate restTemplate;

    // Đọc URL từ tệp application.properties
    @Value("${vehicle.service.url}")
    private String VEHICLE_API_URL;

    /**
     * Lấy tất cả xe từ dịch vụ xe
     */
    public List<VehicleDTO> getAllVehicles() {
        try {
            // URL này bây giờ là "http://localhost:8082/api/vehicles"
            VehicleDTO[] vehicles = restTemplate.getForObject(VEHICLE_API_URL, VehicleDTO[].class);
            return (vehicles != null) ? Arrays.asList(vehicles) : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi VehicleService (getAllVehicles): " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Lấy một xe theo ID
     */
    public VehicleDTO getVehicleById(String vehicleId) {
        try {
            String url = VEHICLE_API_URL + "/" + vehicleId;
            return restTemplate.getForObject(url, VehicleDTO.class);
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi VehicleService (getVehicleById: " + vehicleId + "): " + e.getMessage());
            return null;
        }
    }
}