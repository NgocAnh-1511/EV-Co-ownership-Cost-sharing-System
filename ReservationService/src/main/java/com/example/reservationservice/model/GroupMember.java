package com.example.reservationservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id","user_id"}))
@Data @NoArgsConstructor @AllArgsConstructor
public class GroupMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "group_id", referencedColumnName = "group_id")
    private VehicleGroup group;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    private Double ownershipPercentage;
}
