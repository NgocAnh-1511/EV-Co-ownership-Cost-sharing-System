package com.example.ui_service.controller;

import com.example.ui_service.model.VehicleDTO;
import com.example.ui_service.service.VehicleRestClient;
import com.example.ui_service.service.VehicleServiceRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
public class VehicleServiceController {

    @Autowired
    private VehicleRestClient vehicleRestClient;

    @Autowired
    private VehicleServiceRestClient vehicleServiceRestClient;
    
    @Autowired
    private com.example.ui_service.service.ServiceRestClient serviceRestClient;

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
        System.out.println("🚀 [VEHICLE SERVICE CONTROLLER] Bắt đầu load trang /admin/vehicle-manager");
        System.out.println("   - searchQuery: " + searchQuery);
        System.out.println("   - serviceFilter: " + serviceFilter);
        System.out.println("   - Hiển thị TẤT CẢ dữ liệu (không phân trang)");
        
        try {
            model.addAttribute("pageTitle", "Quản Lý Các Dịch Vụ Xe");
            model.addAttribute("pageDescription", "Quản lý dịch vụ bảo dưỡng, kiểm tra và sửa chữa cho xe");
            
            // Lấy danh sách tất cả xe từ API (để hiển thị thông tin xe)
            System.out.println("📡 [STEP 1] Gọi API để lấy danh sách xe...");
            List<VehicleDTO> vehiclesFromAPI = vehicleRestClient.getAllVehicles();
            System.out.println("✅ [STEP 1] Đã lấy " + (vehiclesFromAPI != null ? vehiclesFromAPI.size() : 0) + " xe từ API");
            final List<VehicleDTO> allVehicles = vehiclesFromAPI != null ? vehiclesFromAPI : new ArrayList<>();
            
            // Lấy danh sách tất cả dịch vụ xe từ API
            System.out.println("📡 [STEP 2] Gọi API để lấy danh sách dịch vụ xe...");
            List<Map<String, Object>> servicesFromAPI = vehicleServiceRestClient.getAllVehicleServices();
            System.out.println("✅ [STEP 2] Đã lấy " + (servicesFromAPI != null ? servicesFromAPI.size() : 0) + " dịch vụ từ API");
            
            // Kiểm tra nếu không có dữ liệu
            if (servicesFromAPI == null || servicesFromAPI.isEmpty()) {
                System.out.println("⚠️ WARNING: Không có dữ liệu từ API vehicleservices!");
                System.out.println("   - Kiểm tra xem API có đang chạy không: http://localhost:8083/api/vehicleservices");
                System.out.println("   - Kiểm tra xem có dữ liệu trong bảng vehicleservice không");
            }
            final List<Map<String, Object>> allVehicleServices = servicesFromAPI != null && !servicesFromAPI.isEmpty() 
                    ? servicesFromAPI : new ArrayList<>();
            
            // Helper method để lấy vehicleId từ service
            final java.util.function.Function<Map<String, Object>, String> getVehicleId = service -> {
                String vehicleId = null;
                
                // Ưu tiên 1: Lấy trực tiếp từ root (id giờ là Integer, không còn composite key)
                vehicleId = (String) service.get("vehicleId");
                if (vehicleId != null && !vehicleId.trim().isEmpty()) {
                    return vehicleId.trim();
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
                
                // Fallback: Kiểm tra xem id có phải là Map không (tương thích ngược)
                Object idObj = service.get("id");
                if (idObj instanceof Map) {
                    Map<String, Object> idMap = (Map<String, Object>) idObj;
                    vehicleId = (String) idMap.get("vehicleId");
                    if (vehicleId != null && !vehicleId.trim().isEmpty()) {
                        return vehicleId.trim();
                    }
                }
                
                return "";
            };
            
            // Helper method để lấy serviceType từ service
            final java.util.function.Function<Map<String, Object>, String> getServiceTypeFunc = service -> {
                String serviceType = (String) service.get("serviceType");
                if (serviceType != null && !serviceType.trim().isEmpty()) {
                    return serviceType.trim();
                }
                
                serviceType = (String) service.get("service_type");
                if (serviceType != null && !serviceType.trim().isEmpty()) {
                    return serviceType.trim();
                }
                
                Object serviceObj = service.get("service");
                if (serviceObj instanceof Map) {
                    Map<String, Object> serviceMap = (Map<String, Object>) serviceObj;
                    serviceType = (String) serviceMap.get("serviceType");
                    if (serviceType != null && !serviceType.trim().isEmpty()) {
                        return serviceType.trim();
                    }
                }
                
                return null;
            };
            
            // Tính toán thống kê từ bảng vehicleservice
            // Tổng số xe cần xử lý (chỉ tính những xe có dịch vụ pending hoặc in_progress)
            long totalVehicles = 0;
            try {
                List<String> vehiclesNeedingProcessing = allVehicleServices.stream()
                        .filter(service -> {
                            try {
                                // Chỉ tính những dịch vụ có status pending hoặc in_progress
                                String status = (String) service.get("status");
                                if (status == null) return false;
                                String statusLower = status.toLowerCase().trim();
                                return statusLower.equals("pending") || 
                                       statusLower.equals("in_progress") || 
                                       statusLower.equals("in progress");
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .map(getVehicleId)
                        .filter(id -> id != null && !id.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
                totalVehicles = vehiclesNeedingProcessing.size();
                System.out.println("📊 Tổng số xe cần xử lý (pending/in_progress): " + totalVehicles);
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi tính tổng số xe cần xử lý: " + e.getMessage());
                totalVehicles = 0;
            }
            
            // Bảo dưỡng - chỉ tính pending hoặc in_progress
            long maintenanceVehicles = 0;
            try {
                List<String> maintenanceVehicleIds = allVehicleServices.stream()
                        .filter(service -> {
                            try {
                                // Kiểm tra status - chỉ tính pending hoặc in_progress
                                String status = (String) service.get("status");
                                if (status == null) return false;
                                String statusLower = status.toLowerCase().trim();
                                boolean isPendingOrInProgress = statusLower.equals("pending") || 
                                                               statusLower.equals("in_progress") || 
                                                               statusLower.equals("in progress");
                                if (!isPendingOrInProgress) return false;
                                
                                // Kiểm tra serviceType - bảo dưỡng
                                String serviceType = getServiceTypeFunc.apply(service);
                                if (serviceType == null || serviceType.trim().isEmpty()) return false;
                                String st = serviceType.trim().toLowerCase();
                                return st.contains("bảo dưỡng") || st.contains("maintenance") ||
                                       st.equals("bảo dưỡng") || st.equals("maintenance");
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .map(getVehicleId)
                        .filter(id -> id != null && !id.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
                maintenanceVehicles = maintenanceVehicleIds.size();
                System.out.println("📊 Số xe bảo dưỡng (pending/in_progress): " + maintenanceVehicles);
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi tính số xe bảo dưỡng: " + e.getMessage());
                maintenanceVehicles = 0;
            }
            
            // Kiểm tra - chỉ tính pending hoặc in_progress
            long inspectionVehicles = 0;
            try {
                List<String> inspectionVehicleIds = allVehicleServices.stream()
                        .filter(service -> {
                            try {
                                // Kiểm tra status - chỉ tính pending hoặc in_progress
                                String status = (String) service.get("status");
                                if (status == null) return false;
                                String statusLower = status.toLowerCase().trim();
                                boolean isPendingOrInProgress = statusLower.equals("pending") || 
                                                               statusLower.equals("in_progress") || 
                                                               statusLower.equals("in progress");
                                if (!isPendingOrInProgress) return false;
                                
                                // Kiểm tra serviceType - kiểm tra
                                String serviceType = getServiceTypeFunc.apply(service);
                                if (serviceType == null || serviceType.trim().isEmpty()) return false;
                                String st = serviceType.trim().toLowerCase();
                                return st.contains("kiểm tra") || st.contains("inspection") || 
                                       st.contains("check") || st.contains("kiểm định") ||
                                       st.equals("kiểm tra") || st.equals("inspection") || st.equals("check");
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .map(getVehicleId)
                        .filter(id -> id != null && !id.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
                inspectionVehicles = inspectionVehicleIds.size();
                System.out.println("📊 Số xe kiểm tra (pending/in_progress): " + inspectionVehicles);
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi tính số xe kiểm tra: " + e.getMessage());
                inspectionVehicles = 0;
            }
            
            // Sửa chữa - chỉ tính pending hoặc in_progress
            long brokenVehicles = 0;
            try {
                List<String> brokenVehicleIds = allVehicleServices.stream()
                        .filter(service -> {
                            try {
                                // Kiểm tra status - chỉ tính pending hoặc in_progress
                                String status = (String) service.get("status");
                                if (status == null) return false;
                                String statusLower = status.toLowerCase().trim();
                                boolean isPendingOrInProgress = statusLower.equals("pending") || 
                                                               statusLower.equals("in_progress") || 
                                                               statusLower.equals("in progress");
                                if (!isPendingOrInProgress) return false;
                                
                                // Kiểm tra serviceType - sửa chữa
                                String serviceType = getServiceTypeFunc.apply(service);
                                if (serviceType == null || serviceType.trim().isEmpty()) return false;
                                String st = serviceType.trim().toLowerCase();
                                return st.contains("sửa chữa") || st.contains("repair") || 
                                       st.contains("fix") || st.equals("sửa chữa") || st.equals("repair");
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .map(getVehicleId)
                        .filter(id -> id != null && !id.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
                brokenVehicles = brokenVehicleIds.size();
                System.out.println("📊 Số xe sửa chữa (pending/in_progress): " + brokenVehicles);
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi tính số xe sửa chữa: " + e.getMessage());
                brokenVehicles = 0;
            }
            
            // Nhóm TẤT CẢ dịch vụ theo vehicleId
            Map<String, List<Map<String, Object>>> allVehicleServicesMap = allVehicleServices.stream()
                    .collect(Collectors.groupingBy(getVehicleId));
            
            // Nhóm chỉ dịch vụ đang chờ (pending/in_progress)
            Map<String, List<Map<String, Object>>> pendingVehicleServicesMap = allVehicleServices.stream()
                    .filter(service -> {
                        String status = (String) service.get("status");
                        return status != null && ("pending".equalsIgnoreCase(status) || 
                                "in_progress".equalsIgnoreCase(status) || 
                                "in progress".equalsIgnoreCase(status));
                    })
                    .collect(Collectors.groupingBy(getVehicleId));
            
            // Lấy TẤT CẢ vehicleId có trong bảng vehicleservice
            Set<String> allVehicleIdsFromServices = allVehicleServicesMap.keySet().stream()
                    .filter(id -> id != null && !id.isEmpty())
                    .collect(Collectors.toSet());
            
            // Map TẤT CẢ xe có trong bảng vehicleservice
            List<Map<String, Object>> vehiclesWithServices = allVehicles.stream()
                    .filter(vehicle -> allVehicleIdsFromServices.contains(vehicle.getVehicleId()))
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
                        String overallStatus = "complete";
                        if (!allServicesForVehicle.isEmpty()) {
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
                                        Instant instant = null;
                                        if (d instanceof Instant) {
                                            instant = (Instant) d;
                                        } else if (d instanceof String) {
                                            instant = Instant.parse((String) d);
                                        } else if (d instanceof java.sql.Timestamp) {
                                            instant = ((java.sql.Timestamp) d).toInstant();
                                        } else if (d instanceof LocalDateTime) {
                                            instant = ((LocalDateTime) d).atZone(ZoneId.systemDefault()).toInstant();
                                        }
                                        
                                        if (instant != null) {
                                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                                                    .withZone(ZoneId.systemDefault());
                                            return formatter.format(instant);
                                        }
                                    } catch (Exception e) {
                                        System.err.println("⚠️ Lỗi khi format date: " + d);
                                    }
                                    return d.toString();
                                })
                                .filter(d -> d != null && !d.isEmpty())
                                .sorted((d1, d2) -> d2.compareTo(d1))
                                .findFirst()
                                .orElse("N/A");
                        vehicleData.put("latestRequestDate", latestRequestDate);
                        vehicleData.put("formattedRequestDate", latestRequestDate);
                        
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
            
            // Lọc theo search query, service filter và loại bỏ những xe có trạng thái "complete"
            List<Map<String, Object>> filteredVehicles = vehiclesWithServices.stream()
                    .filter(vehicle -> {
                        // Loại bỏ những xe có trạng thái "complete"
                        String overallStatus = (String) vehicle.get("overallStatus");
                        if (overallStatus != null && "complete".equalsIgnoreCase(overallStatus)) {
                            return false;
                        }
                        
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
                                matchesService = (services == null || services.isEmpty());
                            } else {
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
            
            // Lấy danh sách dịch vụ đã hoàn thành (lịch sử) từ bảng vehicleservice
            List<Map<String, Object>> completedServices = allVehicleServices.stream()
                    .filter(service -> {
                        String status = (String) service.get("status");
                        return status != null && ("completed".equalsIgnoreCase(status) || 
                                "complete".equalsIgnoreCase(status));
                    })
                    .map(service -> {
                        Map<String, Object> serviceHistory = new HashMap<>();
                        String vehicleId = getVehicleId.apply(service);
                        String serviceId = null;
                        
                        // Lấy serviceId từ composite key
                        Object idObj = service.get("id");
                        if (idObj instanceof Map) {
                            Map<String, Object> idMap = (Map<String, Object>) idObj;
                            serviceId = (String) idMap.get("serviceId");
                        }
                        if (serviceId == null) {
                            serviceId = (String) service.get("serviceId");
                        }
                        
                        // Tìm thông tin xe
                        VehicleDTO vehicle = allVehicles.stream()
                                .filter(v -> v.getVehicleId().equals(vehicleId))
                                .findFirst()
                                .orElse(null);
                        
                        serviceHistory.put("serviceId", serviceId);
                        serviceHistory.put("vehicleId", vehicleId);
                        serviceHistory.put("serviceName", service.get("serviceName"));
                        serviceHistory.put("serviceType", getServiceTypeFunc.apply(service));
                        serviceHistory.put("serviceDescription", service.get("serviceDescription"));
                        serviceHistory.put("status", service.get("status"));
                        
                        // Format requestDate
                        Object requestDateObj = service.get("requestDate");
                        String formattedRequestDate = "N/A";
                        if (requestDateObj != null) {
                            try {
                                Instant instant = null;
                                if (requestDateObj instanceof Instant) {
                                    instant = (Instant) requestDateObj;
                                } else if (requestDateObj instanceof String) {
                                    instant = Instant.parse((String) requestDateObj);
                                } else if (requestDateObj instanceof java.sql.Timestamp) {
                                    instant = ((java.sql.Timestamp) requestDateObj).toInstant();
                                } else if (requestDateObj instanceof LocalDateTime) {
                                    instant = ((LocalDateTime) requestDateObj).atZone(ZoneId.systemDefault()).toInstant();
                                }
                                
                                if (instant != null) {
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                                            .withZone(ZoneId.systemDefault());
                                    formattedRequestDate = formatter.format(instant);
                                }
                            } catch (Exception e) {
                                System.err.println("⚠️ Lỗi khi format requestDate: " + requestDateObj);
                            }
                        }
                        serviceHistory.put("requestDate", service.get("requestDate")); // Giữ nguyên để sort
                        serviceHistory.put("formattedRequestDate", formattedRequestDate);
                        
                        // Format completionDate
                        Object completionDateObj = service.get("completionDate");
                        String formattedCompletionDate = "N/A";
                        if (completionDateObj != null) {
                            try {
                                Instant instant = null;
                                if (completionDateObj instanceof Instant) {
                                    instant = (Instant) completionDateObj;
                                } else if (completionDateObj instanceof String) {
                                    instant = Instant.parse((String) completionDateObj);
                                } else if (completionDateObj instanceof java.sql.Timestamp) {
                                    instant = ((java.sql.Timestamp) completionDateObj).toInstant();
                                } else if (completionDateObj instanceof LocalDateTime) {
                                    instant = ((LocalDateTime) completionDateObj).atZone(ZoneId.systemDefault()).toInstant();
                                }
                                
                                if (instant != null) {
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                                            .withZone(ZoneId.systemDefault());
                                    formattedCompletionDate = formatter.format(instant);
                                }
                            } catch (Exception e) {
                                System.err.println("⚠️ Lỗi khi format completionDate: " + completionDateObj);
                            }
                        }
                        serviceHistory.put("completionDate", service.get("completionDate")); // Giữ nguyên để sort
                        serviceHistory.put("formattedCompletionDate", formattedCompletionDate);
                        
                        // Thông tin xe
                        if (vehicle != null) {
                            serviceHistory.put("vehicleName", vehicle.getName() != null ? vehicle.getName() : vehicle.getVehicleId());
                            serviceHistory.put("vehicleNumber", vehicle.getVehicleNumber());
                            serviceHistory.put("vehicleType", vehicle.getType());
                        } else {
                            serviceHistory.put("vehicleName", vehicleId);
                            serviceHistory.put("vehicleNumber", "-");
                            serviceHistory.put("vehicleType", "-");
                        }
                        
                        return serviceHistory;
                    })
                    .collect(Collectors.toList());
            
            // Sắp xếp lịch sử theo ngày hoàn thành (mới nhất trước)
            completedServices.sort((s1, s2) -> {
                Object d1 = s1.get("completionDate");
                Object d2 = s2.get("completionDate");
                if (d1 == null && d2 == null) return 0;
                if (d1 == null) return 1;
                if (d2 == null) return -1;
                
                try {
                    java.time.Instant instant1 = null;
                    java.time.Instant instant2 = null;
                    
                    if (d1 instanceof java.time.Instant) {
                        instant1 = (java.time.Instant) d1;
                    } else if (d1 instanceof String) {
                        instant1 = java.time.Instant.parse((String) d1);
                    }
                    
                    if (d2 instanceof java.time.Instant) {
                        instant2 = (java.time.Instant) d2;
                    } else if (d2 instanceof String) {
                        instant2 = java.time.Instant.parse((String) d2);
                    }
                    
                    if (instant1 != null && instant2 != null) {
                        return instant2.compareTo(instant1); // Mới nhất trước
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
                return 0;
            });
            
            // Đảm bảo stats luôn có giá trị
            long finalTotalVehicles = totalVehicles >= 0 ? totalVehicles : 0;
            long finalMaintenanceVehicles = maintenanceVehicles >= 0 ? maintenanceVehicles : 0;
            long finalInspectionVehicles = inspectionVehicles >= 0 ? inspectionVehicles : 0;
            long finalBrokenVehicles = brokenVehicles >= 0 ? brokenVehicles : 0;
            
            // Thêm dữ liệu vào model
            model.addAttribute("vehicles", pagedVehicles != null ? pagedVehicles : List.of());
            model.addAttribute("totalVehicles", finalTotalVehicles);
            model.addAttribute("maintenanceVehicles", finalMaintenanceVehicles);
            model.addAttribute("inspectionVehicles", finalInspectionVehicles);
            model.addAttribute("brokenVehicles", finalBrokenVehicles);
            model.addAttribute("searchQuery", searchQuery != null ? searchQuery : "");
            model.addAttribute("serviceFilter", serviceFilter != null ? serviceFilter : "all");
            model.addAttribute("completedServices", completedServices != null ? completedServices : List.of());
            
            System.out.println("✅ [SUCCESS] Đã xử lý thành công!");
            System.out.println("═══════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            System.err.println("═══════════════════════════════════════════════════════");
            System.err.println("❌ LỖI NGHIÊM TRỌNG khi load dữ liệu cho trang quản lý xe!");
            System.err.println("   Error Type: " + e.getClass().getName());
            System.err.println("   Error Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("═══════════════════════════════════════════════════════");
            
            // Set giá trị mặc định
            model.addAttribute("vehicles", List.of());
            model.addAttribute("totalVehicles", 0L);
            model.addAttribute("maintenanceVehicles", 0L);
            model.addAttribute("inspectionVehicles", 0L);
            model.addAttribute("brokenVehicles", 0L);
            model.addAttribute("searchQuery", "");
            model.addAttribute("serviceFilter", "all");
            model.addAttribute("completedServices", List.of());
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
            List<VehicleDTO> allVehicles = vehicleRestClient.getAllVehicles();
            result.put("vehiclesCount", allVehicles != null ? allVehicles.size() : 0);
            result.put("vehicles", allVehicles != null ? allVehicles : List.of());
            
            List<Map<String, Object>> allVehicleServices = vehicleServiceRestClient.getAllVehicleServices();
            result.put("vehicleServicesCount", allVehicleServices != null ? allVehicleServices.size() : 0);
            
            if (allVehicleServices != null && !allVehicleServices.isEmpty()) {
                Map<String, Object> firstService = allVehicleServices.get(0);
                result.put("firstService", firstService);
                result.put("firstServiceKeys", firstService.keySet());
                
                Object idObj = firstService.get("id");
                if (idObj instanceof Map) {
                    Map<String, Object> idMap = (Map<String, Object>) idObj;
                    result.put("firstServiceVehicleId", idMap.get("vehicleId"));
                    result.put("firstServiceServiceId", idMap.get("serviceId"));
                }
                
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
     * API endpoint để lấy danh sách dịch vụ của một xe
     * Lấy TẤT CẢ dịch vụ từ bảng vehicleservice (bao gồm cả completed) để hiển thị:
     * - Dịch vụ đang chờ: pending, in_progress
     * - Lịch sử dịch vụ: completed (lọc từ bảng vehicleservice với status = 'completed')
     * 
     * @param vehicleId ID của xe
     * @return JSON response với danh sách dịch vụ (tất cả status)
     */
    @GetMapping("/admin/vehicle-manager/api/vehicle/{vehicleId}/services")
    @ResponseBody
    public Map<String, Object> getVehicleServices(@PathVariable String vehicleId) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("📡 [API] Lấy danh sách dịch vụ cho xe: " + vehicleId);
            System.out.println("   - Lấy TẤT CẢ dịch vụ từ bảng vehicleservice (bao gồm cả completed)");
            
            // Lấy TẤT CẢ dịch vụ từ bảng vehicleservice (từ backend API)
            // Backend API sẽ query: SELECT * FROM vehicleservice WHERE vehicleId = ?
            // Bao gồm cả các dịch vụ có status = 'completed' (lịch sử)
            List<Map<String, Object>> services = vehicleServiceRestClient.getVehicleServicesByVehicleId(vehicleId);
            
            // Đếm số lượng dịch vụ theo từng trạng thái để log
            long pendingCount = services.stream()
                    .filter(s -> {
                        String status = (String) s.get("status");
                        return status != null && ("pending".equalsIgnoreCase(status) || 
                                "in_progress".equalsIgnoreCase(status) || 
                                "in progress".equalsIgnoreCase(status));
                    })
                    .count();
            long completedCount = services.stream()
                    .filter(s -> {
                        String status = (String) s.get("status");
                        return status != null && ("completed".equalsIgnoreCase(status) || 
                                "complete".equalsIgnoreCase(status));
                    })
                    .count();
            
            System.out.println("   - Tổng số dịch vụ: " + services.size());
            System.out.println("   - Dịch vụ đang chờ (pending/in_progress): " + pendingCount);
            System.out.println("   - Lịch sử dịch vụ (completed): " + completedCount);
            
            response.put("success", true);
            response.put("services", services); // Trả về TẤT CẢ dịch vụ, client sẽ phân tách
            response.put("count", services.size());
            response.put("pendingCount", pendingCount);
            response.put("completedCount", completedCount);
            
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
     * API endpoint để cập nhật trạng thái dịch vụ theo id
     * @param id ID của đăng ký dịch vụ
     * @param requestBody Request body chứa status
     * @return JSON response với kết quả cập nhật
     */
    @PutMapping("/admin/vehicle-manager/api/service/{id}/status")
    @ResponseBody
    public Map<String, Object> updateServiceStatusById(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("📡 [API] Cập nhật trạng thái dịch vụ theo id:");
            System.out.println("   - id: " + id);
            System.out.println("   - status: " + requestBody.get("status"));
            
            String status = (String) requestBody.get("status");
            if (status == null || status.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Trạng thái không được để trống");
                return response;
            }
            
            Map<String, Object> updatedService = vehicleServiceRestClient.updateServiceStatusById(id, status);
            
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
     * API endpoint để cập nhật trạng thái dịch vụ (theo serviceId và vehicleId - tương thích ngược)
     * @param serviceId ID của dịch vụ
     * @param vehicleId ID của xe
     * @param requestBody Request body chứa status
     * @return JSON response với kết quả cập nhật
     * @deprecated Sử dụng updateServiceStatusById thay thế
     */
    @Deprecated
    @PutMapping("/admin/vehicle-manager/api/service/{serviceId}/vehicle/{vehicleId}/status")
    @ResponseBody
    public Map<String, Object> updateServiceStatus(
            @PathVariable String serviceId,
            @PathVariable String vehicleId,
            @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("📡 [API] Cập nhật trạng thái dịch vụ (deprecated):");
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
     * API endpoint để lấy danh sách tất cả dịch vụ từ bảng service
     * @return JSON response với danh sách dịch vụ
     */
    @GetMapping("/admin/vehicle-manager/api/services")
    @ResponseBody
    public Map<String, Object> getAllServices() {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("📡 [API] Lấy danh sách dịch vụ từ bảng service");
            
            List<Map<String, Object>> services = serviceRestClient.getAllServices();
            
            response.put("success", true);
            response.put("services", services);
            response.put("count", services.size());
            
            System.out.println("✅ [API] Đã lấy được " + services.size() + " dịch vụ");
            
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
     * API endpoint để thêm dịch vụ mới vào hệ thống (bảng service)
     * @param requestBody Request body chứa serviceId, serviceName, serviceType
     * @return JSON response với kết quả thêm dịch vụ
     */
    @PostMapping("/admin/vehicle-manager/api/services/create")
    @ResponseBody
    public Map<String, Object> createNewService(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("📡 [API] Thêm dịch vụ mới vào hệ thống:");
            System.out.println("   - Request data: " + requestBody);
            
            String serviceId = (String) requestBody.get("serviceId");
            String serviceName = (String) requestBody.get("serviceName");
            String serviceType = (String) requestBody.get("serviceType");
            
            if (serviceId == null || serviceId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Mã dịch vụ (serviceId) là bắt buộc");
                return response;
            }
            
            if (serviceName == null || serviceName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Tên dịch vụ (serviceName) là bắt buộc");
                return response;
            }
            
            if (serviceType == null || serviceType.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Loại dịch vụ (serviceType) là bắt buộc");
                return response;
            }
            
            // Tạo request data để gọi backend API
            Map<String, Object> serviceData = new HashMap<>();
            serviceData.put("serviceId", serviceId.trim());
            serviceData.put("serviceName", serviceName.trim());
            serviceData.put("serviceType", serviceType.trim());
            
            // Gọi backend API để thêm dịch vụ mới vào bảng service
            Map<String, Object> result = serviceRestClient.addService(serviceData);
            
            response.put("success", true);
            response.put("message", "Đã thêm dịch vụ mới vào hệ thống thành công");
            response.put("service", result);
            
            System.out.println("✅ [API] Đã thêm dịch vụ mới vào hệ thống thành công");
            
            return response;
        } catch (RuntimeException e) {
            System.err.println("❌ [API] Lỗi khi thêm dịch vụ mới: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi khi thêm dịch vụ mới");
            return response;
        } catch (Exception e) {
            System.err.println("❌ [API] Lỗi không mong đợi khi thêm dịch vụ mới: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi khi thêm dịch vụ mới: " + e.getMessage());
            return response;
        }
    }

    /**
     * Helper method để lấy serviceType từ service (cột service_type trong bảng vehicleservice)
     */
    private String getServiceTypeFromMap(Map<String, Object> service) {
        String serviceType = (String) service.get("serviceType");
        if (serviceType != null && !serviceType.isEmpty()) {
            return serviceType;
        }
        serviceType = (String) service.get("service_type");
        if (serviceType != null && !serviceType.isEmpty()) {
            return serviceType;
        }
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
        }
        viewData.put("requestDate", requestDate);
        
        return viewData;
    }
}


