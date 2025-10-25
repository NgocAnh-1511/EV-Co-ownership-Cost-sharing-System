package com.example.costpayment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "GroupFund")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupFund {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fundId")
    private Integer fundId;
    
    @Column(name = "groupId", nullable = false)
    private Integer groupId;
    
    @Column(name = "totalContributed")
    private Double totalContributed = 0.0;
    
    @Column(name = "currentBalance")
    private Double currentBalance = 0.0;
    
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
    
    @OneToMany(mappedBy = "groupFund", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<FundTransaction> fundTransactions;
}
