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
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * @return Thông báo thành công
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
     * @return Danh sách tất cả đăng ký dịch vụ
     */
    @GetMapping
    public ResponseEntity<List<Vehicleservice>> getAllVehicleServices() {
        System.out.println("🔵 [GET] /api/vehicleservices - Lấy tất cả đăng ký dịch vụ");
        try {
            List<Vehicleservice> services = vehicleServiceRepository.findAll();
            System.out.println("✅ Đã lấy " + services.size() + " đăng ký dịch vụ");
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    /**
     * Lấy đăng ký dịch vụ theo service_id và vehicle_id
     * @param serviceId ID của dịch vụ
     * @param vehicleId ID của xe
     * @return ResponseEntity với Vehicleservice hoặc thông báo lỗi
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
     * @param vehicleId ID của xe
     * @return Danh sách dịch vụ của xe
     */
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<?> getVehicleServicesByVehicleId(@PathVariable String vehicleId) {
        try {
            List<Vehicleservice> services = vehicleServiceRepository.findByVehicle_VehicleId(vehicleId);
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi lấy danh sách dịch vụ: " + e.getMessage());
        }
    }

    /**
     * Đăng ký dịch vụ xe mới
     * Controller không có @Transactional - transaction được quản lý bởi service layer
     * @param requestData Map chứa thông tin đăng ký dịch vụ
     * @return ResponseEntity với Vehicleservice đã được tạo
     */
    @PostMapping
    public ResponseEntity<?> registerVehicleService(@RequestBody Map<String, Object> requestData) {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("🔵 [REGISTER SERVICE] Bắt đầu xử lý đăng ký dịch vụ");
        System.out.println("📥 Request data: " + requestData);
        
        try {
            // ========== BƯỚC 1: VALIDATION DỮ LIỆU ĐẦU VÀO ==========
            System.out.println("📋 [STEP 1] Validation dữ liệu đầu vào...");
            
            if (requestData == null) {
                System.err.println("❌ Request data is null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Request data không được để trống");
            }
            
            String serviceId = (String) requestData.get("serviceId");
            System.out.println("   - serviceId: " + serviceId);
            if (serviceId == null || serviceId.trim().isEmpty()) {
                System.err.println("❌ serviceId is null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("serviceId là bắt buộc");
            }

            String vehicleId = (String) requestData.get("vehicleId");
            System.out.println("   - vehicleId: " + vehicleId);
            if (vehicleId == null || vehicleId.trim().isEmpty()) {
                System.err.println("❌ vehicleId is null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("vehicleId là bắt buộc");
            }

            // ========== BƯỚC 2: KIỂM TRA SERVICE VÀ VEHICLE TỒN TẠI ==========
            System.out.println("📋 [STEP 2] Kiểm tra service và vehicle tồn tại...");
            
            ServiceType service;
            Vehicle vehicle;
            try {
                service = vehicleServiceService.validateAndGetService(serviceId);
                System.out.println("   ✅ Service found: " + service.getServiceName() + " (type: " + service.getServiceType() + ")");
                
                vehicle = vehicleServiceService.validateAndGetVehicle(vehicleId);
                System.out.println("   ✅ Vehicle found: " + vehicle.getVehicleNumber());
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Validation error: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }

            // ========== BƯỚC 3: TẠO ENTITY ==========
            System.out.println("📋 [STEP 3] Tạo Vehicleservice entity...");
            
            String serviceDescription = (String) requestData.get("serviceDescription");
            String status = (String) requestData.get("status");
            
            Vehicleservice vehicleService = vehicleServiceService.createVehicleService(
                service,
                vehicle,
                serviceDescription,
                status
            );
            
            System.out.println("   - status: " + vehicleService.getStatus());
            System.out.println("   - request_date: " + vehicleService.getRequestDate());
            System.out.println("   ✅ Entity created successfully");

            // ========== BƯỚC 4: LƯU VÀO DATABASE (TRONG SERVICE LAYER VỚI TRANSACTION) ==========
            System.out.println("📋 [STEP 4] Lưu vào database (service layer với transaction)...");
            
            // Gọi service method có @Transactional - exception sẽ propagate ra ngoài nếu có lỗi
            Vehicleservice savedService = vehicleServiceService.saveVehicleService(vehicleService);
            
            System.out.println("✅ [SUCCESS] Đã đăng ký dịch vụ thành công!");
            System.out.println("   - Service ID: " + savedService.getServiceId());
            System.out.println("   - Vehicle ID: " + savedService.getVehicleId());
            System.out.println("   - Service Name: " + savedService.getServiceName());
            System.out.println("═══════════════════════════════════════════════════════");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedService);
                
        } catch (IllegalArgumentException e) {
            // Validation errors từ service
            System.err.println("❌ [VALIDATION ERROR] IllegalArgumentException:");
            System.err.println("   Message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Database constraint violations
            System.err.println("❌ [DATABASE ERROR] DataIntegrityViolationException:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Root Cause: " + (e.getRootCause() != null ? e.getRootCause().getMessage() : "null"));
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi ràng buộc dữ liệu: " + (e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage()));
                    
        } catch (jakarta.persistence.PersistenceException e) {
            // JPA persistence errors
            System.err.println("❌ [PERSISTENCE ERROR] PersistenceException:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "null"));
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi persistence: " + e.getMessage());
                    
        } catch (RuntimeException e) {
            // Runtime errors (bao gồm các lỗi từ service layer)
            System.err.println("❌ [RUNTIME ERROR] RuntimeException:");
            System.err.println("   Type: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "null"));
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi đăng ký dịch vụ: " + e.getMessage() + 
                          (e.getCause() != null ? " (Cause: " + e.getCause().getMessage() + ")" : ""));
                          
        } catch (Exception e) {
            // Các lỗi khác
            System.err.println("❌ [UNEXPECTED ERROR] Exception:");
            System.err.println("   Type: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "null"));
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi đăng ký dịch vụ: " + e.getMessage() + 
                          (e.getCause() != null ? " (Cause: " + e.getCause().getMessage() + ")" : ""));
        }
    }

    /**
     * Cập nhật đăng ký dịch vụ
     * @param serviceId ID của dịch vụ
     * @param vehicleId ID của xe
     * @param requestData Map chứa thông tin cần cập nhật
     * @return ResponseEntity với Vehicleservice đã được cập nhật hoặc thông báo lỗi
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

            // Lưu ý: Không thể thay đổi service_id và vehicle_id vì chúng là primary key
            // Chỉ có thể cập nhật các thông tin khác
            
            if (requestData.containsKey("serviceDescription")) {
                service.setServiceDescription((String) requestData.get("serviceDescription"));
            }
            
            if (requestData.containsKey("serviceType")) {
                service.setServiceType((String) requestData.get("serviceType"));
            }
            
            if (requestData.containsKey("status")) {
                service.setStatus((String) requestData.get("status"));
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
            return ResponseEntity.ok(updatedService);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi cập nhật dịch vụ: " + e.getMessage());
        }
    }

    /**
     * Xóa đăng ký dịch vụ
     * @param serviceId ID của dịch vụ
     * @param vehicleId ID của xe
     * @return ResponseEntity với thông báo kết quả
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
}
