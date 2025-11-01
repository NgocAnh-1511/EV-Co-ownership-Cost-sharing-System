package com.example.reservationadminservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_groups")
@Getter @Setter @NoArgsConstructor
public class VehicleGroupAdmin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name")
    private String groupName;
    
    @Column(name = "description")
    private String description;
}
