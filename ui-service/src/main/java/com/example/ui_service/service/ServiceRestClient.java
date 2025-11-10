package com.example.ui_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceRestClient {

    private final String BASE_URL = "http://localhost:8083/api/services";

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Lấy tất cả các dịch vụ từ bảng service
     * @return Danh sách dịch vụ dạng Map
     */
    public List<Map<String, Object>> getAllServices() {
        try {
            System.out.println("📡 [SERVICE REST CLIENT] Gọi API lấy danh sách dịch vụ: " + BASE_URL);
            
            // Backend API trả về List<ServiceType>, nhưng RestTemplate sẽ convert sang Map tự động
            ParameterizedTypeReference<List<Map<String, Object>>> typeRef = 
                new ParameterizedTypeReference<List<Map<String, Object>>>() {};
            
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    BASE_URL,
                    HttpMethod.GET,
                    null,
                    typeRef
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> services = response.getBody();
                System.out.println("✅ [SERVICE REST CLIENT] Đã lấy được " + services.size() + " dịch vụ");
                
                // Debug: Log service đầu tiên nếu có
                if (!services.isEmpty()) {
                    System.out.println("   📋 Service đầu tiên: " + services.get(0));
                }
                
                return services;
            } else {
                System.err.println("⚠️ [SERVICE REST CLIENT] API trả về status: " + response.getStatusCode());
                return new ArrayList<>();
            }
            
        } catch (RestClientException e) {
            System.err.println("❌ [SERVICE REST CLIENT] Lỗi khi lấy danh sách dịch vụ: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("❌ [SERVICE REST CLIENT] Lỗi không mong đợi: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Lấy danh sách các loại dịch vụ duy nhất từ bảng service
     * @return Danh sách loại dịch vụ (service_type)
     */
    public List<String> getServiceTypes() {
        try {
            System.out.println("📡 [SERVICE REST CLIENT] Lấy danh sách loại dịch vụ từ: " + BASE_URL + "/types");
            
            ParameterizedTypeReference<List<String>> typeRef = 
                new ParameterizedTypeReference<List<String>>() {};
            
            ResponseEntity<List<String>> response = restTemplate.exchange(
                    BASE_URL + "/types",
                    HttpMethod.GET,
                    null,
                    typeRef
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("✅ [SERVICE REST CLIENT] Đã lấy được " + response.getBody().size() + " loại dịch vụ");
                return response.getBody();
            } else {
                System.err.println("⚠️ [SERVICE REST CLIENT] API trả về status: " + response.getStatusCode());
                return new ArrayList<>();
            }
            
        } catch (RestClientException e) {
            System.err.println("❌ [SERVICE REST CLIENT] Lỗi khi lấy danh sách loại dịch vụ: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("❌ [SERVICE REST CLIENT] Lỗi không mong đợi: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Lấy tất cả các dịch vụ từ bảng service và convert sang ServiceDTO
     * @return Danh sách dịch vụ dạng ServiceDTO
     */
    public List<com.example.ui_service.model.ServiceDTO> getAllServicesAsDTO() {
        try {
            List<Map<String, Object>> servicesMap = getAllServices();
            List<com.example.ui_service.model.ServiceDTO> servicesDTO = new ArrayList<>();
            
            for (Map<String, Object> serviceMap : servicesMap) {
                com.example.ui_service.model.ServiceDTO dto = new com.example.ui_service.model.ServiceDTO();
                dto.setServiceId((String) serviceMap.get("serviceId"));
                dto.setServiceName((String) serviceMap.get("serviceName"));
                dto.setServiceType((String) serviceMap.get("serviceType"));
                servicesDTO.add(dto);
            }
            
            return servicesDTO;
        } catch (Exception e) {
            System.err.println("❌ [SERVICE REST CLIENT] Lỗi khi convert sang ServiceDTO: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Thêm dịch vụ mới vào bảng service
     * @param serviceData Dữ liệu dịch vụ cần thêm (serviceId, serviceName, serviceType)
     * @return Kết quả thêm dịch vụ
     */
    public Map<String, Object> addService(Map<String, Object> serviceData) {
        try {
            System.out.println("📡 [SERVICE REST CLIENT] Thêm dịch vụ mới: " + BASE_URL);
            System.out.println("   Request data: " + serviceData);
            
            org.springframework.http.HttpEntity<Map<String, Object>> request = 
                new org.springframework.http.HttpEntity<>(serviceData);
            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL,
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("✅ [SERVICE REST CLIENT] Đã thêm dịch vụ mới thành công");
                return response.getBody();
            } else {
                System.err.println("❌ [SERVICE REST CLIENT] Thêm dịch vụ thất bại: " + response.getStatusCode());
                throw new RuntimeException("Thêm dịch vụ thất bại với status: " + response.getStatusCode());
            }
            
        } catch (RestClientException e) {
            System.err.println("❌ [SERVICE REST CLIENT] Lỗi khi thêm dịch vụ: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể thêm dịch vụ: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("❌ [SERVICE REST CLIENT] Lỗi không mong đợi: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể thêm dịch vụ: " + e.getMessage(), e);
        }
    }
}
