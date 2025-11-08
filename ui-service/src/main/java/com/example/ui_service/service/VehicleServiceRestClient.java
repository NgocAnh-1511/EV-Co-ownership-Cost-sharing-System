package com.example.ui_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class VehicleServiceRestClient {

    private final String BASE_URL = "http://localhost:8083/api/vehicleservices";

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Đăng ký dịch vụ xe mới
     * @param serviceData Dữ liệu dịch vụ cần đăng ký
     * @return Kết quả đăng ký
     */
    public Map<String, Object> registerVehicleService(Map<String, Object> serviceData) {
        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(serviceData);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL,
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Lỗi khi đăng ký dịch vụ xe: " + e.getMessage());
            throw new RuntimeException("Không thể đăng ký dịch vụ: " + e.getMessage(), e);
        }
    }
}


