package com.example.VehicleServiceManagementService.repository;

import com.example.VehicleServiceManagementService.model.VehicleServiceId;
import com.example.VehicleServiceManagementService.model.Vehicleservice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleServiceRepository extends JpaRepository<Vehicleservice, VehicleServiceId> {
    
    /**
     * Tìm tất cả các dịch vụ của một xe
     * @param vehicleId ID của xe
     * @return Danh sách các dịch vụ
     */
    List<Vehicleservice> findByVehicle_VehicleId(String vehicleId);
    
    /**
     * Tìm tất cả các dịch vụ theo service_id
     * @param serviceId ID của dịch vụ
     * @return Danh sách các đăng ký dịch vụ
     */
    List<Vehicleservice> findByService_ServiceId(String serviceId);
    
    /**
     * Tìm đăng ký dịch vụ theo service_id và vehicle_id
     * @param serviceId ID của dịch vụ
     * @param vehicleId ID của xe
     * @return Optional Vehicleservice
     */
    Optional<Vehicleservice> findById_ServiceIdAndId_VehicleId(String serviceId, String vehicleId);
    
    /**
     * Kiểm tra đăng ký dịch vụ có tồn tại không
     * @param serviceId ID của dịch vụ
     * @param vehicleId ID của xe
     * @return true nếu tồn tại
     */
    boolean existsById_ServiceIdAndId_VehicleId(String serviceId, String vehicleId);
    
    /**
     * Xóa đăng ký dịch vụ theo service_id và vehicle_id
     * @param serviceId ID của dịch vụ
     * @param vehicleId ID của xe
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Vehicleservice v WHERE v.id.serviceId = :serviceId AND v.id.vehicleId = :vehicleId")
    void deleteById_ServiceIdAndId_VehicleId(@Param("serviceId") String serviceId, @Param("vehicleId") String vehicleId);
    
    /**
     * Xóa tất cả các dịch vụ của một xe
     * @param vehicleId ID của xe
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Vehicleservice v WHERE v.vehicle.vehicleId = :vehicleId")
    void deleteByVehicleId(@Param("vehicleId") String vehicleId);
}

