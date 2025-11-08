package com.example.VehicleServiceManagementService.service;

import com.example.VehicleServiceManagementService.model.VehicleServiceId;
import com.example.VehicleServiceManagementService.model.Vehicleservice;
import com.example.VehicleServiceManagementService.model.Vehicle;
import com.example.VehicleServiceManagementService.model.ServiceType;
import com.example.VehicleServiceManagementService.repository.VehicleServiceRepository;
import com.example.VehicleServiceManagementService.repository.VehicleRepository;
import com.example.VehicleServiceManagementService.repository.ServiceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleServiceService {

    @Autowired
    private VehicleServiceRepository vehicleServiceRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Lưu Vehicleservice vào database với transaction
     * Sử dụng composite key (service_id, vehicle_id) làm primary key
     */
    @Transactional
    public Vehicleservice saveVehicleService(Vehicleservice vehicleService) {
        System.out.println("   🔒 [SERVICE] Bắt đầu save entity trong transaction...");
        
        try {
            // Đảm bảo id được khởi tạo
            if (vehicleService.getId() == null) {
                vehicleService.initializeId();
            }
            
            String serviceId = vehicleService.getServiceId();
            String vehicleId = vehicleService.getVehicleId();
            
            System.out.println("   - Saving entity với composite key...");
            System.out.println("   - serviceId: " + serviceId);
            System.out.println("   - vehicleId: " + vehicleId);
            
            // Kiểm tra xem đã tồn tại chưa
            boolean exists = vehicleServiceRepository.existsById_ServiceIdAndId_VehicleId(serviceId, vehicleId);
            if (exists) {
                System.out.println("   - Đăng ký dịch vụ đã tồn tại, sẽ update...");
            }
            
            // Sử dụng repository.save() - sẽ insert hoặc update tùy vào tồn tại hay không
            Vehicleservice savedService = vehicleServiceRepository.save(vehicleService);
            
            // Flush để đảm bảo insert/update được thực thi
            vehicleServiceRepository.flush();
            
            System.out.println("   ✅ Entity đã được lưu thành công!");
            System.out.println("   - Service: " + savedService.getServiceId());
            System.out.println("   - Vehicle: " + savedService.getVehicleId());
            
            return savedService;
            
        } catch (Exception e) {
            System.err.println("   ❌ [SAVE ERROR] Lỗi khi save entity:");
            System.err.println("   - Error type: " + e.getClass().getName());
            System.err.println("   - Error message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("   - Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            // Re-throw exception để transaction rollback
            throw e;
        }
    }

    /**
     * Kiểm tra service và vehicle tồn tại
     */
    public ServiceType validateAndGetService(String serviceId) {
        Optional<ServiceType> serviceOpt = serviceRepository.findById(serviceId);
        if (serviceOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy dịch vụ với ID: " + serviceId);
        }
        return serviceOpt.get();
    }

    /**
     * Kiểm tra vehicle tồn tại
     */
    public Vehicle validateAndGetVehicle(String vehicleId) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
        if (vehicleOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy xe với ID: " + vehicleId);
        }
        return vehicleOpt.get();
    }

    /**
     * Tạo Vehicleservice entity từ request data
     */
    public Vehicleservice createVehicleService(
            ServiceType service,
            Vehicle vehicle,
            String serviceDescription,
            String status) {
        
        Vehicleservice vehicleService = new Vehicleservice();
        
        // Tạo composite key
        VehicleServiceId id = new VehicleServiceId(service.getServiceId(), vehicle.getVehicleId());
        vehicleService.setId(id);
        
        vehicleService.setService(service);
        vehicleService.setVehicle(vehicle);
        vehicleService.setServiceName(service.getServiceName());
        vehicleService.setServiceType(service.getServiceType());
        
        if (serviceDescription != null && !serviceDescription.trim().isEmpty()) {
            vehicleService.setServiceDescription(serviceDescription.trim());
        }
        
        if (status == null || status.trim().isEmpty()) {
            status = "pending";
        }
        vehicleService.setStatus(status);
        vehicleService.setRequestDate(Instant.now());
        
        return vehicleService;
    }
}

