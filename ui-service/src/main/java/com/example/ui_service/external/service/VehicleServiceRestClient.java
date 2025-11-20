package com.example.ui_service.external.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VehicleServiceRestClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public VehicleServiceRestClient(RestTemplate restTemplate,
                                    @Value("${external.vehicleservices.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public Map<String, Object> registerVehicleService(Map<String, Object> serviceData) {
        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(serviceData);
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("Đăng ký dịch vụ thất bại: " + response.getStatusCode());
        } catch (RestClientException e) {
            throw new RuntimeException("Không thể đăng ký dịch vụ: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getAllVehicleServices() {
        try {
            ParameterizedTypeReference<List<Map<String, Object>>> typeRef =
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {};
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    null,
                    typeRef
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getVehicleServicesByVehicleId(String vehicleId) {
        try {
            ParameterizedTypeReference<List<Map<String, Object>>> typeRef =
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {};
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    baseUrl + "/vehicle/" + vehicleId,
                    HttpMethod.GET,
                    null,
                    typeRef
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Map<String, Object> updateServiceStatus(String serviceId, String vehicleId, String status) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/service/" + serviceId + "/vehicle/" + vehicleId,
                HttpMethod.PUT,
                request,
                Map.class
        );
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }
        throw new RuntimeException("Cập nhật trạng thái thất bại: " + response.getStatusCode());
    }
}


