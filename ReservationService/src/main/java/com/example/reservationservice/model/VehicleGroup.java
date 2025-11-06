package com.example.reservationservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "vehicle_groups")
@Data @NoArgsConstructor @AllArgsConstructor
public class VehicleGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;
    private String groupName;
    private String description;
}
