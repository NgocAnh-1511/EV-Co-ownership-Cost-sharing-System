package com.example.VehicleServiceManagementService.repository;

import com.example.VehicleServiceManagementService.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    // JpaRepository đã cung cấp các phương thức cơ bản như save(), findAll(), findById(), deleteById()...
}
