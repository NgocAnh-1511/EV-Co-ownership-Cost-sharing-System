package com.example.groupmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "co_ownership_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoOwnershipGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String groupName;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private String vehicleId; // Reference to Vehicle Management Service
    
    @Column(nullable = false)
    private String groupAdminId; // Reference to User Service
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupStatus status = GroupStatus.ACTIVE;
    
    @Column(nullable = false)
    private Double totalOwnershipPercentage = 100.0;
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GroupMember> members;
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GroupVote> votes;
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GroupFund> funds;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum GroupStatus {
        ACTIVE, INACTIVE, SUSPENDED, DISSOLVED
    }
}
