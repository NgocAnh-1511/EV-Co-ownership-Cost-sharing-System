package com.example.costpayment.dto;

import com.example.costpayment.entity.Cost;
import java.time.LocalDateTime;

public class CostDto {
    private Integer costId;
    private Integer vehicleId;
    private String costType;
    private String costTypeDisplay;
    private Double amount;
    private String description;
    private LocalDateTime createdAt;

    // Constructors
    public CostDto() {}

    public CostDto(Cost cost) {
        this.costId = cost.getCostId();
        this.vehicleId = cost.getVehicleId();
        this.costType = cost.getCostType().name();
        this.costTypeDisplay = cost.getCostType().getDisplayName();
        this.amount = cost.getAmount();
        this.description = cost.getDescription();
        this.createdAt = cost.getCreatedAt();
    }

    // Getters and Setters
    public Integer getCostId() {
        return costId;
    }

    public void setCostId(Integer costId) {
        this.costId = costId;
    }

    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getCostType() {
        return costType;
    }

    public void setCostType(String costType) {
        this.costType = costType;
    }

    public String getCostTypeDisplay() {
        return costTypeDisplay;
    }

    public void setCostTypeDisplay(String costTypeDisplay) {
        this.costTypeDisplay = costTypeDisplay;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Convert to Entity
    public Cost toEntity() {
        Cost cost = new Cost();
        cost.setCostId(this.costId);
        cost.setVehicleId(this.vehicleId);
        if (this.costType != null) {
            cost.setCostType(Cost.CostType.valueOf(this.costType));
        }
        cost.setAmount(this.amount);
        cost.setDescription(this.description);
        cost.setCreatedAt(this.createdAt);
        return cost;
    }

    @Override
    public String toString() {
        return "CostDto{" +
                "costId=" + costId +
                ", vehicleId=" + vehicleId +
                ", costType='" + costType + '\'' +
                ", costTypeDisplay='" + costTypeDisplay + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
