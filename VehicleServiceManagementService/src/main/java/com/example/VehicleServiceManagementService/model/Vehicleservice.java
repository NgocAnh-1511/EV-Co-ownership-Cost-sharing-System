package com.example.VehicleServiceManagementService.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "vehicleservice", schema = "vehicle_management", 
       indexes = {
           @Index(name = "idx_vehicle_id", columnList = "vehicle_id"),
           @Index(name = "idx_service_id", columnList = "service_id"),
           @Index(name = "idx_status", columnList = "status"),
           @Index(name = "idx_service_vehicle", columnList = "service_id, vehicle_id")
       })
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "service", "vehicle"})
public class Vehicleservice {

    @Id  // Đánh dấu trường này là khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Để tự động tăng (auto-increment)
    @Column(name = "id", nullable = false)  // Cột "id" không thể là null
    private Integer id;

    // Thay đổi optional = true để có thể load được ngay cả khi foreign key không tồn tại
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceType service;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Size(max = 255)
    @Column(name = "service_name", length = 255)
    private String serviceName;

    @Lob
    @Column(name = "service_description", columnDefinition = "TEXT")
    private String serviceDescription;

    @Size(max = 50)
    @Column(name = "service_type", length = 50)
    private String serviceType;

    @Column(name = "request_date", nullable = false, updatable = false)
    private Instant requestDate;

    @Size(max = 50)
    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "completion_date", nullable = true)
    private Instant completionDate;

    
    // Helper methods để dễ dàng truy cập serviceId và vehicleId
    public String getServiceId() {
        return service != null ? service.getServiceId() : null;
    }
    
    public String getVehicleId() {
        return vehicle != null ? vehicle.getVehicleId() : null;
    }
    
    // Method để đảm bảo serviceType luôn được set từ ServiceType nếu null
    // Được gọi sau khi entity được load từ database
    @PostLoad
    public void ensureServiceType() {
        // Nếu serviceType null hoặc rỗng, lấy từ ServiceType entity
        if ((serviceType == null || serviceType.trim().isEmpty()) && service != null) {
            String serviceTypeFromService = service.getServiceType();
            if (serviceTypeFromService != null && !serviceTypeFromService.trim().isEmpty()) {
                this.serviceType = serviceTypeFromService;
            }
        }
        // Đảm bảo serviceName cũng được set nếu null
        if ((serviceName == null || serviceName.trim().isEmpty()) && service != null) {
            String serviceNameFromService = service.getServiceName();
            if (serviceNameFromService != null && !serviceNameFromService.trim().isEmpty()) {
                this.serviceName = serviceNameFromService;
            }
        }
    }
}