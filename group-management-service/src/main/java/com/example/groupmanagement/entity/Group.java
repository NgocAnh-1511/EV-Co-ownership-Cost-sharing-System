package com.example.groupmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "`Group`")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "groupId")
    private Integer groupId;
    
    @Column(name = "groupName", nullable = false, length = 100)
    private String groupName;
    
    @Column(name = "adminId", nullable = false)
    private Integer adminId;
    
    @Column(name = "vehicleId")
    private Integer vehicleId;
    
    @Column(name = "createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GroupStatus status = GroupStatus.Active;
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GroupMember> members;
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Voting> votings;
    
    public enum GroupStatus {
        Active, Inactive
    }
}
