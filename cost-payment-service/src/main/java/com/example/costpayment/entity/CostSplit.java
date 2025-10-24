package com.example.costpayment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cost_splits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostSplit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_item_id", nullable = false)
    private CostItem costItem;
    
    @Column(nullable = false)
    private String userId; // Reference to User Service
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal ownershipPercentage;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal splitAmount;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SplitStatus status = SplitStatus.PENDING;
    
    @Column
    private LocalDateTime paidAt;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum SplitStatus {
        PENDING, PAID, OVERDUE, WAIVED
    }
}
