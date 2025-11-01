package com.example.VehicleServiceManagementService.service;

import com.example.VehicleServiceManagementService.model.Vehicle;
import com.example.VehicleServiceManagementService.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    // Lấy tất cả danh sách xe
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    // Lấy thông tin xe theo ID
    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    // Thêm mới xe
    public Vehicle saveVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    // Cập nhật thông tin xe
    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(id);
        if (vehicle.isPresent()) {
            Vehicle v = vehicle.get();
            v.setVehicleName(vehicleDetails.getVehicleName());
            v.setLicensePlate(vehicleDetails.getLicensePlate());
            v.setStatus(vehicleDetails.getStatus());
            // Cập nhật các thuộc tính khác...
            return vehicleRepository.save(v);
        }
        return null;
    }

    // Xóa xe
    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }
}
