package com.example.VehicleServiceManagementService.controller;

import com.example.VehicleServiceManagementService.model.Vehicle;
import com.example.VehicleServiceManagementService.model.Vehiclegroup;
import com.example.VehicleServiceManagementService.repository.VehicleRepository;
import com.example.VehicleServiceManagementService.repository.VehicleGroupRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehicleAPI {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleGroupRepository vehicleGroupRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Thêm nhiều xe vào nhóm
     * @param requestData Map chứa groupId và danh sách vehicles
     * @return ResponseEntity với danh sách xe đã được tạo hoặc thông báo lỗi
     */
    @PostMapping("/batch")
    @Transactional
    public ResponseEntity<?> addVehicles(@RequestBody Map<String, Object> requestData) {
        try {
            String groupId = (String) requestData.get("groupId");
            if (groupId == null || groupId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("groupId là bắt buộc");
            }
            
            Optional<Vehiclegroup> groupOpt = vehicleGroupRepository.findById(groupId);
            if (groupOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy nhóm xe với ID: " + groupId);
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> vehiclesData = (List<Map<String, Object>>) requestData.get("vehicles");
            
            if (vehiclesData == null || vehiclesData.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Danh sách xe không được để trống");
            }
            
            Vehiclegroup group = groupOpt.get();
            List<Vehicle> vehicles = vehiclesData.stream().map(vehicleData -> {
                Vehicle vehicle = new Vehicle();
                
                if (vehicleData.containsKey("vehicleId")) {
                    vehicle.setVehicleId((String) vehicleData.get("vehicleId"));
                }
                
                if (vehicleData.containsKey("type")) {
                    vehicle.setVehicleType((String) vehicleData.get("type"));
                } else if (vehicleData.containsKey("vehicleType")) {
                    vehicle.setVehicleType((String) vehicleData.get("vehicleType"));
                }
                
                if (vehicleData.containsKey("vehicleNumber")) {
                    vehicle.setVehicleNumber((String) vehicleData.get("vehicleNumber"));
                }
                
                if (vehicleData.containsKey("status")) {
                    vehicle.setStatus((String) vehicleData.get("status"));
                } else {
                    vehicle.setStatus("available");
                }
                
                vehicle.setGroup(group);
                return vehicle;
            }).toList();
            
            List<Vehicle> savedVehicles = vehicleRepository.saveAll(vehicles);
            
            // Cập nhật lại số lượng xe trong nhóm
            long actualVehicleCount = vehicleRepository.countByGroupId(groupId);
            group.setVehicleCount((int) actualVehicleCount);
            vehicleGroupRepository.save(group);
            
            System.out.println("DEBUG: Đã thêm " + savedVehicles.size() + " xe vào nhóm " + groupId);
            System.out.println("DEBUG: Số lượng xe trong nhóm sau khi thêm: " + actualVehicleCount);
            
            return ResponseEntity.ok(savedVehicles);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi thêm xe: " + e.getMessage());
        }
    }

    /**
     * Cập nhật thông tin xe
     * @param vehicleId ID của xe cần cập nhật
     * @param vehicleData Map chứa thông tin cần cập nhật
     * @return ResponseEntity với Vehicle đã được cập nhật hoặc thông báo lỗi
     */
    @PutMapping("/{vehicleId}")
    @Transactional
    public ResponseEntity<?> updateVehicle(@PathVariable String vehicleId, @RequestBody Map<String, Object> vehicleData) {
        try {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
            if (vehicleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy xe với ID: " + vehicleId);
            }
            
            Vehicle vehicle = vehicleOpt.get();
            
            // Cập nhật loại xe
            if (vehicleData.containsKey("type")) {
                vehicle.setVehicleType((String) vehicleData.get("type"));
            } else if (vehicleData.containsKey("vehicleType")) {
                vehicle.setVehicleType((String) vehicleData.get("vehicleType"));
            }
            
            // Cập nhật biển số xe
            if (vehicleData.containsKey("vehicleNumber")) {
                vehicle.setVehicleNumber((String) vehicleData.get("vehicleNumber"));
            }
            
            // Cập nhật trạng thái xe
            if (vehicleData.containsKey("status")) {
                vehicle.setStatus((String) vehicleData.get("status"));
            }
            
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            System.out.println("DEBUG: Đã cập nhật xe " + vehicleId);
            
            return ResponseEntity.ok(updatedVehicle);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi cập nhật xe: " + e.getMessage());
        }
    }

    /**
     * Xóa xe khỏi nhóm
     * @param vehicleId ID của xe cần xóa
     * @return ResponseEntity với thông báo kết quả
     */
    @DeleteMapping("/{vehicleId}")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> deleteVehicle(@PathVariable String vehicleId) {
        try {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
            if (vehicleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy xe với ID: " + vehicleId);
            }
            
            Vehicle vehicle = vehicleOpt.get();
            String groupId = vehicle.getGroup() != null ? vehicle.getGroup().getGroupId() : null;
            
            // Tắt foreign key checks tạm thời để xóa các bản ghi liên quan
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            
            try {
                // Xóa tất cả các dịch vụ liên quan đến xe trước
                int deletedServices = entityManager.createNativeQuery(
                    "DELETE FROM vehicle_management.vehicleservice WHERE vehicle_id = :vehicleId"
                ).setParameter("vehicleId", vehicleId).executeUpdate();
                System.out.println("DEBUG: Đã xóa " + deletedServices + " dịch vụ liên quan đến xe " + vehicleId);
                
                // Xóa xe
                vehicleRepository.deleteById(vehicleId);
                vehicleRepository.flush(); // Đảm bảo xóa được thực thi ngay
                
                // Cập nhật lại số lượng xe trong nhóm nếu có
                if (groupId != null) {
                    long actualVehicleCount = vehicleRepository.countByGroupId(groupId);
                    vehicleGroupRepository.findById(groupId).ifPresent(group -> {
                        group.setVehicleCount((int) actualVehicleCount);
                        vehicleGroupRepository.save(group);
                        vehicleGroupRepository.flush(); // Đảm bảo cập nhật được thực thi ngay
                        System.out.println("DEBUG: Đã cập nhật số lượng xe trong nhóm " + groupId + " thành " + actualVehicleCount);
                    });
                }
                
                System.out.println("DEBUG: Đã xóa xe " + vehicleId + " thành công");
            } finally {
                // Bật lại foreign key checks
                entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            }
            
            return ResponseEntity.ok("Xe đã được xóa thành công");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xóa xe vì xe đang được sử dụng trong hệ thống. Vui lòng xóa các bản ghi liên quan trước.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi xóa xe: " + e.getMessage());
        }
    }
}

