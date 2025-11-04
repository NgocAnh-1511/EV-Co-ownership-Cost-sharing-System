package com.example.VehicleServiceManagementService.repository;

import com.example.VehicleServiceManagementService.model.Vehicleservice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface VehicleServiceRepository extends JpaRepository<Vehicleservice, Integer> {
    
    /**
     * Tìm tất cả các dịch vụ của một xe
     * @param vehicleId ID của xe
     * @return Danh sách các dịch vụ
     */
    List<Vehicleservice> findByVehicle_VehicleId(String vehicleId);
    
    /**
     * Xóa tất cả các dịch vụ của một xe
     * @param vehicleId ID của xe
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Vehicleservice v WHERE v.vehicle.vehicleId = :vehicleId")
    void deleteByVehicleId(@Param("vehicleId") String vehicleId);
}

