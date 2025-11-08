package com.example.VehicleServiceManagementService.model;

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
           @Index(name = "idx_status", columnList = "status")
       })
public class Vehicleservice {
    
    @EmbeddedId
    private VehicleServiceId id;

    @NotNull(message = "Service không được để trống")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "service_id", nullable = false, insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_vehicleservice_service"))
    private ServiceType service;

    @NotNull(message = "Vehicle không được để trống")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false, insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_vehicleservice_vehicle"))
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
        return id != null ? id.getServiceId() : (service != null ? service.getServiceId() : null);
    }
    
    public String getVehicleId() {
        return id != null ? id.getVehicleId() : (vehicle != null ? vehicle.getVehicleId() : null);
    }
    
    // Method để khởi tạo id từ service và vehicle
    public void initializeId() {
        if (id == null && service != null && vehicle != null) {
            id = new VehicleServiceId(service.getServiceId(), vehicle.getVehicleId());
        }
    }
}