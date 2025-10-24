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
@Table(name = "financial_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String groupId; // Reference to Group Management Service
    
    @Column(nullable = false)
    private String vehicleId; // Reference to Vehicle Management Service
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;
    
    @Column(nullable = false)
    private LocalDateTime reportPeriodStart;
    
    @Column(nullable = false)
    private LocalDateTime reportPeriodEnd;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCosts = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalOutstanding = BigDecimal.ZERO;
    
    @Column(length = 2000)
    private String summary;
    
    @Column(length = 500)
    private String reportUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.GENERATED;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum ReportType {
        MONTHLY, QUARTERLY, YEARLY, CUSTOM
    }
    
    public enum ReportStatus {
        GENERATED, APPROVED, PUBLISHED, ARCHIVED
    }
}
