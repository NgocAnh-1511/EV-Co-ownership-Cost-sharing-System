package com.example.VehicleServiceManagementService.controller;

import com.example.VehicleServiceManagementService.model.ServiceType;
import com.example.VehicleServiceManagementService.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
public class ServiceAPI {

    @Autowired
    private ServiceService serviceService;

    /**
     * Lấy tất cả các dịch vụ từ bảng service
     * @return Danh sách tất cả dịch vụ
     */
    @GetMapping
    public ResponseEntity<List<ServiceType>> getAllServices() {
        try {
            List<ServiceType> services = serviceService.getAllServices();
            System.out.println("✅ API: Đã lấy " + services.size() + " dịch vụ từ bảng service");
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            System.err.println("❌ API: Lỗi khi lấy danh sách dịch vụ: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy dịch vụ theo ID
     * @param serviceId ID của dịch vụ
     * @return ResponseEntity với ServiceType hoặc thông báo lỗi
     */
    @GetMapping("/{serviceId}")
    public ResponseEntity<?> getServiceById(@PathVariable String serviceId) {
        try {
            ServiceType service = serviceService.getServiceById(serviceId);
            if (service != null) {
                return ResponseEntity.ok(service);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy dịch vụ với ID: " + serviceId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi lấy thông tin dịch vụ: " + e.getMessage());
        }
    }

    /**
     * Thêm dịch vụ mới vào bảng service
     * @param service Dịch vụ cần thêm
     * @return ResponseEntity với ServiceType đã được tạo
     */
    @PostMapping
    public ResponseEntity<?> addService(@Valid @RequestBody ServiceType service, BindingResult bindingResult) {
        try {
            // Validation
            if (bindingResult.hasErrors()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Dữ liệu không hợp lệ: " + bindingResult.getFieldError().getDefaultMessage()));
            }

            if (service.getServiceName() == null || service.getServiceName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Tên dịch vụ không được để trống"));
            }

            // Nếu serviceId không được cung cấp, sẽ tự động generate
            // Nếu có serviceId, kiểm tra đã tồn tại chưa
            if (service.getServiceId() != null && !service.getServiceId().trim().isEmpty()) {
                if (serviceService.existsById(service.getServiceId())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(createErrorResponse("Service ID '" + service.getServiceId() + "' đã tồn tại"));
                }
            }

            ServiceType savedService = serviceService.addService(service);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thêm dịch vụ thành công");
            response.put("data", savedService);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Service ID đã tồn tại"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Đã xảy ra lỗi khi thêm dịch vụ: " + e.getMessage()));
        }
    }

    /**
     * Cập nhật dịch vụ trong bảng service
     * @param serviceId ID của dịch vụ cần cập nhật
     * @param service Dịch vụ với thông tin mới
     * @return ResponseEntity với ServiceType đã được cập nhật hoặc thông báo lỗi
     */
    @PutMapping("/{serviceId}")
    public ResponseEntity<?> updateService(@PathVariable String serviceId, @Valid @RequestBody ServiceType service, BindingResult bindingResult) {
        try {
            // Validation
            if (bindingResult.hasErrors()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Dữ liệu không hợp lệ: " + bindingResult.getFieldError().getDefaultMessage()));
            }

            ServiceType updatedService = serviceService.updateService(serviceId, service);
            if (updatedService != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Cập nhật dịch vụ thành công");
                response.put("data", updatedService);
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Không tìm thấy dịch vụ với ID: " + serviceId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Đã xảy ra lỗi khi cập nhật dịch vụ: " + e.getMessage()));
        }
    }

    /**
     * Xóa dịch vụ khỏi bảng service
     * @param serviceId ID của dịch vụ cần xóa
     * @return ResponseEntity với thông báo kết quả
     */
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<?> deleteService(@PathVariable String serviceId) {
        try {
            boolean deleted = serviceService.deleteService(serviceId);
            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Dịch vụ đã được xóa thành công");
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Không tìm thấy dịch vụ với ID: " + serviceId));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Không thể xóa dịch vụ vì đang được sử dụng trong hệ thống"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Đã xảy ra lỗi khi xóa dịch vụ: " + e.getMessage()));
        }
    }

    /**
     * Kiểm tra dịch vụ có tồn tại không
     * @param serviceId ID của dịch vụ
     * @return ResponseEntity với kết quả kiểm tra
     */
    @GetMapping("/{serviceId}/exists")
    public ResponseEntity<?> checkServiceExists(@PathVariable String serviceId) {
        try {
            boolean exists = serviceService.existsById(serviceId);
            Map<String, Object> response = new HashMap<>();
            response.put("exists", exists);
            response.put("serviceId", serviceId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Đã xảy ra lỗi khi kiểm tra dịch vụ: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách các loại dịch vụ duy nhất từ bảng service
     * @return ResponseEntity với danh sách loại dịch vụ
     */
    @GetMapping("/types")
    public ResponseEntity<?> getServiceTypes() {
        try {
            List<String> serviceTypes = serviceService.getDistinctServiceTypes();
            return ResponseEntity.ok(serviceTypes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Đã xảy ra lỗi khi lấy danh sách loại dịch vụ: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách dịch vụ theo loại
     * @param serviceType Loại dịch vụ
     * @return ResponseEntity với danh sách dịch vụ
     */
    @GetMapping("/type/{serviceType}")
    public ResponseEntity<?> getServicesByType(@PathVariable String serviceType) {
        try {
            List<ServiceType> services = serviceService.getServicesByType(serviceType);
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Đã xảy ra lỗi khi lấy danh sách dịch vụ: " + e.getMessage()));
        }
    }

    /**
     * Đếm tổng số dịch vụ
     * @return ResponseEntity với số lượng dịch vụ
     */
    @GetMapping("/count")
    public ResponseEntity<?> getServiceCount() {
        try {
            long count = serviceService.count();
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Đã xảy ra lỗi khi đếm số lượng dịch vụ: " + e.getMessage()));
        }
    }

    /**
     * Lấy service_id tiếp theo sẽ được generate
     * @return ResponseEntity với service_id tiếp theo
     */
    @GetMapping("/next-id")
    public ResponseEntity<?> getNextServiceId() {
        try {
            String nextId = serviceService.generateNextServiceId();
            Map<String, Object> response = new HashMap<>();
            response.put("nextServiceId", nextId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Đã xảy ra lỗi khi lấy service_id tiếp theo: " + e.getMessage()));
        }
    }

    /**
     * Tạo response lỗi
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
