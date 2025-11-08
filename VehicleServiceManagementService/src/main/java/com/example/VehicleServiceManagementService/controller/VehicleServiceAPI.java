package com.example.VehicleServiceManagementService.controller;

import com.example.VehicleServiceManagementService.model.ServiceType;
import com.example.VehicleServiceManagementService.model.Vehicle;
import com.example.VehicleServiceManagementService.model.Vehicleservice;
import com.example.VehicleServiceManagementService.repository.VehicleServiceRepository;
import com.example.VehicleServiceManagementService.service.VehicleServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vehicleservices")
@CrossOrigin(origins = "*")
public class VehicleServiceAPI {

    @Autowired
    private VehicleServiceRepository vehicleServiceRepository;

    @Autowired
    private VehicleServiceService vehicleServiceService;

    /**
     * Test endpoint để kiểm tra controller hoạt động
     */
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "VehicleServiceAPI controller đang hoạt động",
            "timestamp", Instant.now().toString()
        ));
    }

    /**
     * Lấy tất cả các đăng ký dịch vụ xe
     * Sử dụng native query để đảm bảo lấy được dữ liệu
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllVehicleServices() {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("🔵 [GET] /api/vehicleservices - Lấy tất cả đăng ký dịch vụ");
        
        try {
            // Sử dụng native query để lấy dữ liệu trực tiếp từ database
            List<Object[]> nativeResults = vehicleServiceRepository.findAllAsNative();
            System.out.println("✅ Native query trả về " + nativeResults.size() + " records");
            
            // Convert sang Map để trả về JSON
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object[] row : nativeResults) {
                Map<String, Object> serviceMap = new HashMap<>();
                
                // Column order: service_id, vehicle_id, service_name, service_description, 
                //                service_type, request_date, status, completion_date
                String serviceId = row[0] != null ? row[0].toString() : null;
                String vehicleId = row[1] != null ? row[1].toString() : null;
                
                // Composite key
                Map<String, Object> idMap = new HashMap<>();
                idMap.put("serviceId", serviceId);
                idMap.put("vehicleId", vehicleId);
                serviceMap.put("id", idMap);
                
                // Other fields
                serviceMap.put("serviceId", serviceId);
                serviceMap.put("vehicleId", vehicleId);
                
                if (row.length > 2 && row[2] != null) {
                    serviceMap.put("serviceName", row[2].toString());
                }
                if (row.length > 3 && row[3] != null) {
                    serviceMap.put("serviceDescription", row[3].toString());
                }
                if (row.length > 4 && row[4] != null) {
                    serviceMap.put("serviceType", row[4].toString());
                }
                if (row.length > 5 && row[5] != null) {
                    if (row[5] instanceof java.sql.Timestamp) {
                        serviceMap.put("requestDate", ((java.sql.Timestamp) row[5]).toInstant().toString());
                    } else if (row[5] instanceof java.time.Instant) {
                        serviceMap.put("requestDate", row[5].toString());
                    } else if (row[5] instanceof java.time.LocalDateTime) {
                        serviceMap.put("requestDate", ((java.time.LocalDateTime) row[5]).atZone(java.time.ZoneId.systemDefault()).toInstant().toString());
                    } else {
                        serviceMap.put("requestDate", row[5].toString());
                    }
                }
                if (row.length > 6 && row[6] != null) {
                    serviceMap.put("status", row[6].toString());
                }
                if (row.length > 7 && row[7] != null) {
                    if (row[7] instanceof java.sql.Timestamp) {
                        serviceMap.put("completionDate", ((java.sql.Timestamp) row[7]).toInstant().toString());
                    } else if (row[7] instanceof java.time.Instant) {
                        serviceMap.put("completionDate", row[7].toString());
                    } else if (row[7] instanceof java.time.LocalDateTime) {
                        serviceMap.put("completionDate", ((java.time.LocalDateTime) row[7]).atZone(java.time.ZoneId.systemDefault()).toInstant().toString());
                    } else {
                        serviceMap.put("completionDate", row[7].toString());
                    }
                }
                
                result.add(serviceMap);
            }
            
            System.out.println("✅ Trả về " + result.size() + " services cho client");
            System.out.println("═══════════════════════════════════════════════════════");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách: " + e.getMessage());
            System.err.println("   Error Type: " + e.getClass().getName());
            if (e.getCause() != null) {
                System.err.println("   Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ArrayList<>());
        }
    }

    /**
     * Lấy đăng ký dịch vụ theo service_id và vehicle_id
     */
    @GetMapping("/service/{serviceId}/vehicle/{vehicleId}")
    public ResponseEntity<?> getVehicleServiceByServiceAndVehicle(
            @PathVariable String serviceId,
            @PathVariable String vehicleId) {
        try {
            Optional<Vehicleservice> serviceOpt = vehicleServiceRepository
                    .findById_ServiceIdAndId_VehicleId(serviceId, vehicleId);
            if (serviceOpt.isPresent()) {
                return ResponseEntity.ok(serviceOpt.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy đăng ký dịch vụ với serviceId: " + serviceId + " và vehicleId: " + vehicleId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi lấy thông tin dịch vụ: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách dịch vụ của một xe
     */
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<Map<String, Object>>> getVehicleServicesByVehicleId(@PathVariable String vehicleId) {
        try {
            System.out.println("🔵 [GET] /api/vehicleservices/vehicle/" + vehicleId);
            
            // Sử dụng native query để lấy dữ liệu
            List<Object[]> nativeResults = vehicleServiceRepository.findAllAsNative();
            
            // Filter theo vehicleId
            List<Map<String, Object>> result = nativeResults.stream()
                    .filter(row -> row.length > 1 && row[1] != null && vehicleId.equals(row[1].toString()))
                    .map(row -> {
                        Map<String, Object> serviceMap = new HashMap<>();
                        String serviceId = row[0] != null ? row[0].toString() : null;
                        
                        Map<String, Object> idMap = new HashMap<>();
                        idMap.put("serviceId", serviceId);
                        idMap.put("vehicleId", vehicleId);
                        serviceMap.put("id", idMap);
                        serviceMap.put("serviceId", serviceId);
                        serviceMap.put("vehicleId", vehicleId);
                        
                        if (row.length > 2 && row[2] != null) serviceMap.put("serviceName", row[2].toString());
                        if (row.length > 3 && row[3] != null) serviceMap.put("serviceDescription", row[3].toString());
                        if (row.length > 4 && row[4] != null) serviceMap.put("serviceType", row[4].toString());
                        if (row.length > 5 && row[5] != null) {
                            if (row[5] instanceof java.sql.Timestamp) {
                                serviceMap.put("requestDate", ((java.sql.Timestamp) row[5]).toInstant().toString());
                            } else {
                                serviceMap.put("requestDate", row[5].toString());
                            }
                        }
                        if (row.length > 6 && row[6] != null) serviceMap.put("status", row[6].toString());
                        if (row.length > 7 && row[7] != null) {
                            if (row[7] instanceof java.sql.Timestamp) {
                                serviceMap.put("completionDate", ((java.sql.Timestamp) row[7]).toInstant().toString());
                            } else {
                                serviceMap.put("completionDate", row[7].toString());
                            }
                        }
                        
                        return serviceMap;
                    })
                    .collect(Collectors.toList());
            
            System.out.println("✅ Trả về " + result.size() + " services cho vehicle " + vehicleId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách dịch vụ: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ArrayList<>());
        }
    }

    /**
     * Đăng ký dịch vụ xe mới
     */
    @PostMapping
    public ResponseEntity<?> registerVehicleService(@RequestBody Map<String, Object> requestData) {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("🔵 [REGISTER SERVICE] Bắt đầu xử lý đăng ký dịch vụ");
        System.out.println("📥 Request data: " + requestData);
        
        try {
            // Validation
            String serviceId = (String) requestData.get("serviceId");
            if (serviceId == null || serviceId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("serviceId là bắt buộc");
            }

            String vehicleId = (String) requestData.get("vehicleId");
            if (vehicleId == null || vehicleId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("vehicleId là bắt buộc");
            }

            // Validate và lấy service, vehicle
            ServiceType service;
            Vehicle vehicle;
            try {
                service = vehicleServiceService.validateAndGetService(serviceId);
                vehicle = vehicleServiceService.validateAndGetVehicle(vehicleId);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }

            // Tạo entity
            String serviceDescription = (String) requestData.get("serviceDescription");
            String status = (String) requestData.get("status");
            
            Vehicleservice vehicleService = vehicleServiceService.createVehicleService(
                service,
                vehicle,
                serviceDescription,
                status
            );

            // Lưu vào database
            Vehicleservice savedService = vehicleServiceService.saveVehicleService(vehicleService);
            
            System.out.println("✅ [SUCCESS] Đã đăng ký dịch vụ thành công!");
            System.out.println("   - Service ID: " + savedService.getServiceId());
            System.out.println("   - Vehicle ID: " + savedService.getVehicleId());
            System.out.println("═══════════════════════════════════════════════════════");
            
            // Convert sang Map để trả về
            Map<String, Object> response = convertToMap(savedService);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
                
        } catch (IllegalArgumentException e) {
            System.err.println("❌ [VALIDATION ERROR] " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            System.err.println("❌ [DATABASE ERROR] " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi ràng buộc dữ liệu: " + (e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage()));
                    
        } catch (Exception e) {
            System.err.println("❌ [ERROR] " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi đăng ký dịch vụ: " + e.getMessage());
        }
    }

    /**
     * Cập nhật đăng ký dịch vụ
     */
    @PutMapping("/service/{serviceId}/vehicle/{vehicleId}")
    public ResponseEntity<?> updateVehicleService(
            @PathVariable String serviceId,
            @PathVariable String vehicleId,
            @RequestBody Map<String, Object> requestData) {
        try {
            Optional<Vehicleservice> serviceOpt = vehicleServiceRepository
                    .findById_ServiceIdAndId_VehicleId(serviceId, vehicleId);
            if (serviceOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy đăng ký dịch vụ với serviceId: " + serviceId + " và vehicleId: " + vehicleId);
            }

            Vehicleservice service = serviceOpt.get();

            if (requestData.containsKey("serviceDescription")) {
                service.setServiceDescription((String) requestData.get("serviceDescription"));
            }
            
            if (requestData.containsKey("serviceType")) {
                service.setServiceType((String) requestData.get("serviceType"));
            }
            
            if (requestData.containsKey("status")) {
                String newStatus = (String) requestData.get("status");
                service.setStatus(newStatus);
                
                // Tự động set completionDate khi status = completed
                if (newStatus != null && newStatus.equalsIgnoreCase("completed")) {
                    if (service.getCompletionDate() == null) {
                        service.setCompletionDate(Instant.now());
                        System.out.println("✅ Tự động set completionDate = " + Instant.now());
                    }
                } else if (newStatus != null && (newStatus.equalsIgnoreCase("pending") || newStatus.equalsIgnoreCase("in_progress") || newStatus.equalsIgnoreCase("in progress"))) {
                    // Reset completionDate nếu chuyển về pending/in_progress
                    service.setCompletionDate(null);
                }
            }
            
            if (requestData.containsKey("completionDate")) {
                String completionDateStr = (String) requestData.get("completionDate");
                if (completionDateStr != null && !completionDateStr.isEmpty()) {
                    service.setCompletionDate(Instant.parse(completionDateStr));
                } else {
                    service.setCompletionDate(null);
                }
            }

            Vehicleservice updatedService = vehicleServiceRepository.save(service);
            Map<String, Object> response = convertToMap(updatedService);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi cập nhật dịch vụ: " + e.getMessage());
        }
    }

    /**
     * Xóa đăng ký dịch vụ
     */
    @DeleteMapping("/service/{serviceId}/vehicle/{vehicleId}")
    public ResponseEntity<?> deleteVehicleService(
            @PathVariable String serviceId,
            @PathVariable String vehicleId) {
        try {
            if (vehicleServiceRepository.existsById_ServiceIdAndId_VehicleId(serviceId, vehicleId)) {
                vehicleServiceRepository.deleteById_ServiceIdAndId_VehicleId(serviceId, vehicleId);
                return ResponseEntity.ok("Đăng ký dịch vụ đã được xóa thành công");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy đăng ký dịch vụ với serviceId: " + serviceId + " và vehicleId: " + vehicleId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi xóa dịch vụ: " + e.getMessage());
        }
    }
    
    /**
     * Helper method để convert Vehicleservice entity sang Map
     */
    private Map<String, Object> convertToMap(Vehicleservice vs) {
        Map<String, Object> map = new HashMap<>();
        
        // Composite key
        Map<String, Object> idMap = new HashMap<>();
        idMap.put("serviceId", vs.getServiceId());
        idMap.put("vehicleId", vs.getVehicleId());
        map.put("id", idMap);
        
        // Other fields
        map.put("serviceId", vs.getServiceId());
        map.put("vehicleId", vs.getVehicleId());
        map.put("serviceName", vs.getServiceName());
        map.put("serviceDescription", vs.getServiceDescription());
        map.put("serviceType", vs.getServiceType());
        map.put("status", vs.getStatus());
        
        if (vs.getRequestDate() != null) {
            map.put("requestDate", vs.getRequestDate().toString());
        }
        if (vs.getCompletionDate() != null) {
            map.put("completionDate", vs.getCompletionDate().toString());
        }
        
        return map;
    }
}
