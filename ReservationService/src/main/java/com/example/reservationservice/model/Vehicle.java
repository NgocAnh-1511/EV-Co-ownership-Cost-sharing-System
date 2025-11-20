package com.example.reservationservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "vehicles")
@Data @NoArgsConstructor @AllArgsConstructor
public class Vehicle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;

    private String vehicleName;
    private String licensePlate;
    private String vehicleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "group_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private VehicleGroup vehicleGroup;


    @Enumerated(EnumType.STRING)
    private Status status = Status.AVAILABLE;

    public enum Status { AVAILABLE, IN_USE, MAINTENANCE }
}
