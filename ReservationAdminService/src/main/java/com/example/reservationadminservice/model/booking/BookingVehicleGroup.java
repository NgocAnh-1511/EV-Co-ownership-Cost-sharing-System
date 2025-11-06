package com.example.reservationadminservice.model.booking;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity cho bảng vehicle_groups trong booking database (read-only)
 */
@Entity
@Table(name = "vehicle_groups")
@Getter
@Setter
public class BookingVehicleGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;
    
    @Column(name = "group_name")
    private String groupName;
    
    @Column(name = "description")
    private String description;
}

