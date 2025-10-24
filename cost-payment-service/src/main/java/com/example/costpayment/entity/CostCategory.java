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
@Table(name = "cost_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String categoryName;
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CostType costType;
    
    @Column(nullable = false)
    private Boolean isRecurring = false;
    
    @Column(nullable = false)
    private Boolean isShared = true; // Whether this cost is shared among co-owners
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SplitMethod splitMethod = SplitMethod.OWNERSHIP_PERCENTAGE;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CostItem> costItems;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum CostType {
        CHARGING, MAINTENANCE, INSURANCE, REGISTRATION, CLEANING, 
        PARKING, TOLLS, REPAIRS, UPGRADES, OTHER
    }
    
    public enum SplitMethod {
        OWNERSHIP_PERCENTAGE, USAGE_BASED, EQUAL_SPLIT, CUSTOM
    }
}
