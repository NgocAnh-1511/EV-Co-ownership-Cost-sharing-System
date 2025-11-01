package com.example.VehicleServiceManagementService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "vehicleservice", schema = "vehicle_management")
public class Vehicleservice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Size(max = 255)
    @Column(name = "service_name")
    private String serviceName;

    @Lob
    @Column(name = "service_description")
    private String serviceDescription;

    @Size(max = 50)
    @Column(name = "service_type", length = 50)
    private String serviceType;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "request_date")
    private Instant requestDate;

    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "completion_date")
    private Instant completionDate;

}