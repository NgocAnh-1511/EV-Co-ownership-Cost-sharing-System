package com.example.costpayment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CostShare")
public class CostShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shareId")
    private Integer shareId;

    @Column(name = "costId", nullable = false)
    private Integer costId;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(name = "percent", nullable = false)
    private Double percent;

    @Column(name = "amountShare", nullable = false)
    private Double amountShare;

    @Column(name = "calculatedAt")
    private LocalDateTime calculatedAt;

    // Constructors
    public CostShare() {
        this.calculatedAt = LocalDateTime.now();
    }

    public CostShare(Integer costId, Integer userId, Double percent, Double amountShare) {
        this();
        this.costId = costId;
        this.userId = userId;
        this.percent = percent;
        this.amountShare = amountShare;
    }

    // Getters and Setters
    public Integer getShareId() {
        return shareId;
    }

    public void setShareId(Integer shareId) {
        this.shareId = shareId;
    }

    public Integer getCostId() {
        return costId;
    }

    public void setCostId(Integer costId) {
        this.costId = costId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public Double getAmountShare() {
        return amountShare;
    }

    public void setAmountShare(Double amountShare) {
        this.amountShare = amountShare;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    @Override
    public String toString() {
        return "CostShare{" +
                "shareId=" + shareId +
                ", costId=" + costId +
                ", userId=" + userId +
                ", percent=" + percent +
                ", amountShare=" + amountShare +
                ", calculatedAt=" + calculatedAt +
                '}';
    }
}