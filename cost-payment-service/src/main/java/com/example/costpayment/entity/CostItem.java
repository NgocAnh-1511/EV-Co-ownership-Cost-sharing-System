package com.example.costpayment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cost_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CostCategory category;
    
    @Column(nullable = false)
    private String groupId; // Reference to Group Management Service
    
    @Column(nullable = false)
    private String vehicleId; // Reference to Vehicle Management Service
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private String currency = "VND";
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CostStatus status = CostStatus.PENDING;
    
    @Column(nullable = false)
    private LocalDateTime incurredDate;
    
    @Column
    private LocalDateTime dueDate;
    
    @Column(length = 100)
    private String invoiceNumber;
    
    @Column(length = 500)
    private String receiptUrl;
    
    @OneToMany(mappedBy = "costItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CostSplit> costSplits;
    
    @OneToMany(mappedBy = "costItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum CostStatus {
        PENDING, APPROVED, PAID, OVERDUE, CANCELLED
    }
}
