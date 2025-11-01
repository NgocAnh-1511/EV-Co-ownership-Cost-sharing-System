package com.example.VehicleServiceManagementService.repository;

import com.example.VehicleServiceManagementService.model.Vehiclegroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleGroupRepository extends JpaRepository<Vehiclegroup, Long> {
    // Các phương thức truy vấn tùy chỉnh có thể thêm vào đây nếu cần
}
