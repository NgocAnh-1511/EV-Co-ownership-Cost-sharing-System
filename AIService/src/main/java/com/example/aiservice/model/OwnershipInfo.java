package com.example.aiservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Thông tin sở hữu của các co-owner trong một nhóm xe
 */
@Entity
@Table(name = "ownership_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnershipInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ownershipId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Long vehicleId;
    
    @Column(nullable = false)
    private Long groupId;
    
    /**
     * Tỷ lệ sở hữu (ví dụ: 40.0 = 40%)
     */
    @Column(nullable = false)
    private Double ownershipPercentage;
    
    /**
     * Vai trò trong nhóm: ADMIN, MEMBER
     */
    @Enumerated(EnumType.STRING)
    private Role role = Role.MEMBER;
    
    @Column(name = "joined_date")
    private LocalDateTime joinedDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum Role {
        ADMIN,  // Quản trị nhóm
        MEMBER  // Thành viên thông thường
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (joinedDate == null) {
            joinedDate = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
















