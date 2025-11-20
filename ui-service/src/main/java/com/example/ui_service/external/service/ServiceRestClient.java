package com.example.ui_service.external.service;

import com.example.ui_service.external.model.ServiceDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ServiceRestClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ServiceRestClient(RestTemplate restTemplate,
                             @Value("${external.services.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<Map<String, Object>> getAllServices() {
        try {
            ParameterizedTypeReference<List<Map<String, Object>>> typeRef =
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {};
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    null,
                    typeRef
            );
            return response.getStatusCode().is2xxSuccessful() && response.getBody() != null
                    ? response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<String> getServiceTypes() {
        try {
            ParameterizedTypeReference<List<String>> typeRef =
                    new ParameterizedTypeReference<List<String>>() {};
            ResponseEntity<List<String>> response = restTemplate.exchange(
                    baseUrl + "/types",
                    HttpMethod.GET,
                    null,
                    typeRef
            );
            return response.getStatusCode().is2xxSuccessful() && response.getBody() != null
                    ? response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<ServiceDTO> getAllServicesAsDTO() {
        List<Map<String, Object>> maps = getAllServices();
        List<ServiceDTO> result = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            ServiceDTO dto = new ServiceDTO();
            dto.setServiceId((String) map.get("serviceId"));
            dto.setServiceName((String) map.get("serviceName"));
            dto.setServiceType((String) map.get("serviceType"));
            result.add(dto);
        }
        return result;
    }

    public Map<String, Object> addService(Map<String, Object> serviceData) {
        ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl, serviceData, Map.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }
        throw new RuntimeException("Thêm dịch vụ thất bại: " + response.getStatusCode());
    }
}


