package com.example.groupmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vote_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private GroupVote vote;
    
    @Column(nullable = false)
    private String userId; // Reference to User Service
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private VoteOption selectedOption;
    
    @Column(length = 500)
    private String comment;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime votedAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
