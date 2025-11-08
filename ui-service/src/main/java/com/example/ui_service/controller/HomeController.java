package com.example.ui_service.controller;

import com.example.ui_service.model.ServiceDTO;
import com.example.ui_service.model.VehicleDTO;
import com.example.ui_service.model.VehiclegroupDTO;
import com.example.ui_service.service.VehicleGroupRestClient;
import com.example.ui_service.service.VehicleRestClient;
import com.example.ui_service.service.ServiceRestClient;
import com.example.ui_service.service.VehicleServiceRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private VehicleRestClient vehicleRestClient;

    @Autowired
    private ServiceRestClient serviceRestClient;

    @Autowired
    private VehicleServiceRestClient vehicleServiceRestClient;

    /**
     * Trang quản lý dịch vụ xe - GET
     * Hiển thị danh sách xe với các dịch vụ đang chờ xử lý:
     * - Tìm kiếm xe
     * - Lọc theo loại dịch vụ (Bảo dưỡng, Kiểm tra, Sửa chữa)
     * - Phân trang
     * - Thống kê (tổng số xe, sẵn sàng, bảo dưỡng, sửa chữa)
     */
    @GetMapping("/admin/vehicle-manager")
    public String vehicleManager(
            Model model,
            @RequestParam(value = "searchQuery", required = false, defaultValue = "") String searchQuery,
            @RequestParam(value = "serviceFilter", required = false, defaultValue = "all") String serviceFilter) {
        
            System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("🚀 [HOME CONTROLLER] Bắt đầu load trang /admin/vehicle-manager");
        System.out.println("   - searchQuery: " + searchQuery);
        System.out.println("   - serviceFilter: " + serviceFilter);
        System.out.println("   - Hiển thị TẤT CẢ dữ liệu (không phân trang)");
        
        try {
        model.addAttribute("pageTitle", "Quản Lý Các Dịch Vụ Xe");
            model.addAttribute("pageDescription", "Quản lý dịch vụ bảo dưỡng, kiểm tra và sửa chữa cho xe");
            
            // Lấy danh sách tất cả xe từ API (để hiển thị thông tin xe)
            System.out.println("📡 [STEP 1] Gọi API để lấy danh sách xe...");
            List<VehicleDTO> allVehicles = vehicleRestClient.getAllVehicles();
            System.out.println("✅ [STEP 1] Đã lấy " + (allVehicles != null ? allVehicles.size() : 0) + " xe từ API");
            if (allVehicles == null) {
                allVehicles = new ArrayList<>();
            }
            
            // Lấy danh sách tất cả dịch vụ xe từ API
            System.out.println("📡 [STEP 2] Gọi API để lấy danh sách dịch vụ xe...");
            List<Map<String, Object>> allVehicleServices = vehicleServiceRestClient.getAllVehicleServices();
            System.out.println("✅ [STEP 2] Đã lấy " + (allVehicleServices != null ? allVehicleServices.size() : 0) + " dịch vụ từ API");
            
            // Kiểm tra nếu không có dữ liệu
            if (allVehicleServices == null || allVehicleServices.isEmpty()) {
                System.out.println("⚠️ WARNING: Không có dữ liệu từ API vehicleservices!");
                System.out.println("   - Kiểm tra xem API có đang chạy không: http://localhost:8083/api/vehicleservices");
                System.out.println("   - Kiểm tra xem có dữ liệu trong bảng vehicleservice không");
                // Set giá trị mặc định
                allVehicleServices = new ArrayList<>();
            }
            
            // Debug: Log cấu trúc service đầu tiên nếu có
            if (!allVehicleServices.isEmpty()) {
                Map<String, Object> firstService = allVehicleServices.get(0);
                System.out.println("🔍 Debug - Cấu trúc service đầu tiên:");
                System.out.println("   - Keys: " + firstService.keySet());
                System.out.println("   - id: " + firstService.get("id"));
                System.out.println("   - vehicle: " + firstService.get("vehicle"));
                System.out.println("   - serviceType: " + firstService.get("serviceType"));
                System.out.println("   - serviceName: " + firstService.get("serviceName"));
                System.out.println("   - status: " + firstService.get("status"));
                
                if (firstService.get("id") instanceof Map) {
                    Map<String, Object> idMap = (Map<String, Object>) firstService.get("id");
                    System.out.println("   - id.vehicleId: " + idMap.get("vehicleId"));
                    System.out.println("   - id.serviceId: " + idMap.get("serviceId"));
                    System.out.println("   - id keys: " + idMap.keySet());
                }
                
                if (firstService.get("vehicle") instanceof Map) {
                    Map<String, Object> vehicleMap = (Map<String, Object>) firstService.get("vehicle");
                    System.out.println("   - vehicle.vehicleId: " + vehicleMap.get("vehicleId"));
                    System.out.println("   - vehicle keys: " + vehicleMap.keySet());
                }
            } else {
                System.out.println("⚠️ WARNING: Không có dịch vụ nào trong bảng vehicleservice!");
                System.out.println("   - Kiểm tra xem có dữ liệu trong bảng vehicleservice không");
            }
            
            // Helper method để lấy vehicleId từ service
            // JSON structure từ Vehicleservice entity với @EmbeddedId:
            // {
            //   "id": { "serviceId": "...", "vehicleId": "..." },  <-- Composite key
            //   "vehicle": { "vehicleId": "...", ... },
            //   "serviceType": "...",  <-- Cột service_type trong DB
            //   "status": "...",
            //   ...
            // }
            java.util.function.Function<Map<String, Object>, String> getVehicleId = service -> {
                String vehicleId = null;
                
                // Ưu tiên 1: Lấy từ id.vehicleId (composite key - đây là cách đúng nhất)
                Object idObj = service.get("id");
                if (idObj instanceof Map) {
                    Map<String, Object> idMap = (Map<String, Object>) idObj;
                    vehicleId = (String) idMap.get("vehicleId");
                    if (vehicleId != null && !vehicleId.trim().isEmpty()) {
                        return vehicleId.trim();
                    }
                }
                
                // Ưu tiên 2: Lấy từ vehicle.vehicleId (nested object)
                Object vehicleObj = service.get("vehicle");
                if (vehicleObj instanceof Map) {
                    Map<String, Object> vehicleMap = (Map<String, Object>) vehicleObj;
                    vehicleId = (String) vehicleMap.get("vehicleId");
                    if (vehicleId != null && !vehicleId.trim().isEmpty()) {
                        return vehicleId.trim();
                    }
                }
                
                // Fallback: thử lấy trực tiếp từ root (không có trong Vehicleservice entity nhưng thử để an toàn)
                vehicleId = (String) service.get("vehicleId");
                if (vehicleId != null && !vehicleId.trim().isEmpty()) {
                    return vehicleId.trim();
                }
                
                // Debug: log nếu không tìm thấy vehicleId
                System.out.println("⚠️ Không tìm thấy vehicleId trong service. Keys: " + service.keySet());
                if (idObj instanceof Map) {
                    System.out.println("   - id object keys: " + ((Map<String, Object>) idObj).keySet());
                }
                if (vehicleObj instanceof Map) {
                    System.out.println("   - vehicle object keys: " + ((Map<String, Object>) vehicleObj).keySet());
                }
                return "";
            };
            
            // Tính toán thống kê từ bảng vehicleservice
            // Helper method để lấy serviceType từ service (cột service_type trong bảng vehicleservice)
            // Định nghĩa như final để có thể sử dụng trong nested lambda
            // Lưu ý: serviceType là field trực tiếp trong Vehicleservice entity, không phải nested
            final java.util.function.Function<Map<String, Object>, String> getServiceTypeFunc = service -> {
                // Ưu tiên 1: Lấy trực tiếp từ field serviceType (camelCase - Jackson default)
                // Đây là cột service_type trong bảng vehicleservice
                String serviceType = (String) service.get("serviceType");
                if (serviceType != null && !serviceType.trim().isEmpty()) {
                    return serviceType.trim();
                }
                
                // Ưu tiên 2: Thử lấy từ service_type (snake_case - nếu có custom naming)
                serviceType = (String) service.get("service_type");
                if (serviceType != null && !serviceType.trim().isEmpty()) {
                    return serviceType.trim();
                }
                
                // Ưu tiên 3: Thử lấy từ nested service object (ServiceType entity)
                // Nhưng serviceType trong Vehicleservice là field riêng, không phải từ ServiceType
                Object serviceObj = service.get("service");
                if (serviceObj instanceof Map) {
                    Map<String, Object> serviceMap = (Map<String, Object>) serviceObj;
                    serviceType = (String) serviceMap.get("serviceType");
                    if (serviceType != null && !serviceType.trim().isEmpty()) {
                        return serviceType.trim();
                    }
                }
                
                // Debug: log nếu không tìm thấy
                System.out.println("⚠️ Không tìm thấy serviceType trong service. Keys: " + service.keySet());
                return null;
            };
            
            // Debug: Log cấu trúc JSON và dữ liệu thực tế
            System.out.println("🔍 ===== DEBUG: Phân tích dịch vụ từ bảng vehicleservice =====");
            System.out.println("📊 Tổng số records từ API: " + allVehicleServices.size());
            
            if (!allVehicleServices.isEmpty()) {
                Map<String, Object> firstService = allVehicleServices.get(0);
                System.out.println("📋 Cấu trúc service đầu tiên:");
                System.out.println("   - Keys ở root level: " + firstService.keySet());
                
                // Log id object
                Object idObj = firstService.get("id");
                if (idObj instanceof Map) {
                    Map<String, Object> idMap = (Map<String, Object>) idObj;
                    System.out.println("   - id object: " + idMap);
                    System.out.println("   - id.vehicleId: " + idMap.get("vehicleId"));
                    System.out.println("   - id.serviceId: " + idMap.get("serviceId"));
                }
                
                // Log serviceType
                System.out.println("   - serviceType (root): " + firstService.get("serviceType"));
                System.out.println("   - service_type (root): " + firstService.get("service_type"));
                System.out.println("   - status: " + firstService.get("status"));
                
                // Log vehicle object
                Object vehicleObj = firstService.get("vehicle");
                if (vehicleObj instanceof Map) {
                    Map<String, Object> vehicleMap = (Map<String, Object>) vehicleObj;
                    System.out.println("   - vehicle object: " + vehicleMap.keySet());
                    System.out.println("   - vehicle.vehicleId: " + vehicleMap.get("vehicleId"));
                }
            }
            
            System.out.println("\n🔍 Chi tiết từng service:");
            allVehicleServices.forEach(service -> {
                String vehicleId = getVehicleId.apply(service);
                String serviceType = getServiceTypeFunc.apply(service);
                String status = (String) service.get("status");
                System.out.println("   - VehicleId: [" + vehicleId + "], ServiceType: [" + serviceType + "], Status: [" + status + "]");
            });
            System.out.println("🔍 ===== END DEBUG =====\n");
            
            // Tổng số xe = số lượng vehicle_id duy nhất trong bảng vehicleservice
            long totalVehicles = 0;
            try {
                List<String> allVehicleIds = allVehicleServices.stream()
                        .map(getVehicleId)
                        .filter(id -> id != null && !id.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
                totalVehicles = allVehicleIds.size();
                System.out.println("📊 Tổng số xe (distinct): " + totalVehicles + " - " + allVehicleIds);
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi tính tổng số xe: " + e.getMessage());
                e.printStackTrace();
                totalVehicles = 0;
            }
            
            // Bảo dưỡng = số xe có serviceType chứa "Bảo dưỡng" hoặc "Maintenance" (case-insensitive)
            // Từ DB: có "Bảo dưỡng" (tiếng Việt) và "Maintenance" (tiếng Anh)
            long maintenanceVehicles = 0;
            try {
                List<String> maintenanceVehicleIds = allVehicleServices.stream()
                        .filter(service -> {
                            try {
                                String serviceType = getServiceTypeFunc.apply(service);
                                if (serviceType == null || serviceType.trim().isEmpty()) return false;
                                String st = serviceType.trim().toLowerCase();
                                // Match cả tiếng Việt và tiếng Anh
                                boolean matches = st.contains("bảo dưỡng") || 
                                       st.contains("maintenance") ||
                                       st.equals("bảo dưỡng") ||
                                       st.equals("maintenance");
                                if (matches) {
                                    System.out.println("   ✓ Bảo dưỡng: " + getVehicleId.apply(service) + " - serviceType: [" + serviceType + "]");
                                }
                                return matches;
                            } catch (Exception e) {
                                System.err.println("   ⚠️ Lỗi khi xử lý service: " + e.getMessage());
                                return false;
                            }
                        })
                        .map(getVehicleId)
                        .filter(id -> id != null && !id.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
                maintenanceVehicles = maintenanceVehicleIds.size();
                System.out.println("📊 Bảo dưỡng: " + maintenanceVehicles + " xe - " + maintenanceVehicleIds);
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi tính số xe bảo dưỡng: " + e.getMessage());
                e.printStackTrace();
                maintenanceVehicles = 0;
            }
            
            // Kiểm tra = số xe có serviceType chứa "Kiểm tra", "Inspection", "Check" (case-insensitive)
            // Từ DB: có "Kiểm tra" (tiếng Việt) và "Inspection" (tiếng Anh)
            long inspectionVehicles = 0;
            try {
                List<String> inspectionVehicleIds = allVehicleServices.stream()
                        .filter(service -> {
                            try {
                                String serviceType = getServiceTypeFunc.apply(service);
                                if (serviceType == null || serviceType.trim().isEmpty()) return false;
                                String st = serviceType.trim().toLowerCase();
                                // Match cả tiếng Việt và tiếng Anh
                                boolean matches = st.contains("kiểm tra") || 
                                       st.contains("inspection") || 
                                       st.contains("check") ||
                                       st.contains("kiểm định") ||
                                       st.equals("kiểm tra") ||
                                       st.equals("inspection") ||
                                       st.equals("check");
                                if (matches) {
                                    System.out.println("   ✓ Kiểm tra: " + getVehicleId.apply(service) + " - serviceType: [" + serviceType + "]");
                                }
                                return matches;
                            } catch (Exception e) {
                                System.err.println("   ⚠️ Lỗi khi xử lý service: " + e.getMessage());
                                return false;
                            }
                        })
                        .map(getVehicleId)
                        .filter(id -> id != null && !id.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
                inspectionVehicles = inspectionVehicleIds.size();
                System.out.println("📊 Kiểm tra: " + inspectionVehicles + " xe - " + inspectionVehicleIds);
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi tính số xe kiểm tra: " + e.getMessage());
                e.printStackTrace();
                inspectionVehicles = 0;
            }
            
            // Sửa chữa = số xe có serviceType chứa "Sửa chữa", "Repair", "Fix" (case-insensitive)
            // Từ DB: có "Sửa chữa" (tiếng Việt) và có thể có "Repair" (tiếng Anh)
            long brokenVehicles = 0;
            try {
                List<String> brokenVehicleIds = allVehicleServices.stream()
                        .filter(service -> {
                            try {
                                String serviceType = getServiceTypeFunc.apply(service);
                                if (serviceType == null || serviceType.trim().isEmpty()) return false;
                                String st = serviceType.trim().toLowerCase();
                                // Match cả tiếng Việt và tiếng Anh
                                boolean matches = st.contains("sửa chữa") || 
                                       st.contains("repair") || 
                                       st.contains("fix") ||
                                       st.equals("sửa chữa") ||
                                       st.equals("repair");
                                if (matches) {
                                    System.out.println("   ✓ Sửa chữa: " + getVehicleId.apply(service) + " - serviceType: [" + serviceType + "]");
                                }
                                return matches;
                            } catch (Exception e) {
                                System.err.println("   ⚠️ Lỗi khi xử lý service: " + e.getMessage());
                                return false;
                            }
                        })
                        .map(getVehicleId)
                        .filter(id -> id != null && !id.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
                brokenVehicles = brokenVehicleIds.size();
                System.out.println("📊 Sửa chữa: " + brokenVehicles + " xe - " + brokenVehicleIds);
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi tính số xe sửa chữa: " + e.getMessage());
                e.printStackTrace();
                brokenVehicles = 0;
            }
            
            // Nhóm TẤT CẢ dịch vụ theo vehicleId (không filter status)
            Map<String, List<Map<String, Object>>> allVehicleServicesMap = allVehicleServices.stream()
                    .collect(Collectors.groupingBy(getVehicleId));
            
            // Nhóm chỉ dịch vụ đang chờ (pending/in_progress) để hiển thị trong bảng
            Map<String, List<Map<String, Object>>> pendingVehicleServicesMap = allVehicleServices.stream()
                    .filter(service -> {
                        String status = (String) service.get("status");
                        return status != null && ("pending".equalsIgnoreCase(status) || 
                                "in_progress".equalsIgnoreCase(status) || 
                                "in progress".equalsIgnoreCase(status));
                    })
                    .collect(Collectors.groupingBy(getVehicleId));
            
            // Lấy TẤT CẢ vehicleId có trong bảng vehicleservice (không chỉ pending)
            Set<String> allVehicleIdsFromServices = allVehicleServicesMap.keySet().stream()
                    .filter(id -> id != null && !id.isEmpty())
                    .collect(Collectors.toSet());
            
            System.out.println("📋 Tất cả vehicleId từ bảng vehicleservice: " + allVehicleIdsFromServices);
            System.out.println("   - Số lượng: " + allVehicleIdsFromServices.size());
            
            // Map TẤT CẢ xe có trong bảng vehicleservice (không chỉ xe có dịch vụ đang chờ)
            List<Map<String, Object>> vehiclesWithServices = allVehicles.stream()
                    .filter(vehicle -> {
                        String vehicleId = vehicle.getVehicleId();
                        // Hiển thị xe nếu có trong bảng vehicleservice
                        boolean hasInServices = allVehicleIdsFromServices.contains(vehicleId);
                        if (!hasInServices) {
                            System.out.println("   ⚠️ Xe " + vehicleId + " không có trong bảng vehicleservice - sẽ không hiển thị");
                        }
                        return hasInServices;
                    })
                    .map(vehicle -> {
                        Map<String, Object> vehicleData = new HashMap<>();
                        vehicleData.put("vehicleId", vehicle.getVehicleId());
                        vehicleData.put("name", vehicle.getType() != null ? vehicle.getType() : vehicle.getVehicleId());
                        vehicleData.put("plateNumber", vehicle.getVehicleNumber());
                        vehicleData.put("category", vehicle.getType());
                        vehicleData.put("typeDetail", vehicle.getType());
                        vehicleData.put("iconClass", "icon-car");
                        
                        // Lấy chỉ dịch vụ ĐANG CHỜ (pending/in_progress) để hiển thị
                        List<Map<String, Object>> pendingServices = pendingVehicleServicesMap.getOrDefault(vehicle.getVehicleId(), new ArrayList<>());
                        List<Map<String, Object>> serviceViewData = pendingServices.stream()
                                .map(this::mapServiceToViewData)
                                .collect(Collectors.toList());
                        vehicleData.put("services", serviceViewData);
                        
                        // Lấy TẤT CẢ dịch vụ để xác định trạng thái tổng thể
                        List<Map<String, Object>> allServicesForVehicle = allVehicleServicesMap.getOrDefault(vehicle.getVehicleId(), new ArrayList<>());
                        
                        // Xác định trạng thái tổng thể (overallStatus)
                        String overallStatus = "complete"; // Mặc định là complete
                        if (allServicesForVehicle.isEmpty()) {
                            overallStatus = "complete"; // Không có dịch vụ = complete
                        } else {
                            // Kiểm tra xem có dịch vụ nào đang pending/in_progress không
                            boolean hasPending = allServicesForVehicle.stream()
                                    .anyMatch(s -> {
                                        String status = (String) s.get("status");
                                        if (status == null) return false;
                                        String sLower = status.toLowerCase();
                                        return sLower.contains("pending") || 
                                               sLower.contains("in_progress") || 
                                               sLower.contains("in progress");
                                    });
                            
                            boolean hasInProgress = allServicesForVehicle.stream()
                                    .anyMatch(s -> {
                                        String status = (String) s.get("status");
                                        if (status == null) return false;
                                        String sLower = status.toLowerCase();
                                        return sLower.contains("in_progress") || 
                                               sLower.contains("in progress");
                                    });
                            
                            if (hasInProgress) {
                                overallStatus = "in_progress";
                            } else if (hasPending) {
                                overallStatus = "pending";
                            } else {
                                // Tất cả đều completed
                                overallStatus = "complete";
                            }
                        }
                        vehicleData.put("overallStatus", overallStatus);
                        
                        // Lấy ngày yêu cầu gần nhất từ TẤT CẢ dịch vụ và format lại
                        String latestRequestDate = allServicesForVehicle.stream()
                                .map(s -> s.get("requestDate"))
                                .filter(d -> d != null)
                                .map(d -> {
                                    try {
                                        // Xử lý nhiều định dạng có thể có
                                        Instant instant = null;
                                        if (d instanceof Instant) {
                                            instant = (Instant) d;
                                        } else if (d instanceof String) {
                                            // Parse ISO string
                                            instant = Instant.parse((String) d);
                                        } else if (d instanceof java.sql.Timestamp) {
                                            instant = ((java.sql.Timestamp) d).toInstant();
                                        } else if (d instanceof LocalDateTime) {
                                            instant = ((LocalDateTime) d).atZone(ZoneId.systemDefault()).toInstant();
                                        }
                                        
                                        if (instant != null) {
                                            // Format: dd/MM/yyyy HH:mm:ss
                                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                                                    .withZone(ZoneId.systemDefault());
                                            return formatter.format(instant);
                                        }
                                    } catch (Exception e) {
                                        System.err.println("⚠️ Lỗi khi format date: " + d + " - " + e.getMessage());
                                    }
                                    return d.toString();
                                })
                                .filter(d -> d != null && !d.isEmpty())
                                .sorted((d1, d2) -> d2.compareTo(d1)) // Sort descending để lấy ngày mới nhất
                                .findFirst()
                                .orElse("N/A");
                        vehicleData.put("latestRequestDate", latestRequestDate);
                        vehicleData.put("formattedRequestDate", latestRequestDate); // Alias cho template
                        
                        // Xác định loại dịch vụ chính từ dịch vụ đang chờ
                        String mainServiceType = serviceViewData.stream()
                                .map(s -> (String) s.get("serviceType"))
                                .filter(t -> t != null)
                                .findFirst()
                                .orElse("");
                        vehicleData.put("mainServiceType", mainServiceType);
                        
                        return vehicleData;
                    })
                    .collect(Collectors.toList());
            
            System.out.println("✅ Đã map " + vehiclesWithServices.size() + " xe từ bảng vehicleservice");
            
            // Lọc theo search query và service filter
            List<Map<String, Object>> filteredVehicles = vehiclesWithServices.stream()
                    .filter(vehicle -> {
                        // Lọc theo search query
                        boolean matchesSearch = searchQuery.isEmpty() ||
                                (vehicle.get("plateNumber") != null && 
                                 vehicle.get("plateNumber").toString().toLowerCase().contains(searchQuery.toLowerCase())) ||
                                (vehicle.get("name") != null && 
                                 vehicle.get("name").toString().toLowerCase().contains(searchQuery.toLowerCase())) ||
                                (vehicle.get("vehicleId") != null && 
                                 vehicle.get("vehicleId").toString().toLowerCase().contains(searchQuery.toLowerCase()));
                        
                        // Lọc theo service filter
                        boolean matchesService = true;
                        if (!"all".equals(serviceFilter)) {
                            List<Map<String, Object>> services = (List<Map<String, Object>>) vehicle.get("services");
                            if ("ready".equals(serviceFilter)) {
                                // Sẵn sàng = không có dịch vụ đang chờ
                                matchesService = (services == null || services.isEmpty());
                            } else {
                                // Lọc theo loại dịch vụ (sử dụng getServiceTypeFunc helper - lấy từ cột service_type)
                                // Sử dụng getServiceTypeFromMap vì đây là nested lambda
                                matchesService = services != null && services.stream()
                                        .anyMatch(s -> {
                                            String serviceType = getServiceTypeFromMap(s);
                                            if (serviceType == null) return false;
                                            String st = serviceType.toLowerCase();
                                            return ("maintenance".equals(serviceFilter) && (st.contains("bảo dưỡng") || st.contains("maintenance"))) ||
                                                   ("inspection".equals(serviceFilter) && (st.contains("kiểm tra") || st.contains("inspection") || st.contains("check"))) ||
                                                   ("repair".equals(serviceFilter) && (st.contains("sửa chữa") || st.contains("repair") || st.contains("fix")));
                                        });
                            }
                        }
                        
                        return matchesSearch && matchesService;
                    })
                    .collect(Collectors.toList());
            
            
            // Không phân trang - hiển thị tất cả dữ liệu
            List<Map<String, Object>> pagedVehicles = filteredVehicles;
            
            // Đảm bảo stats luôn có giá trị (fallback về 0 nếu null)
            long finalTotalVehicles = totalVehicles >= 0 ? totalVehicles : 0;
            long finalMaintenanceVehicles = maintenanceVehicles >= 0 ? maintenanceVehicles : 0;
            long finalInspectionVehicles = inspectionVehicles >= 0 ? inspectionVehicles : 0;
            long finalBrokenVehicles = brokenVehicles >= 0 ? brokenVehicles : 0;
            
            System.out.println("📊 [STEP 3] Tính toán stats hoàn tất:");
            System.out.println("     * Tổng số xe: " + finalTotalVehicles + " (distinct vehicle_id từ bảng vehicleservice)");
            System.out.println("     * Bảo dưỡng: " + finalMaintenanceVehicles + " (xe có serviceType=Bảo dưỡng/Maintenance)");
            System.out.println("     * Kiểm tra: " + finalInspectionVehicles + " (xe có serviceType=Kiểm tra/Inspection)");
            System.out.println("     * Sửa chữa: " + finalBrokenVehicles + " (xe có serviceType=Sửa chữa/Repair)");
            
            // Thêm dữ liệu vào model
            System.out.println("📋 [STEP 4] Set model attributes...");
            model.addAttribute("vehicles", pagedVehicles != null ? pagedVehicles : List.of());
            model.addAttribute("totalVehicles", finalTotalVehicles);
            model.addAttribute("maintenanceVehicles", finalMaintenanceVehicles);
            model.addAttribute("inspectionVehicles", finalInspectionVehicles);
            model.addAttribute("brokenVehicles", finalBrokenVehicles);
            model.addAttribute("searchQuery", searchQuery != null ? searchQuery : "");
            model.addAttribute("serviceFilter", serviceFilter != null ? serviceFilter : "all");
            
            System.out.println("✅ [SUCCESS] Đã xử lý thành công!");
            System.out.println("   - Xe sau khi lọc: " + filteredVehicles.size());
            System.out.println("   - Hiển thị TẤT CẢ: " + (pagedVehicles != null ? pagedVehicles.size() : 0) + " xe");
            System.out.println("   - Model attributes đã set:");
            System.out.println("     * vehicles: " + (pagedVehicles != null ? pagedVehicles.size() : 0) + " items");
            System.out.println("     * totalVehicles: " + finalTotalVehicles);
            System.out.println("     * maintenanceVehicles: " + finalMaintenanceVehicles);
            System.out.println("     * inspectionVehicles: " + finalInspectionVehicles);
            System.out.println("     * brokenVehicles: " + finalBrokenVehicles);
            System.out.println("═══════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            System.err.println("═══════════════════════════════════════════════════════");
            System.err.println("❌ LỖI NGHIÊM TRỌNG khi load dữ liệu cho trang quản lý xe!");
            System.err.println("   Error Type: " + e.getClass().getName());
            System.err.println("   Error Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("   Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            System.err.println("═══════════════════════════════════════════════════════");
            
            // Set giá trị mặc định để đảm bảo trang vẫn load được
            model.addAttribute("vehicles", List.of());
            model.addAttribute("totalVehicles", 0L);
            model.addAttribute("maintenanceVehicles", 0L);
            model.addAttribute("inspectionVehicles", 0L);
            model.addAttribute("brokenVehicles", 0L);
            model.addAttribute("searchQuery", "");
            model.addAttribute("serviceFilter", "all");
            model.addAttribute("errorMessage", "Không thể tải dữ liệu từ database. Vui lòng thử lại sau. Chi tiết: " + e.getMessage());
        }
        
        return "admin/vehicle-manager";
    }
    
    /**
     * Test endpoint để kiểm tra API và dữ liệu
     */
    @GetMapping("/admin/vehicle-manager/test")
    @ResponseBody
    public Map<String, Object> testVehicleManager() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test lấy vehicles
            List<VehicleDTO> allVehicles = vehicleRestClient.getAllVehicles();
            result.put("vehiclesCount", allVehicles != null ? allVehicles.size() : 0);
            result.put("vehicles", allVehicles != null ? allVehicles : List.of());
            
            // Test lấy vehicle services
            List<Map<String, Object>> allVehicleServices = vehicleServiceRestClient.getAllVehicleServices();
            result.put("vehicleServicesCount", allVehicleServices != null ? allVehicleServices.size() : 0);
            
            if (allVehicleServices != null && !allVehicleServices.isEmpty()) {
                Map<String, Object> firstService = allVehicleServices.get(0);
                result.put("firstService", firstService);
                result.put("firstServiceKeys", firstService.keySet());
                
                // Test extract vehicleId
                Object idObj = firstService.get("id");
                if (idObj instanceof Map) {
                    Map<String, Object> idMap = (Map<String, Object>) idObj;
                    result.put("firstServiceVehicleId", idMap.get("vehicleId"));
                    result.put("firstServiceServiceId", idMap.get("serviceId"));
                }
                
                // Test extract serviceType
                result.put("firstServiceServiceType", firstService.get("serviceType"));
                result.put("firstServiceStatus", firstService.get("status"));
            }
            
            result.put("status", "success");
            result.put("message", "API test thành công");
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "API test thất bại: " + e.getMessage());
            result.put("error", e.getClass().getName());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Helper method để lấy serviceType từ service (cột service_type trong bảng vehicleservice)
     */
    private String getServiceTypeFromMap(Map<String, Object> service) {
        // Thử lấy trực tiếp từ field serviceType (camelCase - Jackson default)
        String serviceType = (String) service.get("serviceType");
        if (serviceType != null && !serviceType.isEmpty()) {
            return serviceType;
        }
        // Thử lấy từ service_type (snake_case - nếu có custom naming)
        serviceType = (String) service.get("service_type");
        if (serviceType != null && !serviceType.isEmpty()) {
            return serviceType;
        }
        // Thử lấy từ nested service object (nếu có)
        Object serviceObj = service.get("service");
        if (serviceObj instanceof Map) {
            Map<String, Object> serviceMap = (Map<String, Object>) serviceObj;
            serviceType = (String) serviceMap.get("serviceType");
            if (serviceType != null && !serviceType.isEmpty()) {
                return serviceType;
            }
        }
        return null;
    }
    
    /**
     * Map service sang format cho view
     */
    private Map<String, Object> mapServiceToViewData(Map<String, Object> service) {
        Map<String, Object> viewData = new HashMap<>();
        
        viewData.put("serviceName", service.get("serviceName"));
        // Lấy serviceType từ cột service_type trong bảng vehicleservice
        String serviceType = getServiceTypeFromMap(service);
        viewData.put("serviceType", serviceType);
        viewData.put("status", service.get("status"));
        viewData.put("serviceDescription", service.get("serviceDescription"));
        
        // Map status sang CSS class
        String status = (String) service.get("status");
        String statusClass = "pending";
        if (status != null) {
            String s = status.toLowerCase();
            if (s.contains("pending")) {
                statusClass = "pending";
            } else if (s.contains("in_progress") || s.contains("in progress")) {
                statusClass = "in-progress";
            } else if (s.contains("completed")) {
                statusClass = "completed";
            }
        }
        viewData.put("statusClass", statusClass);
        
        // Format request date
        Object requestDateObj = service.get("requestDate");
        String requestDate = "N/A";
        if (requestDateObj != null) {
            requestDate = requestDateObj.toString();
            // Có thể format lại date nếu cần
        }
        viewData.put("requestDate", requestDate);
        
        return viewData;
    }
    

    
    @GetMapping("/admin/enhanced-contract")
    public String EnhancedContractManagement(Model model) {
        model.addAttribute("pageTitle", "Quản Lý Hợp Đồng Điện Tử");
        model.addAttribute("pageDescription", "Quản lý hợp đồng pháp lý cho nhóm đồng sở hữu");
        return "admin/enhanced-contract-management";
    }

    /**
     * API endpoint để lấy danh sách dịch vụ của một xe
     * @param vehicleId ID của xe
     * @return JSON response với danh sách dịch vụ
     */
    @GetMapping("/admin/vehicle-manager/api/vehicle/{vehicleId}/services")
    @ResponseBody
    public Map<String, Object> getVehicleServices(@PathVariable String vehicleId) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("📡 [API] Lấy danh sách dịch vụ cho xe: " + vehicleId);
            
            List<Map<String, Object>> services = vehicleServiceRestClient.getVehicleServicesByVehicleId(vehicleId);
            
            response.put("success", true);
            response.put("services", services);
            response.put("count", services.size());
            
            System.out.println("✅ [API] Đã lấy được " + services.size() + " dịch vụ cho xe " + vehicleId);
            
            return response;
        } catch (Exception e) {
            System.err.println("❌ [API] Lỗi khi lấy danh sách dịch vụ: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi khi lấy danh sách dịch vụ: " + e.getMessage());
            response.put("services", new ArrayList<>());
            return response;
        }
    }

    /**
     * API endpoint để cập nhật trạng thái dịch vụ
     * @param serviceId ID của dịch vụ
     * @param vehicleId ID của xe
     * @param requestBody Request body chứa status
     * @return JSON response với kết quả cập nhật
     */
    @PutMapping("/admin/vehicle-manager/api/service/{serviceId}/vehicle/{vehicleId}/status")
    @ResponseBody
    public Map<String, Object> updateServiceStatus(
            @PathVariable String serviceId,
            @PathVariable String vehicleId,
            @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("📡 [API] Cập nhật trạng thái dịch vụ:");
            System.out.println("   - serviceId: " + serviceId);
            System.out.println("   - vehicleId: " + vehicleId);
            System.out.println("   - status: " + requestBody.get("status"));
            
            String status = (String) requestBody.get("status");
            if (status == null || status.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Trạng thái không được để trống");
                return response;
            }
            
            Map<String, Object> updatedService = vehicleServiceRestClient.updateServiceStatus(serviceId, vehicleId, status);
            
            response.put("success", true);
            response.put("message", "Cập nhật trạng thái thành công");
            response.put("service", updatedService);
            
            System.out.println("✅ [API] Đã cập nhật trạng thái thành công");
            
            return response;
        } catch (Exception e) {
            System.err.println("❌ [API] Lỗi khi cập nhật trạng thái: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi khi cập nhật trạng thái: " + e.getMessage());
            return response;
        }
    }

    /**
     * Trang đăng ký dịch vụ xe cho khách hàng - GET
     * Load dữ liệu từ database: 
     * - Danh sách xe từ bảng vehicle
     * - Danh sách loại dịch vụ từ cột service_type trong bảng service
     * - Danh sách dịch vụ từ cột service_name trong bảng service
     */
    @GetMapping("/user/service-registration")
    public String serviceRegistration(Model model) {
        try {
            model.addAttribute("pageTitle", "Đăng Ký Dịch Vụ Xe");
            model.addAttribute("pageDescription", "Đăng ký dịch vụ bảo dưỡng, sửa chữa và các dịch vụ khác");
            
            // Load danh sách xe từ bảng vehicle trong database
            // Gọi API: GET http://localhost:8083/api/vehicles
            List<VehicleDTO> vehicles = vehicleRestClient.getAllVehicles();
            model.addAttribute("vehicles", vehicles);
            System.out.println("✅ Đã load " + vehicles.size() + " xe từ bảng vehicle");
            
            // Load danh sách loại dịch vụ từ cột service_type trong bảng service
            // Gọi API: GET http://localhost:8083/api/services/types
            List<String> serviceTypes = serviceRestClient.getServiceTypes();
            model.addAttribute("serviceTypes", serviceTypes);
            System.out.println("✅ Đã load " + serviceTypes.size() + " loại dịch vụ từ cột service_type");
            
            // Load danh sách dịch vụ từ bảng service trong database
            // Gọi API: GET http://localhost:8083/api/services
            List<ServiceDTO> services = serviceRestClient.getAllServices();
            model.addAttribute("services", services);
            System.out.println("✅ Đã load " + services.size() + " dịch vụ từ cột service_name");
            
            // Log chi tiết để debug
            if (vehicles.size() > 0) {
                System.out.println("Xe đầu tiên: " + vehicles.get(0).getVehicleId() + " - " + vehicles.get(0).getVehicleNumber());
            }
            if (serviceTypes.size() > 0) {
                System.out.println("Loại dịch vụ: " + serviceTypes);
            }
            if (services.size() > 0) {
                System.out.println("Dịch vụ đầu tiên: " + services.get(0).getServiceId() + " - " + services.get(0).getServiceName() + " (" + services.get(0).getServiceType() + ")");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi load dữ liệu cho trang đăng ký dịch vụ: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("vehicles", List.of());
            model.addAttribute("serviceTypes", List.of());
            model.addAttribute("services", List.of());
            model.addAttribute("errorMessage", "Không thể tải dữ liệu từ database. Vui lòng thử lại sau.");
        }
        return "user/service-registration";
    }

    /**
     * Xử lý đăng ký dịch vụ - POST
     */
    @PostMapping("/user/service-registration")
    public String registerService(
            @RequestParam String vehicleId,
            @RequestParam String serviceId,
            @RequestParam String serviceType,
            @RequestParam(required = false) String serviceDescription,
            RedirectAttributes redirectAttributes) {
        try {
            // Tạo request data
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("vehicleId", vehicleId);
            requestData.put("serviceId", serviceId);
            requestData.put("serviceType", serviceType);
            if (serviceDescription != null && !serviceDescription.trim().isEmpty()) {
                requestData.put("serviceDescription", serviceDescription);
            }
            requestData.put("status", "pending");

            // Gọi API để đăng ký dịch vụ
            Map<String, Object> result = vehicleServiceRestClient.registerVehicleService(requestData);
            
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký dịch vụ thành công! Chúng tôi sẽ xử lý yêu cầu của bạn sớm nhất.");
            return "redirect:/user/service-registration?success=true";
        } catch (Exception e) {
            System.err.println("Lỗi khi đăng ký dịch vụ: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi đăng ký dịch vụ: " + e.getMessage());
            return "redirect:/user/service-registration?error=true";
        }
    }
}
