package com.example.reservationservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name = "vehicle_groups")
@Data @NoArgsConstructor @AllArgsConstructor
public class VehicleGroup {
    @Id
    @Column(name = "group_id", length = 20)
    private String groupId;
    
    private String groupName;
    
    private String description;
    
    @Column(name = "creation_date")
    private LocalDateTime creationDate;
    
    @Column(name = "active")
    private String active;
}
