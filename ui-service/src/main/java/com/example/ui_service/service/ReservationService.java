package com.example.ui_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final String API = "http://localhost:8081/api";
    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getReservationsByVehicleId(int vehicleId) {
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(API + "/vehicles/" + vehicleId + "/reservations", List.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi lấy danh sách đặt xe: " + e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> createReservation(Map<String, Object> data) {
        try {
            String url = API + "/reservations";

            // ✅ Đặt đúng tên key mà backend yêu cầu
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("vehicleId", data.get("vehicleId").toString());
            formData.add("userId", data.get("userId").toString());
            formData.add("startDate", data.get("startDate").toString());  // 👈 khớp backend
            formData.add("endDate", data.get("endDate").toString());      // 👈 khớp backend
            formData.add("purpose", data.get("note").toString());         // 👈 backend dùng "purpose"

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            return res.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi yêu cầu tạo đặt lịch: " + e.getMessage(), e);
        }
    }
}
