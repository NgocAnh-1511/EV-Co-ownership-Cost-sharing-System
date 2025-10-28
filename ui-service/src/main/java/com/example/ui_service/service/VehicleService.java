package com.example.ui_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private static final String API = "http://localhost:8081/api";
    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getVehicles() {
        try {
            ResponseEntity<List> res = restTemplate.getForEntity(API + "/vehicles", List.class);
            return res.getBody();
        } catch (Exception e) {
            System.err.println("⚠️ Không thể kết nối Reservation Service: " + e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getReservations(Long vehicleId) {
        try {
            ResponseEntity<List> res = restTemplate.getForEntity(API + "/vehicles/" + vehicleId + "/reservations", List.class);
            return res.getBody();
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi khi lấy danh sách đặt xe: " + e.getMessage());
            return List.of();
        }
    }

    public boolean isAvailable(Long vehicleId, LocalDateTime start, LocalDateTime end) {
        try {
            String url = API + "/availability?vehicleId=" + vehicleId +
                    "&start=" + start + "&end=" + end;
            ResponseEntity<Boolean> res = restTemplate.getForEntity(url, Boolean.class);
            return Boolean.TRUE.equals(res.getBody());
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi kiểm tra xe trống: " + e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> createReservation(Long vehicleId, Long userId,
                                                 LocalDateTime start, LocalDateTime end, String note) {
        String url = API + "/reservations";

        // ✅ Gửi dữ liệu dạng form URL-encoded
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("vehicleId", String.valueOf(vehicleId));
        formData.add("userId", String.valueOf(userId));
        formData.add("start", start.toString());
        formData.add("end", end.toString());
        formData.add("purpose", note);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // ✅ RestTemplate có converter cho form data
        RestTemplate rest = new RestTemplate();
        rest.getMessageConverters().add(new org.springframework.http.converter.FormHttpMessageConverter());

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        // ✅ Gửi request và nhận phản hồi
        ResponseEntity<Map> response = rest.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        return response.getBody();
    }
}
