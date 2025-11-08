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
    
    /**
     * Test query để kiểm tra dữ liệu có tồn tại không (native query)
     */
    @Query(value = "SELECT COUNT(*) FROM vehicle_management.vehicleservice", nativeQuery = true)
    long countAllNative();
    
    /**
     * Lấy tất cả vehicleservice bằng native query (để test)
     */
    @Query(value = "SELECT * FROM vehicle_management.vehicleservice", nativeQuery = true)
    List<Object[]> findAllNative();
    
    /**
     * Lấy tất cả vehicleservice với LEFT JOIN để tránh bị filter
     * Sử dụng JPQL với LEFT JOIN để đảm bảo load được ngay cả khi foreign key có vấn đề
     */
    @Query("SELECT v FROM Vehicleservice v LEFT JOIN FETCH v.service LEFT JOIN FETCH v.vehicle")
    List<Vehicleservice> findAllWithJoins();
    
    /**
     * Lấy tất cả vehicleservice không cần join (chỉ lấy từ bảng vehicleservice)
     * Sử dụng native query và build entity manually
     */
    @Query(value = "SELECT vs.service_id, vs.vehicle_id, vs.service_name, vs.service_description, " +
                   "vs.service_type, vs.request_date, vs.status, vs.completion_date " +
                   "FROM vehicle_management.vehicleservice vs", nativeQuery = true)
    List<Object[]> findAllAsNative();
    
    /**
     * Kiểm tra duplicate bằng native query (để tránh vấn đề với composite key)
     */
    @Query(value = "SELECT COUNT(*) FROM vehicle_management.vehicleservice " +
                   "WHERE service_id = :serviceId AND vehicle_id = :vehicleId", nativeQuery = true)
    long countByServiceIdAndVehicleIdNative(@Param("serviceId") String serviceId, @Param("vehicleId") String vehicleId);
    
    /**
     * Kiểm tra xem có dịch vụ đang chờ (pending/in_progress) không
     * Chỉ chặn duplicate nếu dịch vụ trước đó chưa completed
     */
    @Query(value = "SELECT COUNT(*) FROM vehicle_management.vehicleservice " +
                   "WHERE service_id = :serviceId AND vehicle_id = :vehicleId " +
                   "AND status IN ('pending', 'in_progress', 'in progress')", nativeQuery = true)
    long countActiveByServiceIdAndVehicleId(@Param("serviceId") String serviceId, @Param("vehicleId") String vehicleId);
}

