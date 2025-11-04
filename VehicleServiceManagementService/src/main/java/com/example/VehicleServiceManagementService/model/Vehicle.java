package com.example.VehicleServiceManagementService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vehicle", schema = "vehicle_management")
public class Vehicle {

    @Id
    @Column(name = "vehicle_id", length = 20, nullable = false)
    private String vehicleId; // 🔹 Dạng "VEH001", dùng String thay vì Integer/Long

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Vehiclegroup group;


    @Size(max = 20)
    @Column(name = "vehicle_number", length = 20)
    private String vehicleNumber; // 🔹 Biển số xe, ví dụ: "30A-12345"

    @Size(max = 50)
    @Column(name = "vehicle_type", length = 50)
    private String vehicleType;

    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status;

    // 🔹 Constructors
    public Vehicle() {}

    public Vehicle(String vehicleId, String vehicleNumber,
                   String vehicleType, String status) {
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.status = status;
    }

    // 🔹 Convenience getters cho hiển thị
    public String getDisplayName() {
        return vehicleNumber != null ? vehicleNumber : vehicleId;
    }
}
