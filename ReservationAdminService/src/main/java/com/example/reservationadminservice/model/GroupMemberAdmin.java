package com.example.reservationadminservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_members")
@Getter @Setter @NoArgsConstructor
public class GroupMemberAdmin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long member_id;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private VehicleGroupAdmin group;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Double ownership_percentage;
}
