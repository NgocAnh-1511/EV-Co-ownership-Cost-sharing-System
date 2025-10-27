package com.example.ui.dto;

public class GroupSummaryDto {
    private int totalGroups;
    private int totalMembers;
    private int totalCars;
    private int activeGroups;

    public GroupSummaryDto() {}

    public GroupSummaryDto(int totalGroups, int totalMembers, int totalCars, int activeGroups) {
        this.totalGroups = totalGroups;
        this.totalMembers = totalMembers;
        this.totalCars = totalCars;
        this.activeGroups = activeGroups;
    }

    public int getTotalGroups() {
        return totalGroups;
    }

    public void setTotalGroups(int totalGroups) {
        this.totalGroups = totalGroups;
    }

    public int getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(int totalMembers) {
        this.totalMembers = totalMembers;
    }

    public int getTotalCars() {
        return totalCars;
    }

    public void setTotalCars(int totalCars) {
        this.totalCars = totalCars;
    }

    public int getActiveGroups() {
        return activeGroups;
    }

    public void setActiveGroups(int activeGroups) {
        this.activeGroups = activeGroups;
    }
}
