package com.example.ui_service.service;

import com.example.ui_service.model.ServiceDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ServiceRestClient {

    private final String BASE_URL = "http://localhost:8083/api/services";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Lấy tất cả các dịch vụ từ bảng service trong database
     * Gọi API: GET http://localhost:8083/api/services
     * API này sẽ query từ bảng service trong database vehicle_management
     * @return Danh sách dịch vụ
     */
    public List<ServiceDTO> getAllServices() {
        try {
            System.out.println("🔍 Đang gọi API: " + BASE_URL + " để lấy danh sách dịch vụ từ bảng service");
            Map[] services = restTemplate.getForObject(BASE_URL, Map[].class);
            if (services == null || services.length == 0) {
                System.out.println("⚠️ Không có dịch vụ nào trong database");
                return Collections.emptyList();
            }
            
            List<ServiceDTO> serviceDTOList = new ArrayList<>();
            for (Map<String, Object> service : services) {
                ServiceDTO dto = new ServiceDTO();
                dto.setServiceId((String) service.get("serviceId"));
                dto.setServiceName((String) service.get("serviceName"));
                dto.setServiceType((String) service.get("serviceType"));
                serviceDTOList.add(dto);
            }
            System.out.println("✅ Đã lấy " + serviceDTOList.size() + " dịch vụ từ bảng service");
            return serviceDTOList;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách dịch vụ từ bảng service: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Lấy dịch vụ theo ID
     * @param serviceId ID của dịch vụ
     * @return ServiceDTO hoặc null
     */
    public ServiceDTO getServiceById(String serviceId) {
        try {
            Map<String, Object> service = restTemplate.getForObject(BASE_URL + "/" + serviceId, Map.class);
            if (service == null) {
                return null;
            }
            ServiceDTO dto = new ServiceDTO();
            dto.setServiceId((String) service.get("serviceId"));
            dto.setServiceName((String) service.get("serviceName"));
            dto.setServiceType((String) service.get("serviceType"));
            return dto;
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy dịch vụ theo ID: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy danh sách các loại dịch vụ từ bảng service
     * @return Danh sách loại dịch vụ
     */
    public List<String> getServiceTypes() {
        try {
            System.out.println("🔍 Đang gọi API: " + BASE_URL + "/types để lấy danh sách loại dịch vụ");
            String[] types = restTemplate.getForObject(BASE_URL + "/types", String[].class);
            if (types == null || types.length == 0) {
                System.out.println("⚠️ Không có loại dịch vụ nào trong database");
                return Collections.emptyList();
            }
            List<String> typeList = Arrays.asList(types);
            System.out.println("✅ Đã lấy " + typeList.size() + " loại dịch vụ từ bảng service");
            return typeList;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách loại dịch vụ từ bảng service: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
