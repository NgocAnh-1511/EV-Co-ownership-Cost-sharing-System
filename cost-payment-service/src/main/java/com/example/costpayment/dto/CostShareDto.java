package com.example.costpayment.dto;

import java.time.LocalDateTime;

public class CostShareDto {
    private Integer shareId;
    private Integer costId;
    private Integer userId;
    private Double percent;
    private Double amountShare;
    private LocalDateTime calculatedAt;

    // Constructors
    public CostShareDto() {}

    public CostShareDto(Integer shareId, Integer costId, Integer userId, Double percent, Double amountShare, LocalDateTime calculatedAt) {
        this.shareId = shareId;
        this.costId = costId;
        this.userId = userId;
        this.percent = percent;
        this.amountShare = amountShare;
        this.calculatedAt = calculatedAt;
    }

    // Getters & Setters
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
        return "CostShareDto{" +
                "shareId=" + shareId +
                ", costId=" + costId +
                ", userId=" + userId +
                ", percent=" + percent +
                ", amountShare=" + amountShare +
                ", calculatedAt=" + calculatedAt +
                '}';
    }
}
