package com.example.VehicleServiceManagementService.service;

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
     * Sử dụng id AUTO_INCREMENT làm primary key
     * Cho phép đăng ký cùng một dịch vụ (service_id) cho cùng một xe (vehicle_id) nhiều lần
     */
    @Transactional
    public Vehicleservice saveVehicleService(Vehicleservice vehicleService) {
        System.out.println("   🔒 [SERVICE] Bắt đầu save entity trong transaction...");
        
        try {
            String serviceId = vehicleService.getServiceId();
            String vehicleId = vehicleService.getVehicleId();
            
            System.out.println("   - Saving entity với id AUTO_INCREMENT...");
            System.out.println("   - serviceId: " + serviceId);
            System.out.println("   - vehicleId: " + vehicleId);
            
            // Kiểm tra duplicate đã được xử lý ở controller layer
            // Ở đây chỉ cần đảm bảo không có conflict khi save
            System.out.println("   🔒 [SERVICE] Kiểm tra lại trước khi save...");
            
            // Kiểm tra xem có dịch vụ đang chờ không (double check)
            long activeCount = vehicleServiceRepository.countActiveByServiceIdAndVehicleId(serviceId, vehicleId);
            if (activeCount > 0) {
                System.out.println("   ⚠️ [SAVE CHECK] Vẫn còn dịch vụ đang chờ, không thể save");
                throw new IllegalArgumentException("Dịch vụ này đã được đăng ký cho xe này và đang trong trạng thái chờ xử lý.");
            }
            
            System.out.println("   - Đăng ký dịch vụ mới, sẽ insert...");
            System.out.println("   - serviceId: " + serviceId + ", vehicleId: " + vehicleId);
            
            // Đảm bảo service và vehicle được set
            if (vehicleService.getService() == null && serviceId != null) {
                ServiceType serviceEntity = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceId));
                vehicleService.setService(serviceEntity);
            }
            
            if (vehicleService.getVehicle() == null && vehicleId != null) {
                Vehicle vehicleEntity = vehicleRepository.findById(vehicleId)
                    .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));
                vehicleService.setVehicle(vehicleEntity);
            }
            
            // Đảm bảo requestDate được set
            if (vehicleService.getRequestDate() == null) {
                vehicleService.setRequestDate(Instant.now());
            }
            
            // Đảm bảo status được set
            if (vehicleService.getStatus() == null || vehicleService.getStatus().trim().isEmpty()) {
                vehicleService.setStatus("pending");
            }
            
            // Lưu entity (id sẽ được tự động generate bởi database)
            Vehicleservice savedService = vehicleServiceRepository.save(vehicleService);
            vehicleServiceRepository.flush();
            
            System.out.println("   ✅ Entity đã được lưu thành công!");
            System.out.println("   - ID: " + savedService.getId());
            System.out.println("   - Service: " + savedService.getServiceId());
            System.out.println("   - Vehicle: " + savedService.getVehicleId());
            
            // Đồng bộ trạng thái vehicle sau khi lưu vehicleservice
            try {
                syncVehicleStatus(vehicleId);
            } catch (Exception e) {
                System.err.println("   ⚠️ [SYNC WARNING] Lỗi khi đồng bộ vehicle status (không ảnh hưởng đến việc lưu): " + e.getMessage());
                // Không throw exception để không ảnh hưởng đến việc lưu vehicleservice
            }
            
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
        
        // id sẽ được tự động generate bởi database (AUTO_INCREMENT)
        // Không cần set id
        
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
    
    /**
     * Đồng bộ trạng thái xe (vehicle.status) dựa trên dịch vụ đang chờ (vehicleservice)
     * Logic:
     * - Nếu có dịch vụ đang chờ (pending/in_progress), cập nhật vehicle status theo serviceType
     * - Nếu không có dịch vụ nào đang chờ, set vehicle status = "ready" (hoặc giữ "in_use" nếu đang là "in_use")
     * 
     * Ưu tiên status:
     * 1. maintenance (bảo dưỡng)
     * 2. repair (sửa chữa)
     * 3. checking (kiểm tra)
     * 4. in_use (đang sử dụng) - chỉ khi không có dịch vụ đang chờ
     * 5. ready (sẵn sàng) - mặc định
     */
    @Transactional
    public void syncVehicleStatus(String vehicleId) {
        try {
            System.out.println("🔄 [SYNC VEHICLE STATUS] Bắt đầu đồng bộ trạng thái cho vehicle: " + vehicleId);
            
            // Lấy vehicle
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
            if (vehicleOpt.isEmpty()) {
                System.out.println("   ⚠️ Vehicle không tồn tại: " + vehicleId);
                return;
            }
            
            Vehicle vehicle = vehicleOpt.get();
            String currentStatus = vehicle.getStatus();
            
            // Lấy tất cả dịch vụ đang chờ (pending/in_progress) của vehicle này
            List<Vehicleservice> activeServices = vehicleServiceRepository.findByVehicle_VehicleId(vehicleId).stream()
                    .filter(vs -> {
                        String status = vs.getStatus();
                        if (status == null) return false;
                        String statusLower = status.toLowerCase().trim();
                        return statusLower.equals("pending") || 
                               statusLower.equals("in_progress") || 
                               statusLower.equals("in progress");
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            System.out.println("   - Số dịch vụ đang chờ: " + activeServices.size());
            
            String newStatus = null;
            
            if (!activeServices.isEmpty()) {
                // Có dịch vụ đang chờ - xác định status dựa trên serviceType
                // Ưu tiên: maintenance > repair > checking
                boolean hasMaintenance = false;
                boolean hasRepair = false;
                boolean hasChecking = false;
                
                for (Vehicleservice vs : activeServices) {
                    String serviceType = vs.getServiceType();
                    if (serviceType != null) {
                        String serviceTypeLower = serviceType.toLowerCase().trim();
                        if (serviceTypeLower.contains("maintenance") || serviceTypeLower.contains("bảo dưỡng")) {
                            hasMaintenance = true;
                        } else if (serviceTypeLower.contains("repair") || serviceTypeLower.contains("sửa chữa")) {
                            hasRepair = true;
                        } else if (serviceTypeLower.contains("checking") || serviceTypeLower.contains("kiểm tra")) {
                            hasChecking = true;
                        }
                    }
                }
                
                // Xác định status ưu tiên
                if (hasMaintenance) {
                    newStatus = "maintenance";
                } else if (hasRepair) {
                    newStatus = "repair";
                } else if (hasChecking) {
                    newStatus = "checking";
                } else {
                    // Nếu có dịch vụ khác nhưng không xác định được loại, dùng status đầu tiên
                    String firstServiceType = activeServices.get(0).getServiceType();
                    if (firstServiceType != null && !firstServiceType.trim().isEmpty()) {
                        newStatus = firstServiceType.toLowerCase().trim();
                    } else {
                        newStatus = "maintenance"; // Mặc định
                    }
                }
                
                System.out.println("   - Có dịch vụ đang chờ → Cập nhật vehicle status = " + newStatus);
            } else {
                // Không có dịch vụ nào đang chờ
                // Nếu vehicle đang là "in_use" hoặc "in-use", giữ nguyên
                // Nếu không, set về "ready"
                if (currentStatus != null && 
                    (currentStatus.equalsIgnoreCase("in_use") || 
                     currentStatus.equalsIgnoreCase("in-use") ||
                     currentStatus.equalsIgnoreCase("in use"))) {
                    newStatus = "in_use";
                    System.out.println("   - Không có dịch vụ đang chờ, giữ nguyên status = " + newStatus);
                } else {
                    newStatus = "ready";
                    System.out.println("   - Không có dịch vụ đang chờ → Cập nhật vehicle status = " + newStatus);
                }
            }
            
            // Chỉ cập nhật nếu status thay đổi
            if (newStatus != null && !newStatus.equals(currentStatus)) {
                vehicle.setStatus(newStatus);
                vehicleRepository.save(vehicle);
                vehicleRepository.flush();
                System.out.println("   ✅ Đã cập nhật vehicle status từ \"" + currentStatus + "\" thành \"" + newStatus + "\"");
            } else {
                System.out.println("   ℹ️ Vehicle status không thay đổi: " + currentStatus);
            }
            
        } catch (Exception e) {
            System.err.println("   ❌ [SYNC ERROR] Lỗi khi đồng bộ trạng thái vehicle: " + e.getMessage());
            e.printStackTrace();
            // Không throw exception để không ảnh hưởng đến luồng chính
        }
    }
    
    /**
     * Đồng bộ trạng thái cho tất cả vehicles
     */
    @Transactional
    public void syncAllVehicleStatuses() {
        try {
            System.out.println("🔄 [SYNC ALL VEHICLES] Bắt đầu đồng bộ trạng thái cho tất cả vehicles...");
            List<Vehicle> allVehicles = vehicleRepository.findAll();
            int count = 0;
            for (Vehicle vehicle : allVehicles) {
                syncVehicleStatus(vehicle.getVehicleId());
                count++;
            }
            System.out.println("✅ [SYNC ALL VEHICLES] Đã đồng bộ " + count + " vehicles");
        } catch (Exception e) {
            System.err.println("❌ [SYNC ALL ERROR] Lỗi khi đồng bộ tất cả vehicles: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

