package com.example.ui_service.service;

import com.example.ui_service.model.CheckinoutDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class CheckinoutRestClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "http://localhost:8082/api/checkinout";

    // Lấy danh sách checkin/checkout
    public List<CheckinoutDTO> getAllLogs() {
        try {
            CheckinoutDTO[] list = restTemplate.getForObject(BASE_URL, CheckinoutDTO[].class);
            return Arrays.asList(list != null ? list : new CheckinoutDTO[0]);
        } catch (Exception e) {
            System.out.println("⚠️ [CheckinoutService] Lỗi khi lấy danh sách check-in/check-out: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
