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
@Table(name = "group_votes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupVote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private CoOwnershipGroup group;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteType voteType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteStatus status = VoteStatus.ACTIVE;
    
    @Column(nullable = false)
    private LocalDateTime startDate;
    
    @Column(nullable = false)
    private LocalDateTime endDate;
    
    @Column(nullable = false)
    private Integer requiredPercentage = 51; // Minimum percentage to pass
    
    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VoteOption> options;
    
    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VoteResponse> responses;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum VoteType {
        MAINTENANCE_UPGRADE, INSURANCE_CHANGE, VEHICLE_SALE, 
        NEW_MEMBER, MEMBER_REMOVAL, FUND_ALLOCATION, OTHER
    }
    
    public enum VoteStatus {
        ACTIVE, PASSED, REJECTED, EXPIRED, CANCELLED
    }
}
