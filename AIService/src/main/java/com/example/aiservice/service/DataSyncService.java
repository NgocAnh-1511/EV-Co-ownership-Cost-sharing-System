package com.example.aiservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service để đồng bộ dữ liệu từ ReservationService
 */
@Service
@Slf4j
public class DataSyncService {
    
    private final RestTemplate restTemplate;
    
    @Value("${reservation.service.url:http://localhost:8081}")
    private String reservationServiceUrl;
    
    public DataSyncService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Lấy danh sách reservations từ ReservationService
     */
    public List<Map<String, Object>> getReservations(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            String url = String.format("%s/api/reservations?vehicleId=%d", 
                reservationServiceUrl, vehicleId);
            
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            
            List<Map<String, Object>> reservations = response.getBody();
            if (reservations != null) {
                // Lọc theo khoảng thời gian
                return reservations.stream()
                    .filter(r -> {
                        String startDatetimeStr = (String) r.get("startDatetime");
                        String endDatetimeStr = (String) r.get("endDatetime");
                        if (startDatetimeStr == null || endDatetimeStr == null) {
                            return false;
                        }
                        LocalDateTime resStart = LocalDateTime.parse(startDatetimeStr);
                        LocalDateTime resEnd = LocalDateTime.parse(endDatetimeStr);
                        return !resEnd.isBefore(startDate) && !resStart.isAfter(endDate);
                    })
                    .toList();
            }
            
            return List.of();
        } catch (Exception e) {
            log.error("Error fetching reservations from ReservationService: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Tính toán usage statistics từ reservations
     */
    public Map<Long, UsageData> calculateUsageFromReservations(
            List<Map<String, Object>> reservations) {
        
        Map<Long, UsageData> usageMap = new HashMap<>();
        
        for (Map<String, Object> reservation : reservations) {
            String status = (String) reservation.get("status");
            if ("CANCELLED".equals(status)) {
                continue; // Bỏ qua các booking đã hủy
            }
            
            // Lấy userId
            Long userId = extractUserId(reservation);
            if (userId == null) continue;
            
            UsageData data = usageMap.computeIfAbsent(userId, k -> new UsageData());
            
            // Tính toán thời gian sử dụng
            String startDatetimeStr = (String) reservation.get("startDatetime");
            String endDatetimeStr = (String) reservation.get("endDatetime");
            
            if (startDatetimeStr != null && endDatetimeStr != null) {
                LocalDateTime startTime = LocalDateTime.parse(startDatetimeStr);
                LocalDateTime endTime = LocalDateTime.parse(endDatetimeStr);
                
                Duration duration = Duration.between(startTime, endTime);
                double hours = duration.toMinutes() / 60.0;
                
                data.totalHours += hours;
                data.bookingCount++;
                
                if ("COMPLETED".equals(status)) {
                    data.completedCount++;
                }
            }
            
            // Cập nhật cancellation count nếu status = CANCELLED
            if ("CANCELLED".equals(status)) {
                data.cancellationCount++;
            }
        }
        
        return usageMap;
    }
    
    private Long extractUserId(Map<String, Object> reservation) {
        Object userObj = reservation.get("user");
        if (userObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> userMap = (Map<String, Object>) userObj;
            Object userIdObj = userMap.get("userId");
            if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            }
        }
        return null;
    }
    
    /**
     * Lấy thông tin user
     */
    public Map<String, Object> getUserInfo(Long userId) {
        try {
            String url = String.format("%s/api/users/%d", reservationServiceUrl, userId);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching user info: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Lấy thông tin vehicle
     */
    public Map<String, Object> getVehicleInfo(Long vehicleId) {
        try {
            String url = String.format("%s/api/vehicles/%d", reservationServiceUrl, vehicleId);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching vehicle info: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Class để lưu trữ dữ liệu usage tạm thời
     */
    public static class UsageData {
        public double totalHours = 0.0;
        public int bookingCount = 0;
        public int completedCount = 0;
        public int cancellationCount = 0;
    }
}
















