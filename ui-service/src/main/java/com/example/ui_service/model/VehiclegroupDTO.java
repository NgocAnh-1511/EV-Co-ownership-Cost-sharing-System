package com.example.ui_service.model;

public class VehiclegroupDTO {

    private String groupId;
    private String groupName;
    private String description;
    private Integer vehicleCount = 0;  // Giá trị mặc định 0
    private String active = "active";

    // Constructors
    public VehiclegroupDTO() {}

    public VehiclegroupDTO(String groupId, String groupName, String description, Integer vehicleCount, String active) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
        this.vehicleCount = vehicleCount;
        this.active = active;
    }

    // Getters and Setters
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVehicleCount() {
        return vehicleCount;
    }

    public void setVehicleCount(Integer vehicleCount) {
        this.vehicleCount = vehicleCount;
    }


}
