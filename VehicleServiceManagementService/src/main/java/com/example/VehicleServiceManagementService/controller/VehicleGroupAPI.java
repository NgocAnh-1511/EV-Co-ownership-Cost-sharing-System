package com.example.VehicleServiceManagementService.controller;

import com.example.VehicleServiceManagementService.model.Vehiclegroup;
import com.example.VehicleServiceManagementService.service.VehicleGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-groups")
@CrossOrigin(origins = "*")
public class VehicleGroupAPI {

    @Autowired
    private VehicleGroupService vehicleGroupService;

    // Lấy danh sách tất cả nhóm xe

    @GetMapping
    public List<Vehiclegroup> getAllVehicleGroups() {
        // Gọi phương thức getAllVehicleGroups() trong service
        return vehicleGroupService.getAllVehicleGroups();
    }

    // Thêm nhóm xe mới
    @PostMapping
    public Vehiclegroup addVehicleGroup(@RequestBody Vehiclegroup vehicleGroup) {
        return vehicleGroupService.addVehicleGroup(vehicleGroup);
    }

    /**
     * Sửa thông tin nhóm xe
     * Có thể sửa: tên, số lượng xe, trạng thái, mô tả
     * 
     * @param groupId ID của nhóm xe cần sửa
     * @param vehicleGroup Đối tượng chứa thông tin cần cập nhật
     * @return ResponseEntity với Vehiclegroup đã được cập nhật hoặc thông báo lỗi
     */
    @PutMapping("/{groupId}")
    public ResponseEntity<?> updateVehicleGroup(@PathVariable String groupId, @RequestBody Vehiclegroup vehicleGroup) {
        try {
            Vehiclegroup updatedGroup = vehicleGroupService.updateVehicleGroup(groupId, vehicleGroup);
            if (updatedGroup != null) {
                return ResponseEntity.ok(updatedGroup);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy nhóm xe với ID: " + groupId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi cập nhật nhóm xe: " + e.getMessage());
        }
    }

    /**
     * Xóa nhóm xe theo groupId
     * Tự động xóa tất cả xe trong nhóm trước khi xóa nhóm
     * @param groupId ID của nhóm xe cần xóa
     * @return ResponseEntity với thông báo kết quả
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<String> deleteVehicleGroup(@PathVariable String groupId) {
        try {
            String resultMessage = vehicleGroupService.deleteVehicleGroup(groupId);
            if (resultMessage != null) {
                return ResponseEntity.ok(resultMessage);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy nhóm xe với ID: " + groupId);
        } catch (Exception e) {
            // Xử lý các lỗi khác
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi xóa nhóm xe: " + e.getMessage());
        }
    }


    // Lọc nhóm xe theo tên và trạng thái
    @GetMapping("/filter")
    public List<Vehiclegroup> filterVehicleGroups(
            @RequestParam(value = "searchQuery", required = false, defaultValue = "") String searchQuery,
            @RequestParam(value = "statusFilter", required = false, defaultValue = "Tất cả") String statusFilter) {
        return vehicleGroupService.filterVehicleGroups(searchQuery, statusFilter);
    }
}
