package com.example.VehicleServiceManagementService.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Composite Primary Key cho Vehicleservice
 * Gồm service_id và vehicle_id
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VehicleServiceId implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Column(name = "service_id", nullable = false, length = 20)
    private String serviceId;
    
    @Column(name = "vehicle_id", nullable = false, length = 20)
    private String vehicleId;
}

