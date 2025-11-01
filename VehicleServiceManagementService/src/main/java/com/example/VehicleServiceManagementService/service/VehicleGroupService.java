package com.example.VehicleServiceManagementService.service;

import com.example.VehicleServiceManagementService.model.Vehiclegroup;
import com.example.VehicleServiceManagementService.repository.VehicleGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VehicleGroupService {

    @Autowired
    private VehicleGroupRepository vehicleGroupRepository;

    /**
     * Lấy tất cả các nhóm xe
     * @return Danh sách tất cả nhóm xe
     */
    public List<Vehiclegroup> getAllVehicleGroups() {
        // Trả về danh sách tất cả các nhóm xe từ repository
        return vehicleGroupRepository.findAll();
    }

    /**
     * Lọc nhóm xe theo tên và trạng thái
     * @param searchQuery Tìm kiếm theo tên nhóm
     * @param statusFilter Lọc theo trạng thái nhóm
     * @return Danh sách nhóm xe đã lọc
     */
    public List<Vehiclegroup> filterVehicleGroups(String searchQuery, String statusFilter) {
        List<Vehiclegroup> allGroups = vehicleGroupRepository.findAll();  // Lấy tất cả các nhóm xe

        // Lọc nhóm xe theo tên và trạng thái
        return allGroups.stream()
                .filter(group -> (searchQuery == null || group.getName().toLowerCase().contains(searchQuery.toLowerCase())) &&  // Lọc theo tên
                        (statusFilter == null || "Tất cả".equals(statusFilter) || group.getActive().equals(statusFilter)))  // Lọc theo trạng thái
                .collect(Collectors.toList());  // Trả về danh sách nhóm xe đã lọc
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
