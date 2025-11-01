package com.example.VehicleServiceManagementService.controller;

import com.example.VehicleServiceManagementService.service.VehicleGroupService;
import com.example.VehicleServiceManagementService.model.Vehiclegroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-groups")
public class VehicleGroupAPI {

    @Autowired
    private VehicleGroupService vehicleGroupService;

    // Lấy danh sách nhóm xe
    @GetMapping
    public List<Vehiclegroup> getAllVehicleGroups() {
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
}