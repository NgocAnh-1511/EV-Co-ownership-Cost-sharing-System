package com.example.costpayment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Cost")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "costId")
    private Integer costId;
    
    @Column(name = "vehicleId", nullable = false)
    private Integer vehicleId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "costType")
    private Cost.CostType costType = Cost.CostType.Other;
    
    @Column(name = "amount", nullable = false)
    private Double amount;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "cost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<CostShare> costShares;
    
    @OneToMany(mappedBy = "cost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Payment> payments;
    
    public enum CostType {
        ElectricCharge, Maintenance, Insurance, Inspection, Cleaning, Other
    }
}
