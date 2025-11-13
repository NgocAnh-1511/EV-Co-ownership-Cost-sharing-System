package com.example.VehicleServiceManagementService.service;

import com.example.VehicleServiceManagementService.model.ServiceType;
import com.example.VehicleServiceManagementService.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    /**
     * Lấy tất cả các dịch vụ từ bảng service
     * @return Danh sách tất cả dịch vụ
     */
    public List<ServiceType> getAllServices() {
        try {
            return serviceRepository.findAll();
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách dịch vụ: " + e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách dịch vụ", e);
        }
    }

    /**
     * Lấy dịch vụ theo ID
     * @param serviceId ID của dịch vụ
     * @return ServiceType nếu tìm thấy, null nếu không
     */
    public ServiceType getServiceById(String serviceId) {
        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Service ID không được để trống");
        }
        Optional<ServiceType> service = serviceRepository.findById(serviceId);
        return service.orElse(null);
    }

    /**
     * Tìm dịch vụ theo tên
     * @param serviceName Tên dịch vụ
     * @return ServiceType nếu tìm thấy, null nếu không
     */
    public ServiceType getServiceByName(String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            return null;
        }
        Optional<ServiceType> service = serviceRepository.findByServiceName(serviceName);
        return service.orElse(null);
    }

    /**
     * Kiểm tra dịch vụ có tồn tại không
     * @param serviceId ID của dịch vụ
     * @return true nếu tồn tại, false nếu không
     */
    public boolean existsById(String serviceId) {
        if (serviceId == null || serviceId.trim().isEmpty()) {
            return false;
        }
        return serviceRepository.existsById(serviceId);
    }

    /**
     * Tự động tạo service_id mới theo format SRV001, SRV002, SRV003, ...
     * @return Service ID mới
     */
    public String generateNextServiceId() {
        String maxServiceId = serviceRepository.findMaxServiceIdWithPrefix();
        
        if (maxServiceId == null || maxServiceId.trim().isEmpty()) {
            // Nếu chưa có service nào, bắt đầu từ SRV001
            return "SRV001";
        }
        
        // Tách số từ service_id (ví dụ: "SRV003" -> 3)
        try {
            String numberPart = maxServiceId.substring(3); // Bỏ qua "SRV"
            int nextNumber = Integer.parseInt(numberPart) + 1;
            return String.format("SRV%03d", nextNumber); // Format: SRV001, SRV002, ...
        } catch (Exception e) {
            // Nếu không parse được, bắt đầu từ SRV001
            System.err.println("Không thể parse service_id: " + maxServiceId + ", bắt đầu từ SRV001");
            return "SRV001";
        }
    }

    /**
     * Thêm dịch vụ mới
     * Nếu serviceId không được cung cấp, sẽ tự động generate theo format SRV001, SRV002, ...
     * @param service Dịch vụ cần thêm
     * @return ServiceType đã được lưu
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     * @throws DataIntegrityViolationException nếu serviceId đã tồn tại
     */
    public ServiceType addService(ServiceType service) {
        if (service == null) {
            throw new IllegalArgumentException("Service không được null");
        }
        
        // Tự động generate service_id nếu không có
        if (service.getServiceId() == null || service.getServiceId().trim().isEmpty()) {
            String generatedId = generateNextServiceId();
            service.setServiceId(generatedId);
            System.out.println("✅ Tự động tạo service_id: " + generatedId);
        }
        
        if (service.getServiceName() == null || service.getServiceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống");
        }
        if (service.getServiceType() == null || service.getServiceType().trim().isEmpty()) {
            throw new IllegalArgumentException("Loại dịch vụ không được để trống");
        }

        // Kiểm tra serviceId đã tồn tại chưa
        if (serviceRepository.existsById(service.getServiceId())) {
            throw new DataIntegrityViolationException("Service ID '" + service.getServiceId() + "' đã tồn tại");
        }

        try {
            return serviceRepository.save(service);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Không thể thêm dịch vụ: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật dịch vụ
     * @param serviceId ID của dịch vụ cần cập nhật
     * @param service Dịch vụ với thông tin mới
     * @return ServiceType đã được cập nhật, null nếu không tìm thấy
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public ServiceType updateService(String serviceId, ServiceType service) {
        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Service ID không được để trống");
        }
        if (service == null) {
            throw new IllegalArgumentException("Service không được null");
        }
        if (service.getServiceName() == null || service.getServiceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống");
        }
        if (service.getServiceType() == null || service.getServiceType().trim().isEmpty()) {
            throw new IllegalArgumentException("Loại dịch vụ không được để trống");
        }

        Optional<ServiceType> existingServiceOpt = serviceRepository.findById(serviceId);
        if (existingServiceOpt.isPresent()) {
            ServiceType existingService = existingServiceOpt.get();
            existingService.setServiceName(service.getServiceName());
            existingService.setServiceType(service.getServiceType());
            // updatedDate sẽ tự động được cập nhật bởi @UpdateTimestamp
            return serviceRepository.save(existingService);
        }
        return null;
    }

    /**
     * Xóa dịch vụ
     * @param serviceId ID của dịch vụ cần xóa
     * @return true nếu xóa thành công, false nếu không tìm thấy
     * @throws DataIntegrityViolationException nếu dịch vụ đang được sử dụng
     */
    public boolean deleteService(String serviceId) {
        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Service ID không được để trống");
        }

        if (!serviceRepository.existsById(serviceId)) {
            return false;
        }

        try {
            serviceRepository.deleteById(serviceId);
            return true;
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                "Không thể xóa dịch vụ '" + serviceId + "' vì đang được sử dụng trong hệ thống", e);
        }
    }

    /**
     * Đếm tổng số dịch vụ
     * @return Số lượng dịch vụ
     */
    public long count() {
        return serviceRepository.count();
    }

    /**
     * Lấy danh sách các loại dịch vụ duy nhất
     * @return Danh sách các loại dịch vụ
     */
    public List<String> getDistinctServiceTypes() {
        return serviceRepository.findDistinctServiceTypes();
    }

    /**
     * Lấy danh sách dịch vụ theo loại
     * @param serviceType Loại dịch vụ
     * @return Danh sách dịch vụ
     */
    public List<ServiceType> getServicesByType(String serviceType) {
        if (serviceType == null || serviceType.trim().isEmpty()) {
            return getAllServices();
        }
        return serviceRepository.findByServiceType(serviceType);
    }
}
