package com.example.costpayment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "CostShare")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostShare {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shareId")
    private Integer shareId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "costId", nullable = false)
    private Cost cost;
    
    @Column(name = "userId", nullable = false)
    private Integer userId;
    
    @Column(name = "percent")
    private Double percent = 0.0;
    
    @Column(name = "amountShare")
    private Double amountShare = 0.0;
    
    @Column(name = "calculatedAt")
    private LocalDateTime calculatedAt = LocalDateTime.now();
}
