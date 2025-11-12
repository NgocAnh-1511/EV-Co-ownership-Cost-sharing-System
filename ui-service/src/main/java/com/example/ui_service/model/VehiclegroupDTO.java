package com.example.ui_service.model;

public class VehiclegroupDTO {

    private String groupId;
    private String name;
    private String description;
    private String active = "active";

    // Constructors
    public VehiclegroupDTO() {}

    public VehiclegroupDTO(String groupId, String name, String description, String active) {
        this.groupId = groupId;
        this.name = name;
        this.description = description;
        this.active = active;
    }

    // Getters and Setters
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String groupName) {
        this.name = groupName;
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
}
