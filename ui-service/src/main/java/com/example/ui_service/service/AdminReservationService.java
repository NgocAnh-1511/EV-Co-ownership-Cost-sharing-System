package com.example.ui_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AdminReservationService {

    @Value("${reservation.admin.service.url:http://localhost:8084}")
    private String adminReservationServiceUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Lấy tất cả reservations từ Admin Reservation Service
     */
    public List<Map<String, Object>> getAllReservations() {
        try {
            String url = adminReservationServiceUrl + "/api/admin/reservations";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.set("Pragma", "no-cache");
            headers.set("Expires", "0");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            
            List<Map<String, Object>> result = response.getBody();
            System.out.println("📦 Fetched " + (result != null ? result.size() : 0) + " reservations from admin service");
            return result != null ? result : List.of();
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi lấy danh sách đặt lịch từ admin service: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
}

