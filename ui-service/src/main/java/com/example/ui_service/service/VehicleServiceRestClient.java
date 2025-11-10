package com.example.ui_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
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
            System.out.println("📡 [REST CLIENT] Gọi API đăng ký dịch vụ: " + BASE_URL);
            System.out.println("   Request data: " + serviceData);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(serviceData);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL,
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("✅ [REST CLIENT] Đăng ký dịch vụ thành công");
                return response.getBody();
            } else {
                System.err.println("❌ [REST CLIENT] Đăng ký dịch vụ thất bại: " + response.getStatusCode());
                throw new RuntimeException("Đăng ký dịch vụ thất bại với status: " + response.getStatusCode());
            }
            
        } catch (RestClientException e) {
            System.err.println("❌ [REST CLIENT] Lỗi khi gọi API đăng ký dịch vụ: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể đăng ký dịch vụ: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("❌ [REST CLIENT] Lỗi không mong đợi: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể đăng ký dịch vụ: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy tất cả dịch vụ xe
     * @return Danh sách dịch vụ xe dạng List<Map<String, Object>>
     */
    public List<Map<String, Object>> getAllVehicleServices() {
        try {
            System.out.println("📡 [REST CLIENT] Gọi API lấy tất cả dịch vụ xe: " + BASE_URL);
            
            ParameterizedTypeReference<List<Map<String, Object>>> typeRef = 
                new ParameterizedTypeReference<List<Map<String, Object>>>() {};
            
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    BASE_URL,
                    HttpMethod.GET,
                    null,
                    typeRef
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                List<Map<String, Object>> body = response.getBody();
                if (body != null) {
                    System.out.println("✅ [REST CLIENT] Đã lấy được " + body.size() + " dịch vụ từ API");
                    
                    // Debug: Log cấu trúc của service đầu tiên nếu có
                    if (!body.isEmpty()) {
                        Map<String, Object> firstService = body.get(0);
                        System.out.println("   📋 Cấu trúc service đầu tiên:");
                        System.out.println("      Keys: " + firstService.keySet());
                        System.out.println("      id: " + firstService.get("id"));
                        System.out.println("      serviceId: " + firstService.get("serviceId"));
                        System.out.println("      vehicleId: " + firstService.get("vehicleId"));
                        System.out.println("      serviceType: " + firstService.get("serviceType"));
                        System.out.println("      status: " + firstService.get("status"));
                    }
                    
                    return body;
                } else {
                    System.out.println("⚠️ [REST CLIENT] API trả về null body");
                    return new ArrayList<>();
                }
            } else {
                System.err.println("❌ [REST CLIENT] API trả về status: " + response.getStatusCode());
                return new ArrayList<>();
            }
            
        } catch (RestClientException e) {
            System.err.println("❌ [REST CLIENT] Lỗi khi gọi API lấy dịch vụ xe: " + e.getMessage());
            System.err.println("   - Kiểm tra xem backend service có đang chạy không: " + BASE_URL);
            System.err.println("   - Kiểm tra kết nối network");
            e.printStackTrace();
            return new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("❌ [REST CLIENT] Lỗi không mong đợi khi lấy dịch vụ xe: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Lấy danh sách dịch vụ theo vehicleId
     * @param vehicleId ID của xe
     * @return Danh sách dịch vụ của xe
     */
    public List<Map<String, Object>> getVehicleServicesByVehicleId(String vehicleId) {
        try {
            String url = BASE_URL + "/vehicle/" + vehicleId;
            System.out.println("📡 [REST CLIENT] Gọi API lấy dịch vụ theo vehicleId: " + url);
            
            ParameterizedTypeReference<List<Map<String, Object>>> typeRef = 
                new ParameterizedTypeReference<List<Map<String, Object>>>() {};
            
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    typeRef
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("✅ [REST CLIENT] Đã lấy được " + response.getBody().size() + " dịch vụ cho vehicle " + vehicleId);
                return response.getBody();
            } else {
                System.err.println("⚠️ [REST CLIENT] API trả về status: " + response.getStatusCode());
                return new ArrayList<>();
            }
            
        } catch (RestClientException e) {
            System.err.println("❌ [REST CLIENT] Lỗi khi lấy dịch vụ cho xe " + vehicleId + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("❌ [REST CLIENT] Lỗi không mong đợi: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Cập nhật trạng thái dịch vụ xe theo id
     * @param id ID của đăng ký dịch vụ
     * @param status Trạng thái mới
     * @return Kết quả cập nhật
     */
    public Map<String, Object> updateServiceStatusById(Integer id, String status) {
        try {
            String url = BASE_URL + "/" + id;
            System.out.println("📡 [REST CLIENT] Gọi API cập nhật trạng thái theo id: " + url);
            System.out.println("   ID: " + id);
            System.out.println("   Status: " + status);
            
            Map<String, Object> requestData = new java.util.HashMap<>();
            requestData.put("status", status);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestData);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("✅ [REST CLIENT] Cập nhật trạng thái thành công");
                return response.getBody();
            } else {
                System.err.println("❌ [REST CLIENT] Cập nhật trạng thái thất bại: " + response.getStatusCode());
                throw new RuntimeException("Cập nhật trạng thái thất bại với status: " + response.getStatusCode());
            }
            
        } catch (RestClientException e) {
            System.err.println("❌ [REST CLIENT] Lỗi khi cập nhật trạng thái: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể cập nhật trạng thái: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("❌ [REST CLIENT] Lỗi không mong đợi: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể cập nhật trạng thái: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cập nhật trạng thái dịch vụ xe (theo serviceId và vehicleId - lấy bản ghi mới nhất)
     * @param serviceId ID của dịch vụ
     * @param vehicleId ID của xe
     * @param status Trạng thái mới
     * @return Kết quả cập nhật
     * @deprecated Sử dụng updateServiceStatusById thay thế
     */
    @Deprecated
    public Map<String, Object> updateServiceStatus(String serviceId, String vehicleId, String status) {
        try {
            String url = BASE_URL + "/service/" + serviceId + "/vehicle/" + vehicleId;
            System.out.println("📡 [REST CLIENT] Gọi API cập nhật trạng thái: " + url);
            System.out.println("   Status: " + status);
            
            Map<String, Object> requestData = new java.util.HashMap<>();
            requestData.put("status", status);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestData);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("✅ [REST CLIENT] Cập nhật trạng thái thành công");
                return response.getBody();
            } else {
                System.err.println("❌ [REST CLIENT] Cập nhật trạng thái thất bại: " + response.getStatusCode());
                throw new RuntimeException("Cập nhật trạng thái thất bại với status: " + response.getStatusCode());
            }
            
        } catch (RestClientException e) {
            System.err.println("❌ [REST CLIENT] Lỗi khi cập nhật trạng thái: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể cập nhật trạng thái: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("❌ [REST CLIENT] Lỗi không mong đợi: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể cập nhật trạng thái: " + e.getMessage(), e);
        }
    }
}
