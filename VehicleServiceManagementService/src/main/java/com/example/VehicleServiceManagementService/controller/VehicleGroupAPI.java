package com.example.VehicleServiceManagementService.controller;

import com.example.VehicleServiceManagementService.model.Vehiclegroup;
import com.example.VehicleServiceManagementService.service.VehicleGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-groups")
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

    // Sửa thông tin nhóm xe
    @PutMapping("/{id}")
    public Vehiclegroup updateVehicleGroup(@PathVariable Long id, @RequestBody Vehiclegroup vehicleGroup) {
        return vehicleGroupService.updateVehicleGroup(id, vehicleGroup);
    }

    // Xóa nhóm xe
    @DeleteMapping("/{id}")
    public void deleteVehicleGroup(@PathVariable Long id) {
        vehicleGroupService.deleteVehicleGroup(id);
    }

    // Lọc nhóm xe theo tên và trạng thái
    @GetMapping("/filter")
    public List<Vehiclegroup> filterVehicleGroups(
            @RequestParam(value = "searchQuery", required = false, defaultValue = "") String searchQuery,
            @RequestParam(value = "statusFilter", required = false, defaultValue = "Tất cả") String statusFilter) {
        return vehicleGroupService.filterVehicleGroups(searchQuery, statusFilter);
    }
}
