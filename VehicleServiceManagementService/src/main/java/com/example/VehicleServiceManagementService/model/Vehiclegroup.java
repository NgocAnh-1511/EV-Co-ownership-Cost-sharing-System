package com.example.VehicleServiceManagementService.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;


@Entity
@Table(name = "vehiclegroup", schema = "vehicle_management")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Vehiclegroup {

    @Id
    @Column(name = "group_id", length = 20)
    private String groupId;

    @Column(name = "group_name", length = 100)
    private String groupName;

    @Column(name = "description", length = 255)
    private String description;

    // Thay đổi kiểu từ int thành Integer để chấp nhận giá trị null
    @Column(name = "vehicle_count")
    private Integer vehicleCount;
    @Column(name = "active")
    private String active;  // Đổi kiểu thành String

    // Getter và Setter cho groupId
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    // Getter và Setter cho groupName
    public String getName() {
        return groupName;
    }

    public void setName(String groupName) {
        this.groupName = groupName;
    }

    // Getter và Setter cho description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getter và Setter cho vehicleCount
    public Integer getVehicleCount() {
        return vehicleCount;
    }

    public void setVehicleCount(Integer vehicleCount) {
        this.vehicleCount = vehicleCount;
    }
}
