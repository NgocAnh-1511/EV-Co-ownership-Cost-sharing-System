package com.example.VehicleServiceManagementService.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vehiclegroup", schema = "vehicle_management")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // ✅ FIX ở đây
public class Vehiclegroup {

    @Id
    @Column(name = "group_id", length = 20)
    private String groupId;

    @Column(name = "group_name", length = 100)
    private String groupName;

    @Column(name = "description", length = 255)
    private String description;
}
