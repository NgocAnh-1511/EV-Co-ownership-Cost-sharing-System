package com.example.VehicleServiceManagementService.service;

import com.example.VehicleServiceManagementService.model.Vehiclegroup;
import com.example.VehicleServiceManagementService.repository.VehicleGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleGroupService {

    @Autowired
    private VehicleGroupRepository vehicleGroupRepository;

    // Lấy tất cả nhóm xe
    public List<Vehiclegroup> getAllVehicleGroups() {
        return vehicleGroupRepository.findAll();

    }

    // Thêm nhóm xe mới
    public Vehiclegroup addVehicleGroup(Vehiclegroup vehicleGroup) {
        return vehicleGroupRepository.save(vehicleGroup);
    }

    // Sửa thông tin nhóm xe
    public Vehiclegroup updateVehicleGroup(Long id, Vehiclegroup vehicleGroup) {
        Optional<Vehiclegroup> existingGroup = vehicleGroupRepository.findById(id);
        if (existingGroup.isPresent()) {
            Vehiclegroup groupToUpdate = existingGroup.get();
            groupToUpdate.setName(vehicleGroup.getName());
            groupToUpdate.setVehicleCount(vehicleGroup.getVehicleCount());
            // Cập nhật các thuộc tính khác nếu cần
            return vehicleGroupRepository.save(groupToUpdate);
        }
        return null; // Hoặc throw exception nếu không tìm thấy nhóm
    }

    // Xóa nhóm xe
    public void deleteVehicleGroup(Long id) {
        vehicleGroupRepository.deleteById(id);
    }
}
