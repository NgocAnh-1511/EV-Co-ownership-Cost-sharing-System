package com.example.reservationadminservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_groups")
@Getter @Setter @NoArgsConstructor
public class VehicleGroupAdmin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long group_id;

    private String group_name;
    private String description;
}
